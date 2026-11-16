# Respuestas del taller — Lecciones 8 y 9

**Nicolás Ruiz · Ingeniería de Software II · 2026-11-15**

Las respuestas citan valores que salieron en **mi** ejecución del sistema, y que están
visibles en las capturas de [`docs/capturas/`](../capturas/). Cada valor se puede contrastar
con la imagen correspondiente.

---

## Lección 8 — CQRS: explicación del taller

### Qué problema resolvió el modelo de lectura en mi ejecución

En el sistema había dos servicios de escritura con bases distintas: `investigadores-service`
en PostgreSQL y `publicaciones-service` en MongoDB. Para mostrar la ficha de un investigador
con sus publicaciones había que llamar a los dos y unir el resultado.

Al levantar `consultas-service` con el proyector funcionando, una sola petición resolvió
esa pantalla completa. En mi ejecución:

```
GET /api/consultas/investigadores/ana.torres@uptc.edu.co
→ 200 OK · 63 ms · 723 B
```

La respuesta trajo el investigador **y** sus publicaciones en el mismo documento:

```json
{
  "correo": "ana.torres@uptc.edu.co",
  "nombreCompleto": "Ana Torres",
  "estado": "ACTIVO",
  "totalPublicaciones": 5,
  "publicaciones": [
    { "id": "6afa6f9c62aa9aa4daf3adc8", "titulo": "Redes de colaboración académica en instituciones públicas", "tipo": "articulo", "anio": 2025 },
    ...
  ]
}
```

El dato que importa es **`totalPublicaciones: 5`**: ese campo no se calcula al consultar, se
mantiene actualizado por el proyector. Y `63 ms` no incluye ninguna llamada a los servicios de
escritura, porque el modelo de lectura ya tiene todo.

**La prueba de que el modelo se alimenta por eventos** está en el log del proyector, en la
misma ejecución:

```
Proyección: ficha de juan.pelaez@uptc.edu.co actualizada
Proyección: ficha de juan.pelaez@uptc.edu.co ahora tiene 1 publicaciones
Proyección: ficha de juan.pelaez@uptc.edu.co ahora tiene 2 publicaciones
```

Cada línea corresponde a un evento consumido de Kafka, no a una petición HTTP. La ficha se
construyó **después** de que ocurrieron los hechos, en otro proceso.

### Por qué el proyector es idempotente y qué pasaría si no lo fuera

En `FichaInvestigador.agregarPublicacion` la comparación es por `id`:

```java
boolean yaExiste = publicaciones.stream().anyMatch(p -> p.id().equals(nueva.id()));
if (yaExiste) return false;
```

Kafka garantiza **al menos una vez**, así que un reinicio o un rebalanceo puede entregar el
mismo evento dos veces. Sin esa comprobación, la lista tendría la publicación duplicada y
`totalPublicaciones` sería mayor al real. En mi ejecución, `totalPublicaciones: 5` coincide
con las cinco publicaciones listadas en el mismo JSON: el conteo no está inflado.

### El ranking

```
GET /api/consultas/ranking?top=3
→ 200 OK · 12 ms · 1.28 KB
```

El ranking sale ordenado por `totalPublicaciones` sin recorrer los servicios de escritura,
porque el total está guardado en el documento de lectura. Es exactamente el motivo por el que
la clase lo guarda en vez de calcularlo: **permite ordenar con un índice de MongoDB**.

### Lo que se paga

La consistencia eventual. Si registro una publicación y consulto la ficha de inmediato, puede
no aparecer todavía. En la práctica el retardo lo marca el consumidor de Kafka. Para una
pantalla de consulta es aceptable; para una confirmación inmediata al usuario, no.

---

## Lección 9 — EDA: consumidores robustos

### El contador de altas y su verificación

El taller pedía un segundo consumidor del tópico `investigadores.registrados`, con grupo
propio, que contara altas. Lo implementé en `ContadorAltasListener` con el grupo
`consultas-service-contador-altas`, separado del grupo `consultas-service-v2` del proyector.

Eso se ve en el log de mi ejecución: los dos grupos reciben la misma partición.

```
consultas-service-v2: partitions assigned: [investigadores.registrados-0]
consultas-service-contador-altas: partitions assigned: [investigadores.registrados-0]
```

**El valor antes y después:**

| Momento | Petición | Respuesta |
|---|---|---|
| Antes | `GET /api/consultas/estadisticas` | `{"altas": 3}` · 200 OK · 13 ms |
| Después de una alta nueva | `GET /api/consultas/estadisticas` | `{"altas": 4}` · 200 OK · 9 ms |

El incremento de 3 a 4 demuestra que el contador está procesando eventos reales de Kafka, no
un valor fijo.

### La idempotencia, y por qué aquí sí hace falta

En la Lección 8 la operación era idempotente por naturaleza: agregar a una lista comparando
por id. **Incrementar un contador no lo es.** Si el mismo evento llega dos veces y se suma dos
veces, el total queda mal y no hay forma de detectarlo después.

La solución que implementé es la segunda que menciona la guía: **registrar los ids ya
procesados**. `AltaProcesada` usa el id del evento como `_id` de MongoDB, y
`ContadorAltasService.registrarAlta` comprueba `existsById` antes de sumar.

En el log se ve el patrón:

```
Contador de altas: alta registrada (id=2). Total actual: 2
Contador de altas: alta registrada (id=3). Total actual: 3
... alta registrada (id=4). Total actual: 4
```

Cada alta se cuenta **una vez y con su propio id**. Si Kafka reentrega el `id=4`, la
comprobación `existsById` lo descarta y el total no cambia.

**La trampa que menciona la guía** —que registrar el id y aplicar el efecto son dos
escrituras— la resuelve aquí la clave única de MongoDB: el `save` falla si el `_id` ya existe,
así que no hay ventana en la que se cuente sin registrar.

### Los tres modos de falla y cómo los traté

| Falla | Cómo la resolví | Dónde |
|---|---|---|
| **Duplicado** | `existsById` sobre `AltaProcesada` | `ContadorAltasService` |
| **Transitoria** | Reintentos con `ExponentialBackOff(500 ms, ×2)`, techo 4 s, tope 15 s | `KafkaErrorHandlingConfig` |
| **Permanente (venenoso)** | `DeadLetterPublishingRecoverer` + `JacksonException` marcada como no reintentable | `KafkaErrorHandlingConfig` |

### La DLT, verificada en ejecución

Provoqué un mensaje venenoso publicando `{esto no es json}` en `investigadores.registrados`.
Como no es JSON válido, falla al deserializarse con `JacksonException`, que está declarada
como **no reintentable**: no tiene sentido reintentar un mensaje que nunca va a parsearse.

El resultado lo comprobé consumiendo el tópico muerto:

```
docker exec -it rica-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic investigadores.registrados.DLT --from-beginning

{esto no es json}
{esto no es json}
```

El mensaje venenoso **no se perdió ni bloqueó la partición**: quedó aislado en
`investigadores.registrados.DLT` y el consumidor siguió avanzando con los mensajes
posteriores. Eso lo confirma el hecho de que después del venenoso el contador siguió
procesando altas válidas hasta llegar a 4.

### Qué no hice y por qué

- **No implementé un reprocesador de la DLT.** La guía advierte que una DLT sin dueño «es
  pérdida de datos con pasos extra». Aquí está declarada y es inspeccionable, pero nadie la
  revisa automáticamente. Es deuda reconocida.
- **No usé un schema registry.** Los eventos van en JSON sin validación de compatibilidad, así
  que la disciplina de esquemas es manual. Para el alcance del taller alcanza; en producción
  sería insuficiente.
- **La ventana de doble escritura no está cerrada del todo.** El `_id` único de MongoDB cubre
  el caso de este taller, pero un diseño más estricto necesitaría que el registro y el efecto
  ocurrieran en la misma transacción.
