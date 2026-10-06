package rica_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.CorreoDuplicadoException;
import rica_api.investigadores.CorreoInstitucional;
import rica_api.investigadores.GrupoInvestigacion;
import rica_api.investigadores.Investigador;
import rica_api.investigadores.InvestigadorFactory;
import rica_api.investigadores.InvestigadorRepository;
import rica_api.investigadores.InvestigadorService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigadorServiceTest {

    @Mock
    private InvestigadorRepository investigadorRepository;

    private InvestigadorService investigadorService;

    @BeforeEach
    void setUp() {
        investigadorService = new InvestigadorService(investigadorRepository, new InvestigadorFactory());
    }

    @Test
    void buscarPorIdDevuelveElInvestigadorCuandoExiste() {
        Investigador investigador = new Investigador(1L, "Ana Torres",
                new CorreoInstitucional("ana.torres@uptc.edu.co"), new GrupoInvestigacion("GIT-UPTC"));
        when(investigadorRepository.findById(1L)).thenReturn(Optional.of(investigador));

        Investigador resultado = investigadorService.buscarPorId(1L);

        assertThat(resultado.getNombreCompleto()).isEqualTo("Ana Torres");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(investigadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> investigadorService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrarConstruyeElAgregadoCompletoYLoGuarda() {
        when(investigadorRepository.existsByCorreoInstitucionalValor("ana.torres@uptc.edu.co")).thenReturn(false);
        when(investigadorRepository.save(any(Investigador.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Investigador resultado = investigadorService.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(resultado.getCorreoInstitucional().valor()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(resultado.getGrupoInvestigacion().nombre()).isEqualTo("GIT-UPTC");
        verify(investigadorRepository).save(resultado);
    }

    @Test
    void registrarRechazaCorreoQueNoEsInstitucional() {
        assertThatThrownBy(() -> investigadorService.registrar("Ana Torres", "ana@gmail.com", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        when(investigadorRepository.existsByCorreoInstitucionalValor("carlos.ruiz@uptc.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> investigadorService.registrar("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(investigadorRepository).existsByCorreoInstitucionalValor("carlos.ruiz@uptc.edu.co");
    }

}
