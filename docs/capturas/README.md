# Evidencias de ejecución — Talleres L5 y L6

Capturas de los servicios corriendo y de los logs de comunicación, tomadas el 2026-11-15.

Cada captura se referencia al código de `main` para que quien evalúe pueda comprobar que
el nombre que aparece en la imagen existe en el repositorio.

---

## L5 — dos servicios, dos bases, puerto sin red

| Captura | Qué muestra | Dónde está el código en `main` |
|---|---|---|
| `l5-01-compose-up.png` | Postgres (`investigadores-db`) y Mongo (`publicaciones-mongo`) corriendo | `investigadores-service/docker-compose.yml`, `publicaciones-service/docker-compose.yml` |
| `l5-02-investigadores-running.png` | `investigadores-service` en Tomcat, puerto 8080 | `investigadores-service` |
| `l5-03-publicaciones-running.png` | `publicaciones-service` en Tomcat, puerto 8081 | `publicaciones-service` |
| `l5-04-verificador-pendiente.png` | `VerificadorInvestigadorPendiente` devolviendo `true` | Clase histórica del taller L5; en `main` fue reemplazada por `VerificadorInvestigadorGrpc` (ver L6). El puerto que se conserva es `VerificadorInvestigador` |
| `l5-05-bruno-l5.png` | Creación de investigador y publicación respondiendo 201 | Colecciones en `bruno/` |

**Por qué `VerificadorInvestigadorPendiente` no está en `main`:** es el adaptador deliberadamente
incompleto del taller L5, que permitía arrancar sin red. En L6 se reemplazó por el adaptador
gRPC real. Lo que demuestra el taller es que **el puerto no cambió** — `PublicacionService`
siguió dependiendo de `VerificadorInvestigador`, y solo cambió el adaptador.

---

## L6 — gRPC, Kafka, Saga y resiliencia

| Captura | Qué muestra | Dónde está el código en `main` |
|---|---|---|
| `l6-01-kafka-running.png` | Kafka (`rica-kafka`) corriendo junto a las dos bases | `investigadores-service/docker-compose.yml` (servicio `kafka`) |
| `l6-02-eureka.png` | `INVESTIGADORES-SERVICE` y `PUBLICACIONES-SERVICE` registrados | `eureka-server` |
| `l6-03-verificador-grpc.png` | `VerificadorInvestigadorGrpc` con `withDeadlineAfter(5, TimeUnit.SECONDS)` y Circuit Breaker | `publicaciones-service/.../VerificadorInvestigadorGrpc.java` |
| `l6-04-grpc-log.png` | Log `gRPC ExisteInvestigador(...) -> true` al crear una publicación | `VerificadorInvestigadorGrpc.java` (método `existe`) |
| `l6-05-saga-kafka-log.png` | Eventos de la Saga: `investigadores.en-eliminacion` y `publicaciones.marcadas-huerfanas` | `investigadores-service/.../InvestigadorService.java`, `publicaciones-service/.../InvestigadorEnEliminacionListener.java` |
| `l6-06-circuit-breaker-503.png` | Publicación rechazada con 503 cuando el servidor gRPC está caído | `ServicioNoDisponibleException` y el advice en `publicaciones-service` |

---

## Nombres verificables para la rúbrica MT1

La rúbrica pide citar un nombre de clase, tópico o servicio que exista en el repositorio.
Estos tres existen en `main` y se pueden comprobar con `grep`:

1. **`VerificadorInvestigadorGrpc`** — clase en `publicaciones-service`
2. **`investigadores.en-eliminacion`** — tópico en `investigadores-service`
3. **`investigadores-service`** / **`publicaciones-service`** — módulos Maven en la raíz

---

## Nota de coherencia

Las capturas se tomaron ejecutando la rama `taller-microservicios`, que es el contenido
histórico L5/L6 del que derivó `main`. El código mostrado en las capturas coincide con el de
`main` salvo por un detalle: `main` movió los puertos a `dominio/puertos` y
`publicaciones/puertos` (refactor del taller L4/L7). Los nombres de clase, tópicos y servicios
citados arriba se conservan.
