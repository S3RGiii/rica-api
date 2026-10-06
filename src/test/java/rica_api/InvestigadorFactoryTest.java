package rica_api;

import org.junit.jupiter.api.Test;
import rica_api.investigadores.Investigador;
import rica_api.investigadores.InvestigadorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvestigadorFactoryTest {

    private final InvestigadorFactory factory = new InvestigadorFactory();

    @Test
    void creaUnInvestigadorCompletoYValidado() {
        Investigador investigador = factory.crear("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(investigador.getId()).isNull();
        assertThat(investigador.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(investigador.getCorreoInstitucional().valor()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(investigador.getGrupoInvestigacion().nombre()).isEqualTo("GIT-UPTC");
    }

    @Test
    void rechazaUnNombreEnBlanco() {
        assertThatThrownBy(() -> factory.crear("  ", "ana.torres@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void rechazaUnCorreoFueraDelDominioInstitucional() {
        assertThatThrownBy(() -> factory.crear("Ana Torres", "ana@gmail.com", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
