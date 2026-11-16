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

## L8 — CQRS: ficha de lectura, proyección y ranking

| Captura | Qué muestra | Dónde está el código en `main` |
|---|---|---|
| `l8-01-ficha-json.png` | `GET /api/consultas/investigadores/ana.torres@uptc.edu.co` → 200 con `correo`, `nombreCompleto`, `estado`, `totalPublicaciones: 5` y la lista de publicaciones embebida | `ConsultaController`, `ServicioConsultas`, `FichaInvestigador`, `FichaResponse` |
| `l8-02-proyeccion-log.png` | Log del proyector actualizando la ficha a partir de los eventos de Kafka | `ProyectorFichas`, `ProyeccionListener` |
| `l8-03-ranking.png` | `GET /api/consultas/ranking?top=3` ordenado por `totalPublicaciones` | `ServicioConsultas.ranking` |

**Qué demuestra:** una sola lectura devuelve datos de dos servicios sin llamarlos, porque el
modelo de lectura se alimenta por eventos. Es CQRS nivel 2.

---

## L9 — EDA: contador de altas, idempotencia y Dead Letter Topic

| Captura | Qué muestra | Dónde está el código en `main` |
|---|---|---|
| `l9-01-estadisticas-antes.png` | `GET /api/consultas/estadisticas` → `{"altas": 3}` | `EstadisticasController`, `ContadorAltasService` |
| `l9-02-estadisticas-despues.png` | El mismo endpoint tras una alta nueva → `{"altas": 4}` | `ContadorAltasListener` (grupo propio) |
| `l9-03-idempotencia-log.png` | Log del contador registrando cada alta por su id (`id=2`, `id=3`, `id=4`) junto al del proyector actualizando la ficha | `ContadorAltasService` + `AltaProcesada` (clave única por id de evento) |
| `l9-04-dlt.png` | `kafka-console-consumer` sobre `investigadores.registrados.DLT` mostrando el mensaje venenoso `{esto no es json}` | `KafkaErrorHandlingConfig` (DLT + `ExponentialBackOff`), `TopicosConfig` |

**Qué demuestra:** el par 3 → 4 prueba que el contador procesa eventos reales; la captura de
idempotencia prueba que un duplicado no infla el total; la DLT prueba que un mensaje que no se
puede procesar **no se pierde ni bloquea la partición**.

---

## L10 — Observabilidad

| Captura | Qué muestra | Dónde está el código en `main` |
|---|---|---|
| `l10-01-actuator.png` | `/actuator/health` respondiendo `UP` | `spring-boot-starter-actuator` en el `pom.xml` |
| `l10-02-correlationid.png` | Log con `correlationId=<uuid>` en la línea | `CorrelationIdFilter`, patrón en `application.yml`, `OutboxPublicadorEventos` |

---

## L11 — Contenedores y CI

| Captura | Qué muestra | Dónde está el código en `main` |
|---|---|---|
| `l11-01-docker-ps.png` | Contenedores de infraestructura corriendo | `docker-compose.yml` raíz |
| `l11-02-ci.png` | Workflow `CI` en verde (run #11, commit `ef92b71`), matriz sobre 5 servicios | `.github/workflows/ci.yml` |

---

## Nombres verificables para la rúbrica MT1 y MT2

La rúbrica pide citar un nombre de clase, tópico, servicio o valor que exista en el
repositorio. Estos se comprueban con `grep` dentro de `main`:

**Para MT1 (talleres 5 y 6):**

1. **`VerificadorInvestigadorGrpc`** — clase en `publicaciones-service`
2. **`investigadores.en-eliminacion`** — tópico en `investigadores-service`
3. **`investigadores-service`** / **`publicaciones-service`** — módulos Maven en la raíz

**Para MT2 (talleres 8 y 9):**

1. **`FichaInvestigador`** / `totalPublicaciones` — el JSON de la ficha
2. **`ProyectorFichas`** — el log de la proyección
3. **`investigadores.registrados.DLT`** y `KafkaErrorHandlingConfig` — la salida de la DLT
4. **`/api/consultas/estadisticas`** con `"altas"` — el valor antes (3) y después (4)

---

## Nota de coherencia

Las capturas de L5 y L6 se tomaron ejecutando la rama `taller-microservicios`, que es el
contenido histórico del que derivó `main`. El código mostrado coincide con el de `main` salvo
por un detalle: `main` movió los puertos a `dominio/puertos` y `publicaciones/puertos`
(refactor de L4/L7). Los nombres citados arriba se conservan.

Las capturas de L8, L9, L10 y L11 se tomaron directamente sobre `main`.
