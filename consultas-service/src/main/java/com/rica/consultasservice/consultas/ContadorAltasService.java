package com.rica.consultasservice.consultas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Consumidor del contador de altas (Lección 9).
 *
 * Cuenta cuántas altas de investigador se han registrado. Como "incrementar un
 * contador" no es idempotente por naturaleza, la idempotencia se resuelve
 * registrando el id del evento en una colección aparte: si el id ya está,
 * el mensaje es un duplicado y no se cuenta.
 *
 * La escritura del id y el efecto son dos operaciones, pero la operación de
 * guardado falla en MongoDB si el _id ya existe (clave única), así que la
 * segunda entrega no produce doble conteo. Ese es el patrón que pide la lección.
 */
@Service
public class ContadorAltasService {

    private static final Logger log = LoggerFactory.getLogger(ContadorAltasService.class);

    private final AltaProcesadaRepository procesadas;
    private final ObjectMapper objectMapper;

    public ContadorAltasService(AltaProcesadaRepository procesadas, ObjectMapper objectMapper) {
        this.procesadas = procesadas;
        this.objectMapper = objectMapper;
    }

    public void registrarAlta(String mensaje) {
        JsonNode evento = objectMapper.readTree(mensaje);
        String eventoId = evento.get("id").asText();

        if (procesadas.existsById(eventoId)) {
            log.info("Contador de altas: evento duplicado ignorado (id={})", eventoId);
            return;
        }

        procesadas.save(new AltaProcesada(eventoId));
        log.info("Contador de altas: alta registrada (id={}). Total actual: {}",
                eventoId, procesadas.count());
    }

    public long total() {
        return procesadas.count();
    }
}
