# Entrega de talleres

**Ingeniería de Software II · Talleres individuales · Corte 15 de noviembre de 2026**

---

## Texto para Moodle

```
TALLERES (individual)
Repositorio: https://github.com/S3RGiii/rica-api
Commit del corte: 1261dd5f6e953a0d1fe6fd729b83f27a664ffdcf
Rama o PR por taller:
  Taller 3 (DDD)          -> taller-ddd
  Taller 4 (hexagonal)    -> taller-hexagonal
  Taller 5 (microservicios) -> taller-microservicios-lineal
  Taller 6 (comunicación) -> taller-microservicios-lineal
  Taller 8 (CQRS)         -> taller-l8-cqrs
  Taller 9 (EDA)          -> main
  Taller 10 (observabilidad) -> main
  Taller 11 (contenedores y CI) -> main
```

> Los talleres 3 a 8 se conservan como ramas históricas; su contenido está
> integrado en `main`, que es lo que se evalúa. Los talleres 9, 10 y 11 se
> hicieron directamente sobre `main`.

---

## Resultado de la autoevaluación

Ejecutado con `VerificarTalleres.java` sobre un **clon limpio** del commit del corte:

| Bloque | Puntaje |
|---|---:|
| Talleres 3 a 9 (parte automática) | **60 / 60** |
| Talleres 10 y 11 (bonus) | **10 / 10** |
| **Huella del informe** | `0048de9cc48a` |

Sin avisos en el informe: no hay cambios sin confirmar y el historial no está
concentrado en las últimas 48 horas.

El informe completo está en [`docs/ENTREGA-informe-talleres.md`](ENTREGA-informe-talleres.md).

> **Nota sobre la huella:** el informe de ese archivo se generó sobre el commit
> `1261dd5`. Como este documento se agregó después, el hash del commit que se
> entrega es el que aparece arriba y la huella se recalcula sobre él. Para
> reproducirla: clonar el repositorio, hacer `git checkout <hash>` y ejecutar
> `java VerificarTalleres.java`.

---

## Qué hay en el repositorio

| Lección | Entregable | Dónde |
|---|---|---|
| L3 · DDD | Agregado, value object, factory y pruebas | `investigadores-service/.../dominio/`, `CorreoInstitucionalTest` |
| L4 · Hexagonal | Puertos y adaptadores separados | `dominio/puertos/`, `infraestructura/` |
| L5 · Microservicios | Auditoría de la descomposición | [`NOTAS.md`](../NOTAS.md) |
| L6 · Comunicación | gRPC, Kafka, Outbox, Saga, Eureka, Gateway | `investigadores-service`, `publicaciones-service` |
| L7 · ATAM | Árbol de utilidad, ADRs y funciones de aptitud | [`ATAM-LITE.md`](ATAM-LITE.md), [`adr/`](adr/), `ArquitecturaTest` |
| L8 · CQRS | Modelo de lectura y proyector idempotente | `consultas-service` |
| L9 · EDA | DLT, backoff e idempotencia | [`L9-L11.md`](L9-L11.md), `KafkaErrorHandlingConfig` |
| L10 · Observabilidad | Actuator, correlationId en logs y en eventos | `CorrelationIdFilter`, `OutboxPublicadorEventos` |
| L11 · Contenedores y CI | Dockerfile multi-etapa, compose y matriz | `Dockerfile`, `docker-compose.yml`, `.github/workflows/ci.yml` |

---

## Verificación

```
28 tests verdes
  14 en investigadores-service
   7 en publicaciones-service
   7 en consultas-service

6 funciones de aptitud con ArchUnit (se ejecutan en cada build)
```

Para levantar todo:

```bash
docker compose up -d postgres mongodb kafka eureka   # infraestructura
docker compose --profile app up -d --build           # los cinco módulos
```

O con `mvnw` desde cada módulo, con la infraestructura levantada.
