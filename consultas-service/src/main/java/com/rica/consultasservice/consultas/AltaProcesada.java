package com.rica.consultasservice.consultas;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Registro de una alta de investigador ya contabilizada.
 *
 * Guarda el id del evento procesado para que el consumidor sea idempotente:
 * incrementar un contador NO es idempotente por naturaleza (contar dos veces
 * el mismo evento da 2), asi que la unica forma de resistir duplicados es
 * registrar el id y descartar los repetidos antes de sumar.
 *
 * Cada documento usa como clave el id del investigador, que en el evento
 * "investigadores.registrados" identifica de forma estable un alta concreta.
 */
@Document(collection = "altas_procesadas")
public class AltaProcesada {

    @Id
    private String eventoId;

    public AltaProcesada(String eventoId) {
        this.eventoId = eventoId;
    }

    public String getEventoId() { return eventoId; }

}
