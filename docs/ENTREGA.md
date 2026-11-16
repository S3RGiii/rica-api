# Entrega de talleres

**Ingeniería de Software II · Talleres individuales · Corte 15 de noviembre de 2026**

---

## Texto para Moodle

```
TALLERES (individual)
Repositorio: https://github.com/S3RGiii/rica-api
Commit del corte: <pegar aqui la salida de: git rev-parse HEAD>
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

> **Por qué el hash va en blanco:** ningún archivo del repositorio puede contener
> el hash del commit que lo incluye — al commitearlo, el hash cambia. El hash del
> corte se obtiene **después** del último `git push`, con `git rev-parse HEAD`, y
> es el que se pega en Moodle. Ese mismo hash es el que hay que usar para
> `git checkout` al verificar.

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

Sin avisos en el informe: no hay cambios sin confirmar y el historial no está
concentrado en las últimas 48 horas.

El informe completo está en [`docs/ENTREGA-informe-talleres.md`](ENTREGA-informe-talleres.md).

> **Cómo verificar y obtener la huella del commit del corte:**
>
> ```
> git rev-parse HEAD
> git clone https://github.com/S3RGiii/rica-api.git evaluacion
> cd evaluacion
> git checkout <hash obtenido arriba>
> java VerificarTalleres.java
> ```
>
> La huella se calcula sobre ese commit. Dos evaluadores del mismo commit deben
> obtener la misma huella. Si difieren, casi siempre es porque uno no está en el
> commit del corte o el clon tiene cambios sin confirmar.

---

## Evidencias de ejecución (parte manual)

Las capturas de los talleres 5 a 11 están en [`docs/capturas/`](capturas/), con un
`README.md` que mapea cada imagen al código de `main`.

| Criterio manual | Evidencia |
|---|---|
| **MT1** · talleres 5 y 6 | 11 capturas: servicios corriendo, adaptador pendiente, gRPC con deadline, Saga por Kafka, 503 del circuit breaker |
| **MT2** · talleres 8 y 9 | Ficha JSON con `totalPublicaciones: 5`, log del proyector, estadísticas **3 → 4**, log del contador, DLT con `{esto no es json}` |
| **MT3** · respuestas propias | [`docs/RESPUESTAS-L8-L9.md`](RESPUESTAS-L8-L9.md) — citan los valores de la ejecución |
| **MT4** · historial Git | Un commit por bloque de taller, en orden cronológico |

**Nombres verificables con `grep` dentro de `main`:**

- MT1: `VerificadorInvestigadorGrpc`, `investigadores.en-eliminacion`,
  `investigadores-service` / `publicaciones-service`
- MT2: `FichaInvestigador`, `ProyectorFichas`, `investigadores.registrados.DLT`,
  `/api/consultas/estadisticas`

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
