package com.rica.consultasservice.consultas;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * El lado de lectura declara los tópicos que consume, para poder arrancar
 * aunque nadie haya publicado todavía en ellos. Declarar un tópico que ya existe no hace nada.
 */
@Configuration
public class TopicosConfig {

    @Bean
    public NewTopic investigadoresRegistrados() {
        return TopicBuilder.name("investigadores.registrados").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic investigadoresEnEliminacion() {
        return TopicBuilder.name("investigadores.en-eliminacion").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic investigadoresEliminados() {
        return TopicBuilder.name("investigadores.eliminados").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic publicacionesRegistradas() {
        return TopicBuilder.name("publicaciones.registradas").partitions(1).replicas(1).build();
    }

    // Topicos muertos: aqui acaban los eventos que agotaron los reintentos.
    // Declararlos permite que existan desde el arranque y poder inspeccionarlos.
    @Bean
    public NewTopic investigadoresRegistradosDlt() {
        return TopicBuilder.name("investigadores.registrados" + KafkaErrorHandlingConfig.SUFIJO_DLT)
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic investigadoresEnEliminacionDlt() {
        return TopicBuilder.name("investigadores.en-eliminacion" + KafkaErrorHandlingConfig.SUFIJO_DLT)
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic investigadoresEliminadosDlt() {
        return TopicBuilder.name("investigadores.eliminados" + KafkaErrorHandlingConfig.SUFIJO_DLT)
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic publicacionesRegistradasDlt() {
        return TopicBuilder.name("publicaciones.registradas" + KafkaErrorHandlingConfig.SUFIJO_DLT)
                .partitions(1).replicas(1).build();
    }

}
