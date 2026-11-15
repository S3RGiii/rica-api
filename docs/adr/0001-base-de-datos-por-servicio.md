# 1. Una base de datos por servicio

Estado: Aceptado

## Contexto

En L4 `rica-api` era un monolito hexagonal con una sola fuente de datos y dos agregados —investigadores y publicaciones— que se consultaban entre sí en memoria. Al separar el sistema en dos servicios desplegables (L5) aparece una decisión que no estaba en el código: qué pasa con los datos.

`investigadores-service` necesita garantías transaccionales para el límite anual de publicaciones y las reglas de unicidad del correo institucional. `publicaciones-service` necesita guardar documentos con estructura variable y consultarlos por autor y por año. Son dos formas de acceso a datos distintas sobre dos agregados distintos.

## Decisión

Cada servicio es dueño exclusivo de su base de datos:

| Servicio | Motor | Justificación |
|---|---|---|
| `investigadores-service` | PostgreSQL 16 + Flyway | Invariantes relacionales: correo único, límite anual por año, estado del investigador |
| `publicaciones-service` | MongoDB 7 | Documentos de forma variable; consulta por `investigadorCorreo` y por año |

Ningún servicio abre una conexión a la base del otro. Si `publicaciones-service` necesita saber si un investigador existe, lo pregunta por el contrato `VerificadorInvestigador`, no por SQL.

## Alternativas descartadas

**Base compartida entre ambos servicios.** Es la opción que menos trabajo cuesta al principio y la que se descartó por una razón concreta: convierte dos servicios en un monolito distribuido. Un cambio de esquema en `investigadores` rompería a `publicaciones` sin que su build se entere, y los despliegues dejarían de ser independientes porque el esquema es compartido. La regla de la Lección 5 es explícita en esto.

**Una sola base poliglota con dos esquemas.** Habría dado independencia lógica sin independencia física, pero el acoplamiento operativo se mantiene: una migración fallida en un esquema bloquea el otro.

## Consecuencias

(+) El esquema de un servicio evoluciona sin coordinar con el otro.

(+) El fallo de una base no tumba al otro servicio: `publicaciones-service` puede seguir listando publicaciones de un investigador aunque PostgreSQL esté caído.

(+) Cada servicio usa el motor que le conviene, no el que le tocó.

(−) **No hay integridad referencial entre servicios.** La base de publicaciones acepta un `investigadorCorreo` que puede no existir; la única barrera es la verificación en el caso de uso. Es una integridad *eventual*, no garantizada por el motor.

(−) **No hay transacción distribuida.** Cualquier operación que toque ambos agregados exige Saga y compensación (ver ADR-003).

(−) **Consultar datos combinados obliga a dos llamadas.** La ficha de un investigador con sus publicaciones no se resuelve con un `JOIN`; es el problema que motiva CQRS en el ADR-004.

(−) **Dos respaldos, dos planes de recuperación, dos ventanas de mantenimiento.** El costo operativo se duplica.
