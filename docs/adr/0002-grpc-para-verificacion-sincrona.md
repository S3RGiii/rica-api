# 2. gRPC para la verificación síncrona de investigador

Estado: Aceptado

## Contexto

`publicaciones-service` no puede registrar una publicación si el investigador no existe. Es una pregunta que necesita respuesta **antes** de escribir: no se puede diferir a un evento, porque el usuario está esperando el resultado del `POST`.

En L5 esa pregunta la respondía `VerificadorInvestigadorPendiente`, un adaptador que devolvía `true` siempre. Era un marcador visible, no una solución: el caso de uso "correo inexistente" no funcionaba. L6 tiene que reemplazarlo por una llamada real entre procesos.

El puerto ya estaba definido y `PublicacionService` ya dependía de él, no de una tecnología. Eso deja la decisión abierta: la pregunta es **cómo** se hace la llamada, no **dónde** encaja.

## Decisión

gRPC con Protocol Buffers sobre HTTP/2:

- `investigador.proto` define `InvestigadorGrpc.ExisteInvestigador(CorreoRequest) returns (ExisteResponse)`.
- `investigadores-service` expone el servidor en el puerto 9090 (`InvestigadorGrpcService`).
- `publicaciones-service` consume con `InvestigadorGrpcBlockingStub`, `withDeadlineAfter(5, SECONDS)` y un Circuit Breaker de Resilience4j envolviendo la llamada.

El contrato es un `.proto` versionado, y el adaptador `VerificadorInvestigadorGrpc` es el único punto del servicio que conoce gRPC.

## Alternativas descartadas

**REST con JSON.** Era la opción por defecto y se descartó por dos razones medibles: el contrato no es verificable en tiempo de compilación —un cambio de nombre en un campo JSON solo falla en ejecución— y el payload de una pregunta booleana viaja con texto redundante. gRPC da el cliente tipado y genera el código a partir del `.proto`.

**Consultar la base de investigadores directamente.** Es la vía más rápida de implementar y la que rompe el ADR-001: acopla los esquemas y hace que un cambio de tabla rompa el otro servicio. Se descartó sin discusión.

**Evento asíncrono con respuesta diferida.** Se descartó porque el caso de uso es una validación previa, no una propagación de un hecho. Aceptar la publicación y rechazarla después obliga a un estado intermedio visible ("pendiente de validación") que el negocio no pidió y que complica la interfaz.

## Consecuencias

(+) **Contrato tipado y verificado en compilación.** Si el `.proto` cambia de forma incompatible, el build del cliente falla antes de desplegar.

(+) **Latencia menor** que REST/JSON para el mismo payload, por serialización binaria y HTTP/2.

(+) **`PublicacionService` no cambió entre L5 y L6.** El puerto cumplió su función: el núcleo no supo que la tecnología cambió.

(+) **La degradación está acotada.** Circuit Breaker (ventana 10, mínimo 5 llamadas, umbral 50 %, 10 s abierto) + deadline de 5 s evitan que una caída del servidor se propague como espera indefinida.

(−) **El Circuit Breaker está construido a mano en el adaptador**, con `CircuitBreaker.of(...)` en el constructor, en vez de usar la anotación de Resilience4j. Eso lo hace difícil de configurar por propiedad y de reutilizar; es deuda técnica reconocida, no una decisión de diseño.

(−) **gRPC no se prueba automáticamente.** Los 9 tests existentes son de dominio y aplicación; el adaptador se verifica a mano con `grpcurl` y Bruno. Un fallo de integración no lo detecta el build.

(−) **Un `.proto` mal versionado rompe el cliente.** gRPC castiga más que REST los cambios incompatibles: no hay margen para "ignorar el campo que sobra".

(−) **Requiere abrir un puerto adicional** (9090) y gestionar su ciclo de vida aparte del HTTP (8080).

(−) **El adaptador traduce `StatusRuntimeException` y `CallNotPermittedException` a `ServicioNoDisponibleException` → HTTP 503.** Es correcto, pero significa que el cliente HTTP de publicaciones recibe un 503 genérico y no distingue "servidor caído" de "circuito abierto".
