package rica_api;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final InvestigadorRepository investigadorRepository;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              InvestigadorRepository investigadorRepository) {
        this.publicacionRepository = publicacionRepository;
        this.investigadorRepository = investigadorRepository;
    }

    public Publicacion registrar(Publicacion publicacion) {
        if (!investigadorRepository.existsByCorreoInstitucional(publicacion.getInvestigadorCorreo())) {
            throw new IllegalArgumentException("El correo del investigador no está registrado.");
        }
        return publicacionRepository.save(publicacion);
    }

    public List<Publicacion> listarPorInvestigador(String correo) {
        return publicacionRepository.findByInvestigadorCorreo(correo);
    }

    public List<Publicacion> listarTodas() {
        return publicacionRepository.findAll();
    }
}