# ATAM-lite — rica-api (L5 + L6 + L8)

**Fecha:** 2026-11-15
**Alcance:** `investigadores-service`, `publicaciones-service`, `consultas-service`, `eureka-server`, `gateway`. Código hasta L8. L7 no tiene taller de código; este documento es su entregable.
**Estado del que se parte:** 25 tests verdes (11 + 7 + 7), verificación manual de gRPC y de la Saga.

Este documento aplica el método ATAM en su versión reducida: árbol de utilidad con cinco escenarios, y análisis de los de mayor riesgo con riesgos, no-riesgos, sensibilidades y compromisos.

---

## 1. Árbol de utilidad

Cinco atributos de calidad, cada uno con un escenario concreto y su par importancia/dificultad (A = alta, M = media, B = baja).

| Atributo | Escenario | Imp. | Dif. |
|---|---|:---:|:---:|
| **Disponibilidad** | Con `investigadores-service` caído, `GET /api/consultas/investigadores/{correo}` sigue devolviendo la ficha completa desde el modelo de lectura en menos de 1 s. | A | M |
| **Consistencia** | Al eliminar un investigador con publicaciones, estas quedan marcadas como huérfanas en ≤ 10 s **o** la baja se revierte. Ninguna publicación queda apuntando a un investigador inexistente. | A | A |
| **Rendimiento** | `GET /api/consultas/investigadores/{correo}` devuelve la ficha con todas sus publicaciones en p95 < 300 ms, **sin llamadas entre servicios** (CQRS nivel 2). | M | A |
| **Modificabilidad** | Cambiar el adaptador de verificación (gRPC → REST → cualquier otro) exige tocar un solo archivo y no modifica `PublicacionService` ni sus tests. | A | B |
| **Testabilidad** | Los casos de uso se prueban sin Spring ni base de datos. El build falla si el dominio importa infraestructura. | M | B |

**Lectura del árbol:** el escenario de **consistencia** es el único con importancia alta **y** dificultad alta. Es donde una evaluación ATAM pone primero la lupa, y es el que se analiza en la sección 2. Los escenarios de disponibilidad y rendimiento se resolvieron con `consultas-service` (ADR-004).

---

## 2. Análisis del escenario de mayor riesgo

**Escenario:** eliminar un investigador cuyas publicaciones viven en otra base.

| Elemento | Análisis |
|---|---|
| **Enfoques arquitectónicos** | Saga coreografiada sobre Kafka; Transactional Outbox para publicar; estado intermedio `EN_ELIMINACION` (ADR-003) |
| **Cómo responde** | `investigadores-service` marca `EN_ELIMINACION` y escribe el evento en `outbox_event` en la misma transacción. El poller lo publica en ≤ 2 s. `publicaciones-service` marca las publicaciones como huérfanas y confirma. Solo entonces se borra al investigador. |
| **Punto de sensibilidad** | El `fixedDelay = 2000` del `OutboxPoller`. Fija el mínimo de latencia de toda la Saga: ningún ajuste en otro componente puede bajar de ahí. |
| **Punto de compromiso** | Consistencia eventual. Durante la Saga el investigador figura como `EN_ELIMINACION`. Se gana independencia de servicios y tolerancia a la caída de Kafka, a cambio de una ventana con estado intermedio visible. |
| **No-riesgo** | El Outbox impide perder el evento aunque Kafka esté caído: el mensaje se queda en la tabla y se reintenta. Es la decisión del ADR-003 y está verificada. |
| **No-riesgo** | Cada servicio con su propia base: un cambio de esquema en publicaciones no afecta a investigadores (ADR-001). |

### Riesgos

| # | Riesgo | Decisión |
|---|---|---|
| **R1** | **No hay tiempo límite ni compensación.** Si `publicaciones-service` nunca confirma, el investigador queda `EN_ELIMINACION` para siempre y no se puede volver a dar de baja ni reactivar. | **Mitigar.** Añadir un `EN_ELIMINACION` con marca de tiempo y un job que revierta a `ACTIVO` pasados N minutos sin confirmación. Es la acción pendiente más importante. |
| **R2** | **El consumidor no es idempotente.** `PublicacionesMarcadasListener` no registra mensajes procesados; Kafka entrega "al menos una vez". | **Aceptar (por ahora).** El efecto es borrar algo ya borrado, que es idempotente por naturaleza. Se documenta para no reutilizar el patrón en un flujo acumulativo. |
| **R3** | **La tabla de outbox no se purga.** Crece indefinidamente. | **Mitigar.** Trabajo programado que archive eventos publicados con más de N días. |
| **R4** | **Head-of-line blocking.** Si un evento no se puede publicar, el poller corta el bucle y todos los siguientes esperan. | **Aceptar.** Es intencional para conservar el orden (ADR-003). Se mitiga con monitoreo del tamaño del outbox pendiente. |
| **R5** | **`publicaciones-service` publica sin Outbox.** Escritura dual: si cae entre guardar y publicar, el evento se pierde y `consultas-service` nunca actualiza la ficha. | **Mitigar.** Se acepta porque el modelo de lectura es reconstruible, pero un fallo silencioso deja la ficha desactualizada sin alerta. La acción concreta es mover publicaciones a Outbox, igual que investigadores. |
| **R6** | **El Circuit Breaker está construido a mano** en el constructor del adaptador, no por configuración. | **Aceptar.** Es deuda técnica reconocida; no afecta la corrección del comportamiento. |

### Puntos de compromiso

| Compromiso | Gana | Pierde |
|---|---|---|
| Mensajería asíncrona para la baja (ADR-003) | Disponibilidad: la baja no depende de que publicaciones esté arriba | Consistencia inmediata: ventana con `EN_ELIMINACION` |
| gRPC para la validación previa (ADR-002) | Rendimiento y contrato tipado | Acoplamiento en tiempo de ejecución: si investigadores está caído, no se registra la publicación (correcto para el negocio, pero es un punto único) |
| Base por servicio (ADR-001) | Modificabilidad e independencia de despliegue | Integridad referencial y consultas combinadas |

---

## 3. Riesgo que el árbol de utilidad deja al descubierto

El escenario de **rendimiento** (ficha del investigador en p95 < 300 ms) ya está resuelto en el código: `consultas-service` responde con una sola lectura sobre una colección desnormalizada, sin llamadas entre servicios (ADR-004, 7 tests).

Lo que el árbol deja al descubierto ahora es distinto: **el escenario de consistencia sigue sin resolverse** y los dos escenarios con mejor cobertura (disponibilidad y rendimiento) se apoyan en un modelo de lectura que hereda la debilidad del riesgo **R5** — `publicaciones-service` publica sin Outbox, así que un fallo entre guardar y publicar deja la ficha desactualizada sin que nada lo detecte.

Esto es exactamente lo que ATAM debe hacer visible: mejorar un atributo de calidad trasladó presión a otro. La proyección es más rápida y más disponible que la consulta cruzada que reemplazó, pero es **derivada** y su exactitud depende de que todos los eventos lleguen.

---

## 4. Funciones de aptitud

Se definen dos, ambas verificables y ejecutadas por el build:

| Función | Tipo | Qué protege | Dónde |
|---|---|---|---|
| El dominio no depende de la infraestructura | Atómica · disparada | La regla del ADR-001 y de L4 | `ArquitecturaTest` (ArchUnit) |
| El caso de uso no conoce al adaptador | Atómica · disparada | La regla del puerto (ADR-002): `PublicacionService` solo debe depender de `VerificadorInvestigador` | `ArquitecturaTest` (ArchUnit) |

La primera es la regla clásica de la Lección 4 convertida en build. La segunda protege el punto que hace valioso el taller L5→L6: que cambiar de tecnología no toque el núcleo.

**Lo que no hay:** ninguna función de aptitud **continua**. No hay métricas en producción, alertas de p95 ni monitoreo del tamaño del outbox pendiente. Se documenta como deuda, no como hecho.

---

## 5. Qué se aprendió de esta evaluación

1. El único riesgo que puede dejar el sistema en un estado **irrecuperable sin intervención manual** es la ausencia de compensación en la Saga (R1). No es un problema de rendimiento ni de estilo: es un estado del que no se sale solo.
2. El escenario de rendimiento está diseñado pero no construido. Separar "existe" de "está propuesto" evita vender como hecho lo que todavía es un ADR en estado Propuesta.
3. Las funciones de aptitud existentes son todas **disparadas**. Sin monitoreo, la arquitectura evoluciona a ciegas entre commits.
