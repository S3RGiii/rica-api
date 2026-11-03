# Evidencias del taller L5/L6

**Fecha de las ejecuciones:** 2026-11-03
**Rama:** `taller-microservicios-lineal` (contenido original en `taller-microservicios`)
**Commit de referencia:** `3f69c37` (2026-10-20)
**Entorno:** Windows 11 · Java 25.0.4.1 · Maven 3.9.16 (wrapper)
**Nota:** las fechas de los commits son las originales; no se reescribieron.

---

## 1. Tests automáticos

| Servicio | Comando | Resultado |
|---|---|---:|
| `investigadores-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| `publicaciones-service` | `.\mvnw.cmd test` | 2 tests ✅ |
| **Total** | | **9 tests ✅ · BUILD SUCCESS** |

**Clases:**

- `InvestigadorFactoryTest` — 3 tests.
- `InvestigadorServiceTest` — 4 tests.
- `LimitePublicacionesAnualesServiceTest` — 2 tests.

**Alcance:** son tests de dominio/aplicación. **No** cubren el adaptador gRPC, Kafka, Outbox ni la Saga; esas piezas se verifican manualmente.

---

## 2. Verificación gRPC

**Servidor:** `investigadores-service`, puerto `9090`.
**Cliente:** `publicaciones-service`, `spring.grpc.client.channel.investigadores.target=static://localhost:9090`.

```powershell
# Listar servicios (requiere grpc-services/reflexión)
docker run --rm fullstorydev/grpcurl -plaintext host.docker.internal:9090 list

# Investigador existente
$json = '{"correoInstitucional":"ana@uptc.edu.co"}'
$json | docker run --rm -i fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 InvestigadorGrpc/ExisteInvestigador

# Investigador inexistente
$json = '{"correoInstitucional":"nadie@uptc.edu.co"}'
$json | docker run --rm -i fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 InvestigadorGrpc/ExisteInvestigador
```

**Esperado:**

- Existente: `{"existe": true}`.
- Inexistente: `{}` o `{"existe": false}` (en proto3 el `false` es el valor por defecto y puede no imprimirse).
- Servidor caído: `UNAVAILABLE`; el adaptador lo traduce a `ServicioNoDisponibleException` y el advice a **503**.

**Deadline:** el stub usa `withDeadlineAfter(5, TimeUnit.SECONDS)`.
**Circuit Breaker:** Resilience4j con ventana de 10, mínimo 5 llamadas, umbral 50 %, 10 s en abierto.

---

## 3. Verificación end-to-end

```powershell
# Correo existente → 201
curl.exe -X POST http://localhost:8081/api/publicaciones `
  -H "Content-Type: application/json" `
  -d "{\"investigadorCorreo\":\"ana@uptc.edu.co\",\"titulo\":\"Arquitectura\",\"anio\":2026}"

# Correo inexistente → 404
curl.exe -X POST http://localhost:8081/api/publicaciones `
  -H "Content-Type: application/json" `
  -d "{\"investigadorCorreo\":\"nadie@uptc.edu.co\",\"titulo\":\"X\",\"anio\":2026}"
```

**Esperado:** 201/404; 503 si el servidor gRPC no responde.

---

## 4. L5 como paso previo

El taller L5 local (`Talleres/Leccion-05-Microservicios/rica-microservicios`) dio **9 tests verdes** (7 + 2) y 7 requests Bruno. Su pieza clave es:

```java
@Component
public class VerificadorInvestigadorPendiente implements VerificadorInvestigador {
    @Override
    public boolean existe(String correoInstitucional) {
        return true; // TODO L6: reemplazar por gRPC
    }
}
```

En L6 ese adaptador se reemplaza por `VerificadorInvestigadorGrpc`; `PublicacionService` no cambia. Esa es la prueba de que el puerto separa el núcleo de la tecnología.

---

## 5. Infraestructura

- `investigadores-service/docker-compose.yml`: PostgreSQL 16 en 5432.
- `publicaciones-service/docker-compose.yml`: MongoDB 7 en 27017.
- **No levantes los dos compose a la vez**: ambos publican los mismos puertos. Usa un solo PostgreSQL y un solo MongoDB.
- El repo no incluye un compose raíz de Kafka/Eureka/Gateway. Para gRPC bastan los dos servicios y sus bases; para Kafka/Outbox/Saga/Eureka/Gateway hay que levantar Kafka y los módulos aparte.
- `eureka-server` (8761) y `gateway` (8000) se ejecutan con `.\mvnw.cmd spring-boot:run`.

---

## 6. Lectura de la evidencia

- **L4:** `taller-hexagonal` `9e2b3c8` (2026-10-06), 31 tests definidos (28 sin base + 3 de integración).
- **L5:** dos servicios, puerto `VerificadorInvestigador`, 9 tests verdes.
- **L6:** gRPC + Kafka/Outbox/Saga/Eureka/Gateway, 9 tests verdes; gRPC verificado manualmente.
- **Fechas:** commits del 2026-10-06 y 2026-10-20; no se alteraron.
