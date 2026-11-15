# Gobernanza de la arquitectura de RICA

**RICA · Lección 13.** Guardarraíles verificables, cómo se toman las decisiones, deuda
técnica y radar tecnológico.

---

## 1. Guardarraíles verificables

Un guardarraíl no es una recomendación: es una regla que **el build o el pipeline hace
cumplir**. Estos son los que existen hoy.

| # | Guardarraíl | Cómo se verifica | Qué pasa si se viola |
|---|---|---|---|
| **G1** | El dominio no depende de la infraestructura | `ArquitecturaTest` con ArchUnit en `investigadores-service` | El build falla (`mvnw test`) |
| **G2** | La capa de aplicación no depende de la infraestructura | `ArquitecturaTest` en `investigadores-service` | El build falla |
| **G3** | Los adaptadores de entrada solo hablan con aplicación o dominio | `ArquitecturaTest` en `investigadores-service` | El build falla |
| **G4** | `PublicacionService` no conoce su adaptador gRPC | `ArquitecturaTest` en `publicaciones-service` | El build falla |
| **G5** | Solo el adaptador habla gRPC y usa Resilience4j | `ArquitecturaTest` en `publicaciones-service` | El build falla |
| **G6** | Todos los módulos compilan y pasan sus pruebas | `.github/workflows/ci.yml`, matriz sobre 5 servicios | El check de CI queda en rojo |

**G6 es el guardarraíl de integración:** la matriz usa `fail-fast: false`, así que un
módulo roto no oculta el estado de los otros cuatro.

**Lo que falta:** ningún guardarraíl impide *duplicar* un tópico de Kafka en dos servicios,
ni verifica que un evento nuevo se registre en el ADR correspondiente. Son candidatos, no
implementados.

---

## 2. Cómo se toman las decisiones

**Modelo adoptado: foro consultivo, no junta de aprobación.** Cada decisión de
arquitectura se registra en un ADR y se discute con quien tenga contexto; no hay un comité
que autorice cada cambio.

| Tipo de decisión | Quién decide | Cómo queda registrada |
|---|---|---|
| Frontera entre servicios | Quien posee el contexto afectado | ADR con alternativa descartada |
| Tecnología de integración (REST, gRPC, Kafka) | Quien implementa, con revisión | ADR + justificación síncrona/asíncrona en `ARQUITECTURA.md` §3 |
| Estilo dentro de un servicio | El dueño del servicio | Convención del módulo; protegido por ArchUnit |
| Cambio de esquema de eventos | Productor y consumidores | Nota en el ADR del flujo; los tópicos van versionados por nombre |

**Regla de oro:** un ADR sin **alternativa descartada** y sin **consecuencia negativa** no
se acepta. Es la regla que hace que el registro sea honesto; los cuatro ADR de este
repositorio la cumplen.

**Ley de Conway aplicada:** el reparto de módulos refleja el reparto de responsabilidades.
En el taller individual hay una sola persona y el historial lo muestra; en el proyecto de
grupo del G5, los tres subdominios tienen dueño y rama propios.

---

## 3. Deuda técnica registrada

La deuda se registra para pagarla, no para olvidarla. Estas son las cinco principales,
con su disparador de pago.

| # | Deuda | Riesgo | Cuándo se paga |
|---|---|---|---|
| **D1** | La Saga no tiene compensación: un investigador puede quedar `EN_ELIMINACION` indefinidamente | Estado irrecuperable sin intervención manual | Antes de cualquier uso con datos reales. Disparador: el primer caso de baja que no confirme |
| **D2** | La tabla de outbox no se purga | Degradación del poller | Al superar 10 000 eventos publicados |
| **D3** | `publicaciones-service` publica sin Outbox (escritura dual) | Ficha desactualizada sin alerta | Al mover publicaciones a un esquema con garantía de entrega |
| **D4** | El consumidor de la Saga no es idempotente | Duplicado repetiría la confirmación | Al reutilizar el patrón para un efecto acumulativo |
| **D5** | No hay fitness function continua ni métricas de p95 | La arquitectura se degrada sin que nadie lo note | Al desplegar en un entorno con tráfico real |

**Regla de deuda:** cada elemento tiene un **disparador** concreto, no una fecha vaga. Una
deuda sin disparador es una deuda que no se paga.

---

## 4. Radar tecnológico

Cuatro anillos, con la decisión y su razón.

| Anillo | Tecnología | Razón |
|---|---|---|
| **Adoptar** | PostgreSQL, MongoDB, Kafka, Spring Boot, Flyway, ArchUnit, Docker Compose, GitHub Actions | En uso, verificados por tests y CI |
| **Adoptar** | gRPC + Protocol Buffers para contratos internos | En uso; el `.proto` da verificación en compilación |
| **Probar** | Testcontainers | Evolución natural del CI: probar el adaptador contra un Kafka real en vez de solo el dominio |
| **Probar** | OpenTelemetry | La correlación por cabecera y MDC cubre el caso actual; OTel sería el paso siguiente con más servicios |
| **Evaluar** | Redis como caché de la ficha | Solo si la latencia de `consultas-service` deja de cumplir el p95 objetivo |
| **Evitar** | Kubernetes en este alcance | Compose cubre cinco procesos en una máquina; K8s añade operación sin problema que resolver |
| **Evitar** | Service mesh (Istio, Linkerd) | Justificado con números en `ARQUITECTURA.md` §6 |
| **Evitar** | Event Sourcing | El estado actual es la fuente de verdad; el historial completo no es requisito |
| **Evitar** | Una base nueva sin ADR | Cada motor adicional duplica respaldos y operación |

---

## 5. Revisión de arquitectura

Se aplica **ATAM-lite** al cierre de cada hito, no solo al final. El resultado de la
última evaluación está en [`../ATAM-LITE.md`](../ATAM-LITE.md): cinco escenarios, seis
riesgos con su decisión, tres puntos de compromiso.

**Métricas de resultado (DORA), todavía no medidas en este proyecto:**

| Métrica | Estado |
|---|---|
| Frecuencia de despliegue | Medible con `git log` sobre `main` |
| Tiempo de entrega de un cambio | Medible: del commit a verde en CI |
| Tasa de fallos de cambio | No medida: falta registrar reversiones |
| Tiempo de recuperación | No medida: no hay entorno productivo |

Reconocer que dos de las cuatro no se pueden medir todavía es parte de la gobernanza: una
métrica que no se mide no se puede mejorar.
