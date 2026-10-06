package rica_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.InvestigadorRepository;
import rica_api.publicaciones.LimitePublicacionesAlcanzadoException;
import rica_api.publicaciones.LimitePublicacionesAnualesService;
import rica_api.publicaciones.Publicacion;
import rica_api.publicaciones.PublicacionRegistrada;
import rica_api.publicaciones.PublicacionRepository;
import rica_api.publicaciones.PublicacionService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private InvestigadorRepository investigadorRepository;

    @Mock
    private LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PublicacionService publicacionService;

    @Test
    void registrarGuardaLaPublicacionYPublicaElEvento() {
        when(investigadorRepository.existsByCorreoInstitucionalValor("ana.torres@uptc.edu.co")).thenReturn(true);
        when(limitePublicacionesAnualesService.alcanzoLimite("ana.torres@uptc.edu.co", 2026)).thenReturn(false);
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(invocacion -> {
            Publicacion publicacion = invocacion.getArgument(0);
            publicacion.setId("P-1");
            return publicacion;
        });

        Publicacion resultado = publicacionService.registrar(publicacionDePrueba());

        assertThat(resultado.getId()).isEqualTo("P-1");
        verify(eventPublisher).publishEvent(any(PublicacionRegistrada.class));
    }

    @Test
    void registrarRechazaInvestigadorInexistente() {
        when(investigadorRepository.existsByCorreoInstitucionalValor("ana.torres@uptc.edu.co")).thenReturn(false);

        assertThatThrownBy(() -> publicacionService.registrar(publicacionDePrueba()))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(publicacionRepository, never()).save(any(Publicacion.class));
    }

    @Test
    void registrarRechazaCuandoSeAlcanzoElLimiteAnual() {
        when(investigadorRepository.existsByCorreoInstitucionalValor("ana.torres@uptc.edu.co")).thenReturn(true);
        when(limitePublicacionesAnualesService.alcanzoLimite("ana.torres@uptc.edu.co", 2026)).thenReturn(true);

        assertThatThrownBy(() -> publicacionService.registrar(publicacionDePrueba()))
                .isInstanceOf(LimitePublicacionesAlcanzadoException.class);

        verify(publicacionRepository, never()).save(any(Publicacion.class));
    }

    @Test
    void registrarSinAnioUsaElAnioActualParaVerificarElLimite() {
        when(investigadorRepository.existsByCorreoInstitucionalValor("ana.torres@uptc.edu.co")).thenReturn(true);
        when(limitePublicacionesAnualesService.alcanzoLimite("ana.torres@uptc.edu.co", LocalDate.now().getYear()))
                .thenReturn(false);
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Publicacion sinAnio = publicacionDePrueba();
        sinAnio.setAnio(null);

        publicacionService.registrar(sinAnio);

        verify(limitePublicacionesAnualesService).alcanzoLimite("ana.torres@uptc.edu.co", LocalDate.now().getYear());
    }

    private Publicacion publicacionDePrueba() {
        Publicacion publicacion = new Publicacion();
        publicacion.setTitulo("Publicación de prueba");
        publicacion.setAnio(2026);
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        return publicacion;
    }
}
