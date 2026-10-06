package rica_api.investigadores;

import jakarta.persistence.Embeddable;

@Embeddable
public record GrupoInvestigacion(String nombre) {

    public GrupoInvestigacion {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El grupo de investigación es obligatorio");
        }
    }
}
