# Informe de verificacion (talleres)

- Repositorio: rica-api
- Commit: aed75ae36f076b490126487a9556fc7550267d71
- Rama actual: main (fecha del commit: 2026-11-15T16:54:49-05:00)
- **Huella del informe: 39af9b9002c5**  (dos evaluadores del mismo commit deben obtener la misma huella)

## Talleres 3 a 9 (parte automatica)

| ID | Criterio | Puntos | Obtenido | Evidencia |
|---|---|---|---|---|
| T3a | Taller 3 (DDD): paquete de dominio con al menos 3 clases | 3 | 3 | 6 clases |
| T3b | Taller 3: existe una Factory | 3 | 3 | investigadores-service/src/main/java/com/rica/investigadoresservice/investigadores/aplicacion/InvestigadorFactory.java |
| T3c | Taller 3: hay un Value Object (record, @Embeddable o clase de valor) | 2 | 2 | consultas-service/src/main/java/com/rica/consultasservice/consultas/FichaResponse.java, consultas-service/src/main/java/com/rica/consultasservice/consultas/ResumenPublicacion.java (+3) |
| T3d | Taller 3: pruebas unitarias del dominio | 2 | 2 | 1 clases de prueba |
| T4a | Taller 4 (hexagonal): paquete de puertos | 3 | 3 | 3 archivos |
| T4b | Taller 4: adaptadores o infraestructura separados | 3 | 3 | 12 archivos |
| T4c | Taller 4: el dominio no importa Spring | 2 | 2 | limpio |
| T5a | Taller 5: al menos 2 servicios independientes con puerto propio | 4 | 4 | 5 servicios; 5 puertos distintos |
| T5b | Taller 5: NOTAS.md con la auditoria | 5 | 5 | 548 palabras |
| T6a | Taller 6: gRPC (archivo .proto) | 3 | 3 | investigadores-service/src/main/proto/investigador.proto, publicaciones-service/src/main/proto/investigador.proto |
| T6b | Taller 6: Circuit Breaker | 3 | 3 | publicaciones-service/pom.xml, publicaciones-service/src/main/java/com/rica/publicacionesservice/publicaciones/VerificadorInvestigadorGrpc.java (+1) |
| T6c | Taller 6: Eureka (servidor y clientes) | 2 | 2 | 5 pom con Eureka |
| T6d | Taller 6: API Gateway | 2 | 2 | gateway/pom.xml |
| T6e | Taller 6: Kafka con productor, consumidor y Outbox | 2 | 2 | consumidores 4; productores 8 |
| T8a | Taller 8 (CQRS): modelo de lectura o proyector | 4 | 4 | consultas-service/src/main/java/com/rica/consultasservice/consultas/FichaInvestigador.java, consultas-service/src/main/java/com/rica/consultasservice/consultas/FichaRepository.java, consultas-service/src/main/java/com/rica/consultasservice/consultas/FichaResponse.java (+4) |
| T8b | Taller 8: un servicio consume eventos con @KafkaListener | 3 | 3 | 4 clases |
| T8c | Taller 8: pruebas del proyector o de la ficha | 3 | 3 | 2 clases de prueba |
| T9a | Taller 9 (EDA): Dead Letter Topic | 4 | 4 | consultas-service/src/main/java/com/rica/consultasservice/consultas/KafkaErrorHandlingConfig.java |
| T9b | Taller 9: consumidor idempotente | 4 | 4 | consultas-service/src/main/java/com/rica/consultasservice/consultas/FichaInvestigador.java, consultas-service/src/main/java/com/rica/consultasservice/consultas/KafkaErrorHandlingConfig.java (+2) |
| T9c | Taller 9: reintentos con espera (backoff) | 3 | 3 | consultas-service/src/main/java/com/rica/consultasservice/consultas/KafkaErrorHandlingConfig.java, docker-compose.yml (+2) |

**Total automatico: 60 / 60**

## Talleres 10 y 11 (bonus opcional)

| ID | Criterio | Puntos | Obtenido | Evidencia |
|---|---|---|---|---|
| T10a | Taller 10 (observabilidad): Actuator en al menos 3 servicios | 1 | 1 | 3 servicios |
| T10b | Taller 10: filtro con X-Correlation-Id | 1 | 1 | consultas-service/src/main/java/com/rica/consultasservice/compartido/CorrelationIdFilter.java, investigadores-service/src/main/java/com/rica/investigadoresservice/compartido/CorrelationIdFilter.java (+1) |
| T10c | Taller 10: patron de log con correlationId en al menos 3 servicios | 1 | 1 | 3 archivos yml |
| T10d | Taller 10: el correlationId viaja dentro de los eventos | 1 | 1 | investigadores-service/src/main/java/com/rica/investigadoresservice/investigadores/infraestructura/salida/mensajeria/OutboxPublicadorEventos.java |
| T11a | Taller 11: Dockerfile multi-etapa | 1 | 1 | consultas-service/Dockerfile, eureka-server/Dockerfile (+3) |
| T11b | Taller 11: .dockerignore | 1 | 1 |  |
| T11c | Taller 11: docker-compose.yml | 1 | 1 |  |
| T11d | Taller 11: CI con matriz de servicios | 2 | 2 | .github/workflows/ci.yml |
| T11e | Taller 11: el CI ejecuta las pruebas con mvnw | 1 | 1 | .github/workflows/ci.yml |

**Bonus: 10 / 10**

## Aporte por integrante (de git log)

| Autor | Commits | Archivos distintos | Lineas + | Lineas - |
|---|---|---|---|---|
| Nicolás Ruiz | 25 | 261 | 15043 | 2412 |

## Avisos (no puntuan)

- ATENCION: hay cambios sin confirmar. El informe describe el repositorio tal como esta en disco, no el commit. Para evaluar el commit use un clon limpio.

La parte manual de la rubrica (criterios MT) se completa en Moodle con la guia de evaluacion.
