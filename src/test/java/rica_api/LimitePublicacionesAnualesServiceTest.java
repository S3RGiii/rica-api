package rica_api;

import org.junit.jupiter.api.Test;
import rica_api.publicaciones.LimitePublicacionesAnualesService;
import rica_api.publicaciones.Publicacion;
import rica_api.publicaciones.PublicacionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LimitePublicacionesAnualesServiceTest {

    private final PublicacionRepository publicacionRepository = mock(PublicacionRepository.class);
    private final LimitePublicacionesAnualesService limiteService =
            new LimitePublicacionesAnualesService(publicacionRepository);

    @Test
    void noAlcanzaElLimiteConMenosDeTresPublicacionesDelAnio() {
        when(publicacionRepository.findByInvestigadorCorreo("ana.torres@uptc.edu.co"))
                .thenReturn(List.of(publicacion(2026), publicacion(2026)));

        assertThat(limiteService.alcanzoLimite("ana.torres@uptc.edu.co", 2026)).isFalse();
    }

    @Test
    void alcanzaElLimiteConTresPublicacionesDelAnio() {
        when(publicacionRepository.findByInvestigadorCorreo("ana.torres@uptc.edu.co"))
                .thenReturn(List.of(publicacion(2026), publicacion(2026), publicacion(2026)));

        assertThat(limiteService.alcanzoLimite("ana.torres@uptc.edu.co", 2026)).isTrue();
    }

    @Test
    void ignoraPublicacionesDeOtrosAnios() {
        when(publicacionRepository.findByInvestigadorCorreo("ana.torres@uptc.edu.co"))
                .thenReturn(List.of(publicacion(2025), publicacion(2025), publicacion(2025), publicacion(2026)));

        assertThat(limiteService.alcanzoLimite("ana.torres@uptc.edu.co", 2026)).isFalse();
    }

    private Publicacion publicacion(Integer anio) {
        Publicacion publicacion = new Publicacion();
        publicacion.setAnio(anio);
        return publicacion;
    }
}
