package rica_api;

import org.junit.jupiter.api.Test;
import rica_api.investigadores.GrupoInvestigacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrupoInvestigacionTest {

    @Test
    void aceptaUnGrupoConNombre() {
        GrupoInvestigacion grupo = new GrupoInvestigacion("GIT-UPTC");

        assertThat(grupo.nombre()).isEqualTo("GIT-UPTC");
    }

    @Test
    void rechazaUnGrupoNulo() {
        assertThatThrownBy(() -> new GrupoInvestigacion(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaUnGrupoEnBlanco() {
        assertThatThrownBy(() -> new GrupoInvestigacion("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
