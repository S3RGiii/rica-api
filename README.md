# RICA — Talleres L5/L6/L8: microservicios, gRPC, mensajería y CQRS

Taller de **Ingeniería de Software II** (UPTC). Esta rama contiene la evolución de `rica-api` desde el monolito hexagonal (L4) hasta dos servicios que se comunican por gRPC y mensajería (L5 + L6), más el lado de lectura CQRS de la Lección 8.

**Rama:** `main` (L5, L6, L7 y L8 integrados)
**Contenido original L5/L6:** commit `ffd390a` (2026-10-20)
**Base L4:** `taller-hexagonal` · código `becb667`, docs `1c7ab7f`
**Remoto:** <https://github.com/S3RGiii/rica-api>

> Las fechas son las reales de los commits. Los hashes citados existen en este repositorio y se pueden verificar con `git cat-file -t <hash>`.

## Ramas (no confundir)

| Rama | Qué es | Uso |
|---|---|---|
| `taller-hexagonal` | L4: monolito hexagonal | Base histórica |
| `taller-microservicios` | L5/L6 **original**, con historia separada | Referencia histórica |
| `taller-microservicios-lineal` | L5/L6 sobre la base L4, historia lineal | Integrada a `main` |
| `taller-l8-cqrs` | L8: `consultas-service` | Integrada a `main` |

Todo está integrado en `main`. Las ramas se conservan como historia del taller; no hay trabajo pendiente en ninguna de ellas.

## Ruta del curso (L1–L9)

| Lección | Estado | Dónde |
|---|---|---|
| L1 · Fundamentos | Teoría | `Material/Teoria_Ingenieria_de_Software_II.md` |
| L2 · Estilos clásicos | Teoría | `Material/Teoria_Ingenieria_de_Software_II.md` |
| L3 · DDD | Hecho | Rama `taller-ddd` |
| L4 · Hexagonal | Hecho | Rama `taller-hexagonal` |
| L5 · Microservicios | Hecho | `investigadores-service`, `publicaciones-service` |
| L6 · Comunicación | Hecho | gRPC, Kafka, Outbox, Saga, Resilience4j, Eureka y Gateway |
| L7 · ATAM y evolutionary architecture | Hecho (documentación) | `docs/ATAM-LITE.md`, `docs/adr/` y `ArquitecturaTest` en cada servicio |
| L8 · CQRS | Hecho | `consultas-service` + proyector de eventos |
| L9 · EDA | Hecho | DLT + backoff en `KafkaErrorHandlingConfig`; ver `docs/L9-L11.md` |
| L10 · Observabilidad | Hecho | Actuator, `CorrelationIdFilter` y correlationId dentro de los eventos |
| L11 · Contenedores y CI | Hecho | Dockerfile multi-etapa, `docker-compose.yml` y matriz en GitHub Actions |

> El repositorio contiene **código hasta L11**. La entrega y la verificación están en [`docs/ENTREGA.md`](docs/ENTREGA.md).

## Módulos y puertos

| Módulo | Puerto | Rol |
|---|---:|---|
| `investigadores-service` | 8080 HTTP · 9090 gRPC | Servidor gRPC `ExisteInvestigador`; PostgreSQL + Flyway; Outbox |
| `publicaciones-service` | 8081 HTTP | Cliente gRPC; MongoDB; consumidor Kafka; publica `publicaciones.registradas` |
| `consultas-service` | 8083 HTTP | **Lado de lectura CQRS**; MongoDB `rica_consultas`; consume 4 eventos; DLT |
| `eureka-server` | 8761 | Registro de servicios |
| `gateway` | 8000 | Punto único de entrada; rutas `lb://` |

## Qué se implementó

- **L5:** separación en `investigadores-service` y `publicaciones-service`, cada uno con su base; `PublicacionService` depende del puerto `VerificadorInvestigador`, no de `InvestigadorRepository`.
- **L6 — gRPC:** `investigador.proto` con `ExisteInvestigador`; servidor `InvestigadorGrpcService`; cliente `VerificadorInvestigadorGrpc` con `InvestigadorGrpcBlockingStub`, deadline de 5 s y Resilience4j Circuit Breaker.
- **L6 — Kafka/Outbox:** `investigadores.registrados`, `investigadores.en-eliminacion`, `investigadores.eliminados`, `publicaciones.marcadas-huerfanas`; Transactional Outbox en investigadores.
- **L6 — Saga:** eliminación coreografiada de investigador con publicaciones huérfanas.
- **L6 — Eureka/Gateway:** registro y rutas por nombre lógico.
- **L6 — Resiliencia:** `ServicioNoDisponibleException` → HTTP 503 cuando el servidor gRPC no responde.
- **L8 — CQRS:** `consultas-service` mantiene una ficha desnormalizada por investigador en `rica_consultas`/`fichas_investigador`; escucha `investigadores.registrados`, `investigadores.en-eliminacion`, `investigadores.eliminados` y `publicaciones.registradas`.
- **L8 — proyector idempotente:** una publicación repetida no se duplica; una publicación que llega antes del registro crea la ficha sin nombre y el nombre se completa después.
- **L8 — solo lectura:** `GET /api/consultas/investigadores`, `GET /api/consultas/investigadores/{correo}` y `GET /api/consultas/ranking?top=3`. No hay POST/PUT/DELETE.
- **L8 — escritura intacta si cae la lectura:** Kafka conserva los eventos; al reiniciar `consultas-service` el consumidor se pone al día. Es consistencia eventual.
- **L8 — reconstrucción:** se puede borrar la colección `fichas_investigador`, cambiar el `group-id` y reproducir los eventos; el modelo de lectura no es la fuente de verdad.

## Evidencia de pruebas

**Verificado el 2026-11-15:**

| Servicio | Comando | Resultado |
|---|---|---:|
| `investigadores-service` | `.\mvnw.cmd test` | 14 tests ✅ |
| `publicaciones-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| `consultas-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| **Total** | | **28 tests ✅ · BUILD SUCCESS** |

- **Bruno:** 8 requests del camino L5/L6, incluida `4-eliminar-investigador` (Saga).
- **gRPC:** no tiene test automático; se verifica con `grpcurl` y Bruno.
- **Alcance:** los 28 tests son de dominio, aplicación y funciones de aptitud. **No** cubren Kafka, MongoDB, Outbox, Saga, gRPC ni la prueba end-to-end de L8; esas piezas se verifican manualmente con la infraestructura levantada.
- **Informe de verificación:** [`docs/ENTREGA.md`](docs/ENTREGA.md) y [`docs/ENTREGA-informe-talleres.md`](docs/ENTREGA-informe-talleres.md) (60/60 + 10/10 de bonus).

## Cómo correr el camino L5/L6

### 1. Bases

Cada servicio trae su compose y publican puertos distintos: `investigadores-service` levanta PostgreSQL en 5432 y Kafka en 9092; `publicaciones-service` levanta MongoDB en 27017. Puedes levantar ambos.

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

## Cómo correr L8

`consultas-service` reutiliza la misma instancia de MongoDB, pero con la base `rica_consultas`. No comparte base con `publicaciones-service` (`rica`).

```powershell
cd consultas-service
.\mvnw.cmd spring-boot:run
```

El servicio queda en `localhost:8083` y se registra en Eureka. Necesita Kafka y MongoDB arriba; el `group-id` inicial es `consultas-service-v2`.

### Consultas

```powershell
# Ficha completa de un investigador (investigador + publicaciones)
curl.exe http://localhost:8083/api/consultas/investigadores/ana@uptc.edu.co

# Fichas en estado intermedio de la Saga
curl.exe "http://localhost:8083/api/consultas/investigadores?estado=EN_ELIMINACION"

# Ranking por número de publicaciones
curl.exe "http://localhost:8083/api/consultas/ranking?top=3"
```

### Reconstruir el modelo de lectura

```powershell
# 1. Detener consultas-service y borrar la colección
docker exec -it publicaciones-mongo mongosh rica_consultas --eval "db.fichas_investigador.drop()"

# 2. Cambiar el group-id en consultas-service/src/main/resources/application.yml
#    (por ejemplo, consultas-service-v3) y arrancar de nuevo.
```

Kafka recuerda el offset por grupo; un grupo nuevo, con `auto-offset-reset: earliest`, reproduce los eventos desde el principio. Las publicaciones anteriores a L8 no se pueden reconstruir porque `publicaciones-service` no publicaba eventos antes de este cambio.

## L8 en una frase

El lado de escritura no cambia: cada comando exitoso anuncia un hecho. `consultas-service` traduce esos hechos a una ficha desnormalizada; una consulta devuelve investigador y publicaciones sin llamar a otro servicio, y si el lado de lectura cae, Kafka guarda los eventos para cuando vuelva.

## Infraestructura

El repo no incluye un compose raíz con Kafka/Eureka/Gateway; solo PostgreSQL y MongoDB por servicio. Para el camino gRPC bastan los dos servicios y sus bases. Para Kafka/Outbox/Saga/Eureka/Gateway hay que levantar Kafka y los módulos correspondientes aparte. `consultas-service` no tiene compose propio: usa el MongoDB existente con la base `rica_consultas`.

## Stack

- Spring Boot 4.1.1 · Java 25 · Spring Cloud 2025.1.3.
- Spring gRPC server/client + Protocol Buffers.
- Kafka · Resilience4j · Eureka · Gateway.
- PostgreSQL 16 + Flyway · MongoDB 7.
- CQRS: `consultas-service` con `spring-boot-starter-data-mongodb` y `spring-boot-starter-kafka`.

## Documentación

- `docs/L5.md` — paso L5: dos servicios, puerto `VerificadorInvestigador` y adaptador pendiente.
- `docs/L8.md` — L8: modelo de lectura, proyector, eventos, consultas y reconstrucción.
- `docs/EVIDENCIAS.md` — resultados, comandos y alcance de la verificación.
- Rama L4: <https://github.com/S3RGiii/rica-api/tree/taller-hexagonal>
- Teoría del curso: `Material/Teoria_Ingenieria_de_Software_II.md` (L1–L9), fuera del repo.
