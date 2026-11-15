package com.rica.consultasservice.consultas;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Segundo consumidor del tópico de altas, con grupo propio.
 *
 * La lección 9 pide un consumidor distinto del proyector para poder provocar
 * fallas sobre él sin afectar la ficha. Un grupo propio garantiza que ambos
 * reciben todas las altas: el proyector alimenta la ficha y este alimenta el
 * contador de altas.
 */
@Component
public class ContadorAltasListener {

    private final ContadorAltasService contador;

    public ContadorAltasListener(ContadorAltasService contador) {
        this.contador = contador;
    }

    @KafkaListener(topics = "investigadores.registrados", groupId = "consultas-service-contador-altas")
    public void onAlta(String mensaje) {
        contador.registrarAlta(mensaje);
    }
}
