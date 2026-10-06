package rica_api.publicaciones;

import org.springframework.stereotype.Service;

@Service
public class LimitePublicacionesAnualesService {

    public static final int LIMITE_ANUAL = 3;

    private final PublicacionRepository publicacionRepository;

    public LimitePublicacionesAnualesService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public boolean alcanzoLimite(String investigadorCorreo, int anio) {
        long publicacionesDelAnio = publicacionRepository.findByInvestigadorCorreo(investigadorCorreo).stream()
                .filter(publicacion -> publicacion.getAnio() != null && publicacion.getAnio() == anio)
                .count();
        return publicacionesDelAnio >= LIMITE_ANUAL;
    }
}
