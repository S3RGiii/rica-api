# Evidencias de verificación

**Fecha de las ejecuciones:** 2026-11-03
**Entorno:** Windows 11 · Java 25.0.4.1 · Maven 3.9.16 (wrapper) · Docker Desktop con PostgreSQL 16 y MongoDB 7.
**Nota:** las fechas de los commits son las originales; no se reescribieron. Este archivo documenta lo que se ejecutó con y sin infraestructura.

---

## 1. `rica-api` — rama `taller-hexagonal` (L4)

**Commit:** `9e2b3c8` (2026-10-06)
**Comando:** `.\mvnw.cmd test`

**Resultado:** **31/31 tests verdes · BUILD SUCCESS** con PostgreSQL y MongoDB levantados.

| Clase | Tests | Resultado |
|---|---:|---|
| `CorreoInstitucionalTest` | 3 | ✅ |
| `GrupoInvestigacionTest` | 3 | ✅ |
| `InvestigadorControllerTest` | 3 | ✅ |
| `InvestigadorFactoryTest` | 3 | ✅ |
| `InvestigadorServiceTest` | 5 | ✅ |
| `LimitePublicacionesAnualesServiceTest` | 4 | ✅ |
| `PublicacionControllerTest` | 3 | ✅ |
| `PublicacionServiceTest` | 4 | ✅ |
| `InvestigadorRepositoryTest` | 2 | ✅ con PostgreSQL |
| `RicaApiApplicationTests` | 1 | ✅ con PostgreSQL/MongoDB |
| **Total** | **31** | **31 ✅ · BUILD SUCCESS** |

**Verificación con infraestructura (2026-11-03):**

```powershell
cd proyectos\Clases\rica-api
docker compose -f docker-compose.yaml up -d
.\mvnw.cmd test
```

Salida: `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0` · `BUILD SUCCESS`.

**Sin infraestructura:** 28/31. Los 3 de integración fallan con `Connection to localhost:5432 refused`; no son fallos de lógica: el contexto de Spring no puede inicializar Flyway sin PostgreSQL.

---

## 2. Taller L5 — `rica-microservicios`

**Ubicación local:** `Talleres/Leccion-05-Microservicios/rica-microservicios`
**Estado:** dos servicios (`investigadores-service` 8080, `publicaciones-service` 8081), PostgreSQL/MongoDB, REST.
**Pendiente a propósito:** `VerificadorInvestigadorPendiente` devuelve `true`; es el TODO de gRPC.

| Servicio | Comando | Resultado |
|---|---|---:|
| `investigadores-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| `publicaciones-service` | `.\mvnw.cmd test` | 2 tests ✅ |
| **Total** | | **9 tests ✅ · BUILD SUCCESS** |

**Bruno:** 7 requests (investigadores: listar, crear, status; publicaciones: crear con correo real, crear con correo inventado, listar, status).

---

## 3. Taller L6 — `rica-microservicios-taller`

**Ubicación local:** `Talleres/Leccion-06-Comunicacion/rica-microservicios-taller`
**Rama:** `taller-microservicios`
**Commit:** `3f69c37` (2026-10-20)
**Estado:** cuatro módulos (`investigadores-service`, `publicaciones-service`, `eureka-server`, `gateway`); gRPC, Kafka, Outbox, Saga, Resilience4j, Eureka y Gateway.

| Servicio | Comando | Resultado |
|---|---|---:|
| `investigadores-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| `publicaciones-service` | `.\mvnw.cmd test` | 2 tests ✅ |
| **Total** | | **9 tests ✅ · BUILD SUCCESS** |

**Bruno:** 8 requests (incluye `4-eliminar-investigador` para la Saga).

**Cobertura del gRPC:** los 9 tests son de dominio/aplicación. El adaptador gRPC no tiene test automático; se verifica con `grpcurl` y Bruno:

```powershell
docker run --rm fullstorydev/grpcurl -plaintext host.docker.internal:9090 list
$json = '{"correoInstitucional":"ana@uptc.edu.co"}'
$json | docker run --rm -i fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 InvestigadorGrpc/ExisteInvestigador
```

**Infraestructura:** el repo L6 no incluye un `docker-compose` raíz con Kafka/Eureka/Gateway; solo los compose de PostgreSQL y MongoDB por servicio. Para el gRPC bastan los dos servicios y sus bases. Para el resto de L6, Kafka/Eureka/Gateway se levantan aparte.

---

## 4. Lectura de la evidencia

- **L4:** `taller-hexagonal`, commit `9e2b3c8` (2026-10-06). 31 tests definidos; 28 verdes sin base y 3 de integración pendientes de PostgreSQL.
- **L5:** dos servicios y el puerto `VerificadorInvestigador`; 9 tests verdes.
- **L6:** gRPC real, Kafka, Outbox, Saga, Eureka y Gateway; 9 tests verdes; gRPC verificado manualmente.
- **Fechas:** los commits son del 6 y 20 de octubre de 2026. No se alteraron.
