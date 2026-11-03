# RICA — Red de Investigación y Colaboración Académica

API del proyecto de clase de **Ingeniería de Software II** (UPTC). Implementa el dominio de investigadores y publicaciones con DDD, arquitectura hexagonal y persistencia políglota (PostgreSQL + MongoDB).

## Mapa de ramas y evidencia

| Rama | Lección | Qué contiene | Evidencia real |
|---|---|---|---|
| `main` | Integración | Versión integrada de los talleres | `c60509b` |
| `taller-ddd` | L3 · DDD | Lenguaje ubicuo, `CorreoInstitucional`, agregado `Investigador`, factory, servicio de límite anual y pruebas | `6200d64`, `66eb02a` (2026-10-06) |
| `taller-hexagonal` | L4 · Hexagonal | Puertos primarios en investigadores/publicaciones, adaptadores y pruebas | `9e2b3c8` (2026-10-06) |
| `taller-microservicios` | L5 + L6 | `investigadores-service` y `publicaciones-service`, gRPC, Kafka, Outbox, Saga, Resilience4j, Eureka y Gateway | `3f69c37` (2026-10-20) |

> Las fechas de la tabla son las fechas reales de los commits. No se reescribieron ni se alteraron. Este README se agregó el **2026-11-03** para dejar la evidencia visible en el repositorio.

La rama `taller-microservicios` está en el mismo remoto:
<https://github.com/S3RGiii/rica-api/tree/taller-microservicios>

## Cómo verificar

### 1. Infraestructura

```powershell
cd proyectos\Clases\rica-api
docker compose -f docker-compose.yaml up -d
```

Levanta PostgreSQL (`localhost:5432`) y MongoDB (`localhost:27017`) con la base `rica`.

### 2. Pruebas

```powershell
.\mvnw.cmd test
```

**Estado medido el 2026-11-03:**

- 31 tests definidos.
- **28/31 pasan sin infraestructura** (dominio, aplicación y controllers).
- Los 3 restantes son de integración (`InvestigadorRepositoryTest` ×2 y `RicaApiApplicationTests` ×1) y requieren PostgreSQL/MongoDB levantados. Sin la base, el error es `Connection to localhost:5432 refused`.
- Con `docker compose up -d`, vuelve a ejecutar `.\mvnw.cmd test` para completar los 31.

### 3. Aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

- API en `http://localhost:8080`.
- `GET /api/investigadores`
- `POST /api/investigadores`
- `GET /api/publicaciones?investigadorCorreo=...`
- `POST /api/publicaciones`

## Puente L4 → L5 → L6

El cambio central de L4 a L6 es una frontera:

1. **L4:** `PublicacionService` inyecta `InvestigadorRepository` y verifica el investigador contra la misma base.
2. **L5:** se separan `investigadores-service` y `publicaciones-service`. `PublicacionService` pasa a depender del puerto `VerificadorInvestigador`; el adaptador `VerificadorInvestigadorPendiente` devuelve `true` como marcador.
3. **L6:** el puerto se implementa con `VerificadorInvestigadorGrpc` y un `InvestigadorGrpcBlockingStub`; `investigadores-service` expone `ExisteInvestigador` por gRPC en el puerto 9090.

El núcleo de `PublicacionService` no cambia entre L5 y L6: solo cambia el adaptador. Esa es la prueba de que el puerto está bien diseñado.

## Evidencia de los talleres

- **L5:** 9 tests verdes (7 de investigadores + 2 de publicaciones), 7 requests Bruno. El detalle está en `docs/EVIDENCIAS.md`.
- **L6:** 9 tests verdes, 8 requests Bruno (incluye la Saga de eliminación). gRPC se prueba manualmente con `grpcurl` y Bruno; no hay test automático del adaptador.
- El repo L6 no trae un compose raíz de Kafka/Eureka/Gateway; solo PostgreSQL y MongoDB por servicio. Para el gRPC bastan los dos servicios y sus bases.

## Documentación

- `docs/EVIDENCIAS.md` — resultados de tests, comandos y alcance de cada rama.
- `Material/Teoria_Ingenieria_de_Software_II.md` — teoría del curso (L1–L9), fuera de este repositorio.
- `Proyecto-final/CAMINO-RICA-API-A-GRPC.md` — camino detallado L4 → gRPC, fuera de este repositorio.

## Stack

- Spring Boot 4.1.1 · Java 21 · Maven Wrapper.
- PostgreSQL 16 + Flyway (investigadores).
- MongoDB 7 (publicaciones).
- Springdoc OpenAPI.
- La rama `taller-microservicios` usa Java 25 y añade Spring gRPC, Kafka, Resilience4j, Eureka y Gateway.
