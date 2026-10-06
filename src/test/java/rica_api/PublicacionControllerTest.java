package rica_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.publicaciones.LimitePublicacionesAlcanzadoException;
import rica_api.publicaciones.Publicacion;
import rica_api.publicaciones.PublicacionController;
import rica_api.publicaciones.PublicacionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicacionController.class)
class PublicacionControllerTest {

    private static final String CUERPO_VALIDO = """
            {"titulo":"Publicación de prueba","tipo":"articulo","anio":2026,"investigadorCorreo":"ana.torres@uptc.edu.co"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicacionService publicacionService;

    @Test
    void registrarDevuelve201CuandoLaPublicacionSeGuarda() throws Exception {
        Publicacion guardada = new Publicacion();
        guardada.setId("P-1");
        guardada.setTitulo("Publicación de prueba");
        guardada.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        guardada.setAnio(2026);
        when(publicacionService.registrar(any(Publicacion.class))).thenReturn(guardada);

        mockMvc.perform(post("/api/publicaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarDevuelve404CuandoElInvestigadorNoExiste() throws Exception {
        when(publicacionService.registrar(any(Publicacion.class)))
                .thenThrow(new RecursoNoEncontradoException(
                        "No existe un investigador con correo ana.torres@uptc.edu.co"));

        mockMvc.perform(post("/api/publicaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarDevuelve409CuandoSeAlcanzoElLimiteAnual() throws Exception {
        when(publicacionService.registrar(any(Publicacion.class)))
                .thenThrow(new LimitePublicacionesAlcanzadoException(
                        "El investigador alcanzó el límite anual de 3 publicaciones para el año 2026"));

        mockMvc.perform(post("/api/publicaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }
}
