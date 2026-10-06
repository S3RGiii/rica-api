package rica_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rica_api.investigadores.CorreoDuplicadoException;
import rica_api.investigadores.Investigador;
import rica_api.investigadores.InvestigadorController;
import rica_api.investigadores.InvestigadorFactory;
import rica_api.investigadores.InvestigadorUseCase;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvestigadorController.class)
class InvestigadorControllerTest {

    private static final String CUERPO_VALIDO = """
            {"nombreCompleto":"Ana Torres","correoInstitucional":"ana.torres@uptc.edu.co","grupoInvestigacion":"GIT-UPTC"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvestigadorUseCase investigadorUseCase;

    @Test
    void registrarDevuelve201CuandoElInvestigadorSeGuarda() throws Exception {
        Investigador guardado = new InvestigadorFactory().crear("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");
        guardado.setId(1L);
        when(investigadorUseCase.registrar(anyString(), anyString(), anyString())).thenReturn(guardado);

        mockMvc.perform(post("/api/investigadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarDevuelve400CuandoUnaReglaDeDominioNoSeCumple() throws Exception {
        when(investigadorUseCase.registrar(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException(
                        "El correo institucional debe pertenecer al dominio @uptc.edu.co"));

        mockMvc.perform(post("/api/investigadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarDevuelve409CuandoElCorreoEstaDuplicado() throws Exception {
        when(investigadorUseCase.registrar(anyString(), anyString(), anyString()))
                .thenThrow(new CorreoDuplicadoException(
                        "Ya existe un investigador registrado con el correo ana.torres@uptc.edu.co"));

        mockMvc.perform(post("/api/investigadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }
}
