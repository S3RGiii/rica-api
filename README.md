# RICA — Talleres L5/L6/L8: microservicios, gRPC, mensajería y CQRS

Taller de **Ingeniería de Software II** (UPTC). Esta rama contiene la evolución de `rica-api` desde el monolito hexagonal (L4) hasta dos servicios que se comunican por gRPC y mensajería (L5 + L6), más el lado de lectura CQRS de la Lección 8.

**Rama actual:** `taller-l8-cqrs` (L8 sobre `taller-microservicios-lineal`)
**Base L5/L6:** `taller-microservicios-lineal` · commits `44da27b` (contenido) y `3bf5578` (README de ruta)
**Base L4:** `taller-hexagonal` · código `becb667`, docs `1c7ab7f`
**Remoto:** <https://github.com/S3RGiii/rica-api>

> Las fechas son las reales de los commits. Los hashes corresponden a la historia limpia actual; el contenido y las fechas de los commits se conservaron.

## Ramas (no confundir)

| Rama | Qué es | Uso |
|---|---|---|
| `taller-hexagonal` | L4: monolito hexagonal | Base histórica |
| `taller-microservicios` | L5/L6 **original**, con historia separada | Referencia; **no** es la rama del PR lineal |
| `taller-microservicios-lineal` | L5/L6 sobre la base L4, historia lineal | Es la que se integró a `main`; base de L8 |
| `taller-l8-cqrs` | L8: `consultas-service` sobre la lineal | **Esta rama** |

Si buscas la versión que se revisa para L5/L6, usa `taller-microservicios-lineal`. La rama `taller-microservicios` se conserva solo como historia original del taller.

## Ruta del curso (L1–L9)

| Lección | Estado | Dónde |
|---|---|---|
| L1 · Fundamentos | Teoría | `Material/Teoria_Ingenieria_de_Software_II.md` |
| L2 · Estilos clásicos | Teoría | `Material/Teoria_Ingenieria_de_Software_II.md` |
| L3 · DDD | Hecho | Rama `taller-ddd` |
| L4 · Hexagonal | Hecho | Rama `taller-hexagonal` |
| L5 · Microservicios | Hecho | `investigadores-service`, `publicaciones-service` |
| L6 · Comunicación | Hecho | gRPC, Kafka, Outbox, Saga, Resilience4j, Eureka y Gateway |
| L7 · ATAM y evolutionary architecture | Teoría | Guía local; no tiene taller de código |
| L8 · CQRS | **Hecho en esta rama** | `consultas-service` + proyector de eventos |
| L9 · EDA | **Pendiente** | Consumidores robustos, retry/backoff y DLT; no implementado en este repo |

> El repositorio contiene **código hasta L8 en esta rama**. L9 sigue siendo teoría; la guía interactiva local llega a L9, pero ese código no vive aquí.

## Módulos y puertos

| Módulo | Puerto | Rol |
|---|---:|---|
| `investigadores-service` | 8080 HTTP · 9090 gRPC | Servidor gRPC `ExisteInvestigador`; PostgreSQL + Flyway; Outbox |
| `publicaciones-service` | 8081 HTTP | Cliente gRPC; MongoDB; consumidor Kafka; publica `publicaciones.registradas` |
| `consultas-service` | 8083 HTTP | **Lado de lectura CQRS**; MongoDB `rica_consultas`; consume 4 eventos |
| `eureka-server` | 8761 | Registro de servicios |
| `gateway` | 8000 | Punto único de entrada; ruta `lb://consultas-service` |

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

**Verificado el 2026-11-03:**

| Servicio | Comando | Resultado |
|---|---|---:|
| `investigadores-service` | `.\mvnw.cmd test` | 8 tests ✅ |
| `publicaciones-service` | `.\mvnw.cmd test` | 4 tests ✅ |
| `consultas-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| **Total** | | **19 tests ✅ · BUILD SUCCESS** |

- **Bruno:** 8 requests del camino L5/L6, incluida `4-eliminar-investigador` (Saga).
- **gRPC:** no tiene test automático; se verifica con `grpcurl` y Bruno.
- **Alcance:** los 19 tests son de dominio/aplicación. **No** cubren Kafka, MongoDB, Outbox, Saga, gRPC ni la prueba end-to-end de L8; esas piezas se verifican manualmente con la infraestructura levantada.

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
