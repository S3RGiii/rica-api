package rica_api.investigadores;

import org.springframework.stereotype.Component;

@Component
public class InvestigadorFactory {

    public Investigador crear(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        return new Investigador(
                null,
                nombreCompleto,
                new CorreoInstitucional(correoInstitucional),
                new GrupoInvestigacion(grupoInvestigacion));
    }
}
