# RICA — Taller L5/L6: microservicios, gRPC y mensajería

Taller de **Ingeniería de Software II** (UPTC). Esta rama contiene la evolución de `rica-api` desde el monolito hexagonal (L4) hasta dos servicios independientes que se comunican por gRPC y mensajería (L5 + L6).

**Rama:** `taller-microservicios`
**Commit de referencia:** `3f69c37` (2026-10-20)
**Base L4:** `taller-hexagonal`, commit `9e2b3c8` (2026-10-06)
**Remoto:** <https://github.com/S3RGiii/rica-api>

> Las fechas son las reales de los commits. No se reescribieron. Este README se agregó el **2026-11-03** para dejar la evidencia visible.

## Módulos y puertos

| Módulo | Puerto | Rol |
|---|---:|---|
| `investigadores-service` | 8080 HTTP · 9090 gRPC | Servidor gRPC `ExisteInvestigador`; PostgreSQL + Flyway |
| `publicaciones-service` | 8081 HTTP | Cliente gRPC; MongoDB; consumidor Kafka |
| `eureka-server` | 8761 | Registro de servicios |
| `gateway` | 8000 | Punto único de entrada; rutas `lb://` |

## Qué se implementó

- **L5:** separación en `investigadores-service` y `publicaciones-service`, cada uno con su base; `PublicacionService` depende del puerto `VerificadorInvestigador`, no de `InvestigadorRepository`.
- **L6 — gRPC:** `investigador.proto` con `ExisteInvestigador`; servidor `InvestigadorGrpcService`; cliente `VerificadorInvestigadorGrpc` con `InvestigadorGrpcBlockingStub`, deadline de 5 s y Resilience4j Circuit Breaker.
- **L6 — Kafka/Outbox:** `investigadores.registrados`, `investigadores.en-eliminacion`, `publicaciones.marcadas-huerfanas`; Transactional Outbox en investigadores.
- **L6 — Saga:** eliminación coreografiada de investigador con publicaciones huérfanas.
- **L6 — Eureka/Gateway:** registro y rutas por nombre lógico.
- **L6 — Resiliencia:** `ServicioNoDisponibleException` → HTTP 503 cuando el servidor gRPC no responde.

## Evidencia de pruebas

**Verificado el 2026-11-03:**

| Servicio | Comando | Resultado |
|---|---|---:|
| `investigadores-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| `publicaciones-service` | `.\mvnw.cmd test` | 2 tests ✅ |
| **Total** | | **9 tests ✅ · BUILD SUCCESS** |

- **Bruno:** 8 requests, incluida `4-eliminar-investigador` (Saga).
- **gRPC:** no tiene test automático; se verifica con `grpcurl` y Bruno.
- **L5 local:** el mismo par de servicios sin gRPC dio 9 tests verdes; el detalle está en `docs/EVIDENCIAS.md`.

## Cómo correr el camino gRPC

### 1. Bases

Cada servicio trae su compose; **no levantes los dos a la vez** porque ambos publican 5432/27017.

```powershell
cd investigadores-service
docker compose up -d

cd ..\publicaciones-service
docker compose up -d
```

Si ya tienes un PostgreSQL/MongoDB en 5432/27017, reutilízalos y no levantes otro.

### 2. Servicios

```powershell
cd investigadores-service
.\mvnw.cmd spring-boot:run

cd ..\publicaciones-service
.\mvnw.cmd spring-boot:run
```

El servidor gRPC queda en `localhost:9090`; la API de publicaciones en `localhost:8081`.

### 3. Prueba gRPC

```powershell
docker run --rm fullstorydev/grpcurl -plaintext host.docker.internal:9090 list

$json = '{"correoInstitucional":"ana@uptc.edu.co"}'
$json | docker run --rm -i fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 InvestigadorGrpc/ExisteInvestigador
```

**Esperado:** `{"existe": true}` para un investigador registrado; `{}`/`false` para uno inexistente.

### 4. Prueba end-to-end

```powershell
curl.exe -X POST http://localhost:8081/api/publicaciones `
  -H "Content-Type: application/json" `
  -d "{\"investigadorCorreo\":\"ana@uptc.edu.co\",\"titulo\":\"Arquitectura\",\"anio\":2026}"
```

**Esperado:** 201 si el investigador existe; 404 si no; 503 si el servidor gRPC está caído.

## L5 → L6 en una frase

`PublicacionService` no cambia: sigue dependiendo de `VerificadorInvestigador`. En L5 el adaptador es `VerificadorInvestigadorPendiente` (devuelve `true`); en L6 es `VerificadorInvestigadorGrpc`. Ese es el valor del puerto.

## Infraestructura pendiente

El repo no incluye un compose raíz con Kafka/Eureka/Gateway; solo PostgreSQL y MongoDB por servicio. Para el camino gRPC bastan los dos servicios y sus bases. Para Kafka/Outbox/Saga/Eureka/Gateway hay que levantar Kafka y los módulos correspondientes aparte.

## Stack

- Spring Boot 4.1.1 · Java 25 · Spring Cloud 2025.1.3.
- Spring gRPC server/client + Protocol Buffers.
- Kafka · Resilience4j · Eureka · Gateway.
- PostgreSQL 16 + Flyway · MongoDB 7.

## Documentación

- `docs/L5.md` — paso L5: dos servicios, puerto `VerificadorInvestigador` y adaptador pendiente.
- `docs/EVIDENCIAS.md` — resultados, comandos y alcance de la verificación.
- Rama L4: <https://github.com/S3RGiii/rica-api/tree/taller-hexagonal>
- Teoría del curso: `Material/Teoria_Ingenieria_de_Software_II.md` (L1–L9), fuera del repo.
