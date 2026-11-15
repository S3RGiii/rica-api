package com.rica.consultasservice.consultas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class ContadorAltasServiceTest {

    private final Map<String, AltaProcesada> procesadas = new HashMap<>();
    private ContadorAltasService contador;

    @BeforeEach
    void preparar() {
        AltaProcesadaRepository repositorio = Mockito.mock(AltaProcesadaRepository.class);
        Mockito.when(repositorio.existsById(anyString()))
                .thenAnswer(i -> procesadas.containsKey(i.<String>getArgument(0)));
        Mockito.when(repositorio.save(any(AltaProcesada.class)))
                .thenAnswer(i -> {
                    AltaProcesada alta = i.getArgument(0);
                    procesadas.put(alta.getEventoId(), alta);
                    return alta;
                });
        Mockito.when(repositorio.count()).thenAnswer(i -> (long) procesadas.size());
        contador = new ContadorAltasService(repositorio, JsonMapper.builder().build());
    }

    private static String eventoAlta(String id) {
        return "{\"id\":" + id + ",\"correoInstitucional\":\"ana.torres@uptc.edu.co\",\"nombreCompleto\":\"Ana Torres\"}";
    }

    @Test
    void cuentaCadaAltaUnaSolaVez() {
        contador.registrarAlta(eventoAlta("1"));
        contador.registrarAlta(eventoAlta("1")); // duplicado de Kafka

        assertThat(contador.total()).isEqualTo(1);
    }

    @Test
    void cuentaAltasDistintas() {
        contador.registrarAlta(eventoAlta("1"));
        contador.registrarAlta(eventoAlta("2"));

        assertThat(contador.total()).isEqualTo(2);
    }

    @Test
    void unEventoMalformadoNoLlegaAlConteo() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> contador.registrarAlta("{no-json"))
                .isInstanceOf(RuntimeException.class);
        assertThat(contador.total()).isZero();
    }
}
