package rica_api;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import rica_api.investigadores.CorreoInstitucional;
import rica_api.investigadores.GrupoInvestigacion;
import rica_api.investigadores.Investigador;
import rica_api.investigadores.InvestigadorRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class InvestigadorRepositoryTest {

    @Autowired
    private InvestigadorRepository investigadorRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void guardaYRecuperaElAgregadoConSusValueObjects() {
        Investigador guardado = investigadorRepository.saveAndFlush(new Investigador(
                null,
                "Ana Torres",
                new CorreoInstitucional("repo.ana.torres@uptc.edu.co"),
                new GrupoInvestigacion("GIT-UPTC")));

        entityManager.clear();

        Investigador recuperado = investigadorRepository.findById(guardado.getId()).orElseThrow();

        assertThat(recuperado.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(recuperado.getCorreoInstitucional().valor()).isEqualTo("repo.ana.torres@uptc.edu.co");
        assertThat(recuperado.getGrupoInvestigacion().nombre()).isEqualTo("GIT-UPTC");
    }

    @Test
    void encuentraPorElValorDelCorreoInstitucional() {
        investigadorRepository.saveAndFlush(new Investigador(
                null,
                "Carlos Ruiz",
                new CorreoInstitucional("repo.carlos.ruiz@uptc.edu.co"),
                new GrupoInvestigacion("GIT-UPTC")));

        assertThat(investigadorRepository.existsByCorreoInstitucionalValor("repo.carlos.ruiz@uptc.edu.co")).isTrue();
        assertThat(investigadorRepository.existsByCorreoInstitucionalValor("repo.nadie@uptc.edu.co")).isFalse();
    }
}
