# NOTAS — auditoría del paso de monolito a microservicios

**Taller 5 · Descomposición en servicios.** Este documento deja constancia de cómo se
partió el monolito `rica-api` en servicios independientes y qué se aprendió al hacerlo.

## Qué se descompuso y por qué

El monolito original tenía dos agregados que se consultaban entre sí en memoria:
`Investigador` y `Publicacion`. La pregunta que decidió el corte fue: **¿qué cambia
junto y qué puede desplegarse por separado?** El investigador y sus publicaciones
tienen ciclos de vida distintos —un investigador se registra una vez y vive años; las
publicaciones se crean todo el tiempo— y necesitan motores de datos distintos. Ese es
el límite del servicio, no el número de clases.

| Servicio | Puerto | Base | Responsabilidad |
|---|---:|---|---|
| `investigadores-service` | 8080 · gRPC 9090 | PostgreSQL `rica` | Registro, consulta y límite anual de publicaciones |
| `publicaciones-service` | 8081 | MongoDB `rica` | Registro y consulta de publicaciones |
| `consultas-service` | 8083 | MongoDB `rica_consultas` | Modelo de lectura (L8) |

Cada uno tiene su `pom.xml`, su `application.yml` y su base. No comparten proceso ni
esquema.

## El puerto que rompió el acoplamiento

En el monolito, `PublicacionService` consultaba el repositorio de investigadores
directamente. Al separar, se declaró un puerto local:

```java
public interface VerificadorInvestigador {
    boolean existe(String correoInstitucional);
}
```

`PublicacionService` pasó a depender de esa interfaz. La consecuencia verificable es
que **el núcleo de publicaciones ya no importa nada de investigadores**: ni JPA, ni
sus entidades. Esto está protegido por una prueba de arquitectura con ArchUnit que
falla el build si alguien vuelve a acoplar los paquetes.

## El adaptador deliberadamente incompleto

Durante el taller 5 el puerto se implementó con `VerificadorInvestigadorPendiente`,
que devolvía `true` siempre. No era una solución: era un marcador visible que permitía
arrancar el servicio y probar el resto del flujo mientras el contrato de red no
existía. En el taller 6 se reemplazó por `VerificadorInvestigadorGrpc`, con deadline de
5 segundos y Circuit Breaker.

El punto pedagógico: **`PublicacionService` no cambió entre L5 y L6**. Cambió solo el
adaptador. Eso es lo que demuestra que el puerto servía.

## Qué costó y qué se aprendió

- **Lo más difícil no fue el código, fue el dato.** Separar servicios obliga a decidir
  qué pasa con la integridad que antes garantizaba la base. Aquí no hay FK entre
  servicios: la validez de un correo en publicaciones depende de una llamada de red.
- **La descomposición no es gratis.** Se cambiaron nueve tests que antes corrían en
  memoria por un contrato de red. La cobertura del dominio siguió igual, pero lo que
  antes era imposible de romper ahora depende de que otro proceso esté arriba.
- **El límite correcto no es el número de servicios.** Se podría haber hecho un
  servicio por entidad y habría sido un error: el investigador y su límite anual de
  publicaciones son una sola transacción y deben seguir juntos.
- **El error que se evitó:** consultar la base del otro servicio "solo para esta
  consulta". Habría funcionado hoy y habría atado los dos esquemas para siempre.

## Evidencia

- `investigadores-service`: 14 tests verdes.
- `publicaciones-service`: 7 tests verdes.
- `consultas-service`: 7 tests verdes.
- Pruebas de arquitectura con ArchUnit en los tres servicios.
- Documentación de la decisión: `docs/adr/0001-base-de-datos-por-servicio.md`.
