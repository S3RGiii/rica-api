package com.rica.investigadoresservice.investigadores.infraestructura.salida.mensajeria;

import com.rica.investigadoresservice.investigadores.dominio.puertos.PublicadorEventos;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class OutboxPublicadorEventos implements PublicadorEventos {

    private final OutboxEventRepository repositorio;
    private final ObjectMapper objectMapper;

    public OutboxPublicadorEventos(OutboxEventRepository repositorio, ObjectMapper objectMapper) {
        this.repositorio = repositorio;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(String topico, String clave, Map<String, Object> contenido) {
        // El correlationId de la peticion que origino el hecho viaja DENTRO del evento,
        // para que quien lo consuma pueda unir su traza con la del productor.
        Map<String, Object> cuerpo = new HashMap<>(contenido);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            cuerpo.put("correlationId", correlationId);
        }
        String payload = objectMapper.writeValueAsString(cuerpo);
        repositorio.save(new OutboxEvent(topico, clave, payload));
    }
}
