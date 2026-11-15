package com.rica.consultasservice.consultas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Politica de fallo del consumidor del lado de lectura.
 *
 * Un evento que no se puede proyectar no se puede descartar en silencio: si se pierde,
 * la ficha queda desactualizada para siempre y el modelo de lectura deja de ser reconstruible.
 *
 * - Reintentos con espera creciente (exponential backoff): un fallo transitorio
 *   (Mongo caido, timeout) se resuelve solo sin intervencion.
 * - Dead Letter Topic: si se agotan los reintentos, el mensaje se publica en
 *   <topico>.DLT para poder inspeccionarlo y reprocesarlo a mano.
 *
 * La proyeccion es idempotente (FichaInvestigador.agregarPublicacion compara por id),
 * asi que reintentar un mensaje ya aplicado no corrompe el total.
 */
@Configuration
public class KafkaErrorHandlingConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

    public static final String SUFIJO_DLT = ".DLT";

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recuperador = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (registro, excepcion) -> {
                    log.error("Evento enviado a DLT: topico={} offset={} causa={}",
                            registro.topic(), registro.offset(), excepcion.getMessage());
                    return new org.apache.kafka.common.TopicPartition(
                            registro.topic() + SUFIJO_DLT, registro.partition());
                });

        ExponentialBackOff backoff = new ExponentialBackOff(500L, 2.0);
        backoff.setMaxInterval(4000L);
        backoff.setMaxElapsedTime(15_000L);

        DefaultErrorHandler manejador = new DefaultErrorHandler(recuperador, backoff);
        // Errores de deserializacion no mejoran reintentando: van directo al DLT.
        manejador.addNotRetryableExceptions(
                tools.jackson.core.JacksonException.class,
                IllegalArgumentException.class);
        return manejador;
    }
}
