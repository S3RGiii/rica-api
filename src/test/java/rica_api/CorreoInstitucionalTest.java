package rica_api;

import org.junit.jupiter.api.Test;
import rica_api.investigadores.CorreoInstitucional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorreoInstitucionalTest {

    @Test
    void aceptaUnCorreoDelDominioInstitucional() {
        CorreoInstitucional correo = new CorreoInstitucional("ana.torres@uptc.edu.co");

        assertThat(correo.valor()).isEqualTo("ana.torres@uptc.edu.co");
    }

    @Test
    void rechazaUnCorreoNulo() {
        assertThatThrownBy(() -> new CorreoInstitucional(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaUnCorreoDeOtroDominio() {
        assertThatThrownBy(() -> new CorreoInstitucional("ana.torres@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }
}
