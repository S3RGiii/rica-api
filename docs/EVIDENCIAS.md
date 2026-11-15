# Evidencias de los talleres L5/L6/L8

**Fecha de las ejecuciones:** 2026-11-03
**Rama:** `taller-l8-cqrs` (L8 sobre `taller-microservicios-lineal`)
**Base L5/L6:** `44da27b` (contenido) y `3bf5578` (README de ruta)
**Entorno:** Windows 11 · Java 25.0.4.1 · Maven 3.9.16 (wrapper)
**Nota:** las fechas de los commits son las originales; los hashes corresponden a la historia limpia actual.

---

## 1. Tests automáticos

| Servicio | Comando | Resultado |
|---|---|---:|
| `investigadores-service` | `.\mvnw.cmd test` | 8 tests ✅ |
| `publicaciones-service` | `.\mvnw.cmd test` | 4 tests ✅ |
| `consultas-service` | `.\mvnw.cmd test` | 7 tests ✅ |
| **Total** | | **19 tests ✅ · BUILD SUCCESS** |

**Clases:**

- `InvestigadorFactoryTest` — 3 tests.
- `InvestigadorServiceTest` — 5 tests (incluye `confirmarEliminacionBorraAlInvestigadorYPublicaElEventoEliminado`).
- `LimitePublicacionesAnualesServiceTest` — 2 tests.
- `PublicacionServiceTest` — 2 tests (evento publicado después de guardar; no se publica si el comando falla).
- `FichaInvestigadorTest` — 3 tests (total, idempotencia, ficha nueva).
- `ProyectorFichasTest` — 4 tests (registro, publicación antes del registro, duplicado, eliminación).

**Alcance:** son tests de dominio/aplicación. **No** cubren el adaptador gRPC, Kafka, MongoDB, Outbox, Saga ni la prueba end-to-end de L8; esas piezas se verifican manualmente.

---

## 2. L8 — CQRS

### Implementado

- `consultas-service` en el puerto **8083**, con MongoDB `rica_consultas` (misma instancia, base distinta de `publicaciones-service`).
- Consumo de `investigadores.registrados`, `investigadores.en-eliminacion`, `investigadores.eliminados` y `publicaciones.registradas`.
- Proyector idempotente y tolerante al desorden; ficha desnormalizada con publicaciones embebidas.
- Endpoints GET `/api/consultas/investigadores`, `/api/consultas/investigadores/{correo}` y `/api/consultas/ranking`.
- Ruta `consultas-route` en el Gateway (`lb://consultas-service`).
- `publicaciones-service` publica `publicaciones.registradas` después de guardar.
- `investigadores-service` publica `investigadores.eliminados` al confirmar la eliminación de la Saga.

### Verificación end-to-end: pendiente

La prueba completa del taller requiere Kafka, MongoDB, Eureka y los cuatro servicios arriba. **No se ejecutó en esta sesión**, así que no se reporta como evidencia. Los pasos quedan documentados para ejecutarla:

1. Levantar las bases y Kafka.
2. Arrancar `eureka-server`, `investigadores-service`, `publicaciones-service` y `consultas-service`.
3. Registrar un investigador y dos publicaciones.
4. Consultar `GET /api/consultas/investigadores/{correo}` y comprobar `totalPublicaciones: 2`.
5. Consultar el ranking y comprobar el orden descendente.
6. Detener `consultas-service`, registrar otra publicación, reiniciarlo y comprobar que el consumidor procesa el evento pendiente.
7. Eliminar al investigador y comprobar el estado intermedio `EN_ELIMINACION` y luego el 404.
8. Borrar `fichas_investigador`, cambiar el `group-id` y comprobar la reconstrucción.

### Lo que sí está verificado

- `.\mvnw.cmd test` en `consultas-service`: **7 tests verdes**.
- Compilación del Gateway con la ruta nueva: **BUILD SUCCESS**.
- Las pruebas de `publicaciones-service` e `investigadores-service` siguen verdes con los eventos nuevos.

---

## 3. Verificación gRPC

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

## 4. Verificación end-to-end L5/L6

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

## 5. L5 como paso previo

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

## 6. Infraestructura

- `investigadores-service/docker-compose.yml`: PostgreSQL 16 en 5432 y Kafka en 9092.
- `publicaciones-service/docker-compose.yml`: MongoDB 7 en 27017.
- Los dos compose publican puertos distintos, así que se pueden levantar juntos; ese es el escenario de L8.
- El repo no incluye un compose raíz de Kafka/Eureka/Gateway. Para gRPC bastan los dos servicios y sus bases; para Kafka/Outbox/Saga/Eureka/Gateway hay que levantar Kafka y los módulos aparte.
- `consultas-service` no tiene compose propio: usa el mismo MongoDB con la base `rica_consultas` y el mismo Kafka.
- `eureka-server` (8761) y `gateway` (8000) se ejecutan con `.\mvnw.cmd spring-boot:run`.

---

## 7. Lectura de la evidencia

- **L4:** `taller-hexagonal` `becb667` (código) y `1c7ab7f` (docs), 31 tests definidos (28 sin base + 3 de integración).
- **L5:** dos servicios, puerto `VerificadorInvestigador`, 9 tests verdes.
- **L6:** gRPC + Kafka/Outbox/Saga/Eureka/Gateway, 9 tests verdes; gRPC verificado manualmente.
- **L8:** `consultas-service` + proyector, 19 tests verdes en total, end-to-end pendiente de ejecutar.
- **L9:** no implementado en este repo.
- **Fechas:** commits del 2026-10-06 y 2026-10-20; no se alteraron.
