# 4. CQRS con modelo de lectura separado para la ficha del investigador

Estado: **Propuesta** — diseñado, no integrado en `main`

## Contexto

La pantalla principal de RICA muestra la **ficha de un investigador con sus publicaciones**. Hoy esos datos viven en dos servicios con dos bases: el investigador en PostgreSQL, sus publicaciones en MongoDB.

Resolver esa pantalla con la arquitectura actual obliga a `publicaciones-service` a preguntar por gRPC si el investigador existe y a que el cliente HTTP haga dos llamadas y las una en memoria. Si `investigadores-service` está lento o caído, la pantalla entera falla, aunque las publicaciones estén perfectamente disponibles. Y un ranking de investigadores por número de publicaciones no se puede resolver sin traer todas las publicaciones de todos los investigadores.

El taller de la Lección 8 construye `consultas-service` —un tercer servicio con su propia base de lectura— alimentado por los eventos que ya existen de L6.

## Decisión

**Propuesta, todavía no en `main`.** El diseño es CQRS de **nivel 2** (almacenes separados), no Event Sourcing:

- `consultas-service` mantiene una colección de **fichas desnormalizadas** (`FichaInvestigador`), cada una con el investigador y la lista de sus publicaciones.
- `ProyeccionListener` consume los tópicos `investigadores.registrados`, `publicaciones.registradas`, `investigadores.en-eliminacion` e `investigadores.eliminados`.
- `ProyectorFichas` aplica cada evento con tres garantías: **idempotencia** (una publicación no se agrega dos veces, se compara por `id`), **tolerancia al desorden** (crea la ficha parcial y la completa cuando llega el otro evento) y **reconstruibilidad** (el modelo de lectura se puede vaciar y reproducir desde el log de Kafka).
- `GET /api/consultas/investigadores/{correo}` devuelve la ficha completa en una sola lectura.

El estado actual es una rama `taller-l8-cqrs` con un commit (`46a9f49`) que **no está fusionado en `main`**. Por eso este ADR queda en estado Propuesta.

## Alternativas descartadas

**Nivel 1: modelos de lectura separados, misma base.** Habría resuelto la forma del DTO sin resolver el problema real —la consulta cruza dos servicios— y sin añadir un servicio. Se descartó porque no ataca el acoplamiento en tiempo de lectura.

**Que el cliente HTTP haga las dos llamadas y una en memoria.** Es lo que pasa hoy. Se descarta porque traslada la complejidad al frontend, mantiene la dependencia de que ambos servicios estén arriba y no permite un ranking sin traer todos los datos.

**Event Sourcing.** Se descartó explícitamente: guardar el estado como secuencia de eventos complica el dominio y la evolución de esquemas, y el proyecto no necesita historial completo. El estado actual en PostgreSQL y MongoDB sigue siendo la fuente de verdad; los eventos son notificaciones, no el registro.

**Una vista materializada en PostgreSQL.** No es posible sin compartir base entre servicios, lo que viola el ADR-001.

## Consecuencias

**Si se integra:**

(+) **Una sola lectura devuelve la ficha completa.** Se elimina la unión en memoria y la dependencia de que los servicios de escritura estén arriba para consultar.

(+) **Las consultas sobreviven a la caída de los servicios de escritura**, que es precisamente el escenario de disponibilidad del árbol de utilidad.

(+) **Un ranking por publicaciones se resuelve con un índice**, no con una agregación en memoria.

(+) **El modelo de lectura es descartable**: si se corrompe, se reconstruye reproduciendo eventos.

(−) **Un servicio más, una base más y un despliegue más.** Para un proyecto de curso es costo real, no teórico.

(−) **Consistencia eventual visible en la interfaz.** Tras registrar una publicación, la ficha puede tardar en reflejarla. Hay que decidir qué se muestra mientras tanto.

(−) **`publicaciones-service` publica con `KafkaTemplate` directo, sin Outbox.** Es una escritura dual: si el servicio cae entre guardar y publicar, la ficha nunca se entera. Se mitiga porque el modelo es reconstruible, pero un diseño de producción usaría Outbox también aquí.

(−) **La reconstrucción completa depende de la retención de Kafka.** Si los eventos antiguos se borraron, no se pueden reproducir.

(−) **Riesgo de ficha fantasma en una reproducción.** Los tópicos no tienen orden entre sí: una reproducción puede procesar "investigador eliminado" antes que "investigador registrado" y dejar una ficha que no debería existir.

## Nota sobre el estado

Este ADR existe para dejar constancia de una decisión **diseñada y no desplegada**. La documentación de la Lección 12 se evalúa tal como está construido el sistema: describir `consultas-service` como si estuviera en `main` sería una inconsistencia verificable con `git log`. Cuando la rama se integre, este documento pasa a Estado: Aceptado y se actualiza el commit de referencia.
