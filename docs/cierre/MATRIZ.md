# Matriz de decisión — ¿monolito modular, microservicios o híbrida?

**RICA · Lección 13.** Tres alternativas reales, seis criterios con peso justificado, y
análisis de sensibilidad sobre el criterio más importante.

---

## Criterios y pesos

Los pesos salen de los atributos de calidad priorizados en `ARQUITECTURA.md` §1.

| # | Criterio | Peso | Justificación del peso |
|---|---|---:|---|
| 1 | **Disponibilidad de la consulta** | 5 | Es el valor del sistema: si la ficha no se lee, no sirve. El escenario medible exige que sobreviva a la caída de escritura. |
| 2 | **Consistencia del dato de baja** | 5 | Un investigador eliminado con publicaciones vivas corrompe la red de conocimiento; es un error visible y permanente. |
| 3 | **Facilidad de operar** (equipo pequeño) | 4 | Un equipo no puede sostener cinco procesos si cada uno exige operación propia. |
| 4 | **Modificabilidad del núcleo** | 4 | Cambiar de tecnología de integración sin tocar la lógica es lo que permite evolucionar. |
| 5 | **Escalado independiente** | 3 | Las publicaciones crecen más que los investigadores, pero no es la restricción principal hoy. |
| 6 | **Velocidad de entrega** | 3 | Importa para el curso; no domina sobre los atributos del negocio. |

Escala de puntuación: 1 (malo) a 5 (excelente).

---

## Matriz

| Criterio | Peso | A · Monolito modular | B · Microservicios completos | C · Híbrida (actual) |
|---|---:|---:|---:|---:|
| 1 · Disponibilidad de la consulta | 5 | 3 → 15 | 5 → 25 | 5 → 25 |
| 2 · Consistencia del dato de baja | 5 | 5 → 25 | 3 → 15 | 4 → 20 |
| 3 · Facilidad de operar | 4 | 5 → 20 | 2 → 8 | 3 → 12 |
| 4 · Modificabilidad del núcleo | 4 | 3 → 12 | 4 → 16 | 5 → 20 |
| 5 · Escalado independiente | 3 | 2 → 6 | 5 → 15 | 4 → 12 |
| 6 · Velocidad de entrega | 3 | 5 → 15 | 2 → 6 | 3 → 9 |
| **Total** | | **93** | **85** | **98** |

**Lectura:**

- **A** gana en operación y consistencia: una transacción local resuelve la baja sin Saga.
  Pierde en disponibilidad de la consulta, porque una caída del proceso tumba la lectura.
- **B** gana en aislamiento, pero paga consistencia (necesita Saga) y operación (más
  procesos, más bases, más fallos posibles). Es completa cuando hay equipos por servicio.
- **C** gana porque separa solo lo que necesita separarse: la lectura (que da
  disponibilidad) se separa de la escritura, y la baja se resuelve con Saga porque el dato
  vive en dos motores distintos. No distribuye lo que podía quedarse junto.

---

## Análisis de sensibilidad

Se mueve el peso del criterio más importante (±2) y se recalcula.

| Escenario | A · Monolito | B · Microservicios | C · Híbrida | Ganador |
|---|---:|---:|---:|---|
| Base (peso 5 en criterio 1) | 93 | 85 | 98 | **C** |
| Criterio 1 con peso **3** | 83 | 75 | 88 | **C** |
| Criterio 1 con peso **7** | 103 | 95 | 108 | **C** |
| Criterio 2 con peso **3** | 83 | 75 | 88 | **C** |
| Criterio 2 con peso **7** | 103 | 95 | 108 | **C** |

**Conclusión de sensibilidad:** el ganador **no cambia** en ninguno de los cuatro
ajustes. La decisión es **robusta**, no frágil: la híbrida no depende de que un peso
particular esté calibrado de una forma específica.

Prueba adicional, el caso extremo: si el criterio de operación (peso 4) subiera a 5 y el
de escalado (3) bajara a 1, el total de A sería 96 y el de C sería 94 — **el monolito
ganaría**. Es el escenario honesto que hay que reconocer: **si el equipo fuera de una
persona sin capacidad de operar cinco procesos, el monolito sería la decisión correcta.**
La híbrida se sostiene porque el sistema ya tiene el hábito de operación (compose, CI,
health checks) y porque el escenario de disponibilidad de la consulta es requisito del
negocio, no preferencia técnica.

---

## Decisión

> **Elegimos C (híbrida) porque resuelve los dos atributos que más pesan —disponibilidad
> de la lectura y consistencia de la baja— sin pagar el costo operativo de distribuir los
> tres contextos, aceptando el costo de mantener un servicio de lectura con consistencia
> eventual, una Saga sin compensación automática y una tabla de outbox que hay que
> purgar.**

El ganador **coincide** con la arquitectura que está construida. No se oculta el caso en
que el monolito ganaría: está en el análisis de sensibilidad.
