# 3. Eliminar investigadores con Saga coreografiada y Transactional Outbox

Estado: Aceptado

## Contexto

Eliminar un investigador obliga a tocar dos agregados que viven en dos servicios con dos bases: el investigador en PostgreSQL y sus publicaciones en MongoDB. El ADR-001 descartó la transacción distribuida, así que no existe un `commit` que abarque ambos.

Hay una restricción adicional que no es técnica sino de negocio: **no se puede perder el evento**. Si `investigadores-service` marca al investigador como eliminado y el aviso a publicaciones se pierde porque Kafka está caído, las publicaciones quedan huérfanas sin que nadie lo sepa y no hay forma de recuperarlo.

## Decisión

Saga **coreografiada** —sin coordinador central— sobre Kafka, publicando con **Transactional Outbox**:

1. `POST /api/investigadores/{id}/eliminar` marca al investigador como `EN_ELIMINACION` y escribe el evento en la tabla `outbox_event`, **en la misma transacción** que el cambio de estado.
2. `OutboxPoller` lee hasta 50 eventos no publicados cada 2 s, los envía a Kafka y los marca como publicados. Si un envío falla, **corta el bucle** para conservar el orden y reintenta en el siguiente ciclo.
3. `publicaciones-service` consume `investigadores.en-eliminacion`, marca las publicaciones como huérfanas y emite `publicaciones.marcadas-huerfanas`.
4. `PublicacionesMarcadasListener` en investigadores consume esa confirmación y ejecuta `confirmarEliminacion`, que **solo borra si el investigador sigue en `EN_ELIMINACION`**.

Ningún servicio dirige al otro: cada uno reacciona a un hecho publicado.

## Alternativas descartadas

**Orquestación con un coordinador central.** Habría hecho el flujo más fácil de leer y de depurar, pero introduce un servicio que conoce los dos dominios y que se convierte en punto único de fallo. Para un flujo de dos pasos, el coordinador cuesta más de lo que ahorra.

**Borrado en cascada desde `investigadores-service`** llamando por REST a publicaciones. Se descartó porque acopla los servicios de forma síncrona: si publicaciones está lento o caído, la baja del investigador falla o queda a medias. Además el reintento manual perdería la trazabilidad del estado intermedio.

**Publicar el evento directamente con `KafkaTemplate` sin Outbox.** Es lo más simple y lo que hace hoy `publicaciones-service` con sus propios eventos (ver ADR-004). Se descartó aquí porque en la baja de un investigador la pérdida del aviso deja datos inconsistentes de forma permanente, no recuperable.

## Consecuencias

(+) **El evento no se pierde si Kafka está caído.** Queda en `outbox_event` y se publica al volver. Es la garantía que motivó la decisión.

(+) **El orden se conserva.** El poller corta el bucle al primer fallo en vez de saltarse el evento y continuar.

(+) **Servicios desacoplados.** `investigadores-service` no sabe quién consume sus eventos ni cuántos consumidores hay.

(+) **`iniciarEliminacion` es idempotente**: si el investigador ya está en `EN_ELIMINACION`, retorna sin publicar otro evento.

(−) **Consistencia eventual con estado intermedio visible.** Durante la Saga el investigador figura como `EN_ELIMINACION`. Si nadie confirma, **queda así indefinidamente**: no hay tiempo límite ni compensación que revierta la baja. Es el riesgo principal del ATAM-lite.

(−) **El consumidor no es idempotente.** `PublicacionesMarcadasListener` no lleva registro de mensajes procesados. Kafka garantiza entrega "al menos una vez", así que un duplicado repetiría `confirmarEliminacion`. En este caso concreto el daño es menor —borrar algo ya borrado—, pero el patrón no se puede reutilizar tal cual para un efecto acumulativo.

(−) **El intervalo del poller fija la latencia mínima de toda la Saga**: 2 s de base, más el consumo. Es un punto de sensibilidad, no un detalle de configuración.

(−) **La tabla de outbox crece sin política de limpieza.** No hay purga de eventos ya publicados; en un sistema real necesitaría un trabajo de archivado.

(−) **El orden es por `id` ascendente dentro del lote de 50.** Correcto para un agregado, pero si un evento se atasca, todo el outbox se detiene detrás de él (head-of-line blocking).
