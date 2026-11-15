package com.rica.investigadoresservice.investigadores.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas del value object que valida la invariante del correo institucional. */
class CorreoInstitucionalTest {

    @Test
    void aceptaUnCorreoDelDominioInstitucional() {
        CorreoInstitucional correo = new CorreoInstitucional("ana.torres@uptc.edu.co");

        assertThat(correo.valor()).isEqualTo("ana.torres@uptc.edu.co");
    }

    @Test
    void rechazaUnCorreoDeOtroDominio() {
        assertThatThrownBy(() -> new CorreoInstitucional("ana.torres@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

    @Test
    void rechazaUnCorreoNulo() {
        assertThatThrownBy(() -> new CorreoInstitucional(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }
}
