package rica_api.publicaciones;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.InvestigadorRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class PublicacionService implements PublicacionUseCase {

    private final PublicacionRepository publicacionRepository;
    private final InvestigadorRepository investigadorRepository;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;
    private final ApplicationEventPublisher eventPublisher;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              InvestigadorRepository investigadorRepository,
                              LimitePublicacionesAnualesService limitePublicacionesAnualesService,
                              ApplicationEventPublisher eventPublisher) {
        this.publicacionRepository = publicacionRepository;
        this.investigadorRepository = investigadorRepository;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Publicacion registrar(Publicacion publicacion) {
        if (!investigadorRepository.existsByCorreoInstitucionalValor(publicacion.getInvestigadorCorreo())) {
            throw new RecursoNoEncontradoException(
                    "No existe un investigador con correo " + publicacion.getInvestigadorCorreo());
        }

        int anio = publicacion.getAnio() != null ? publicacion.getAnio() : LocalDate.now().getYear();
        if (limitePublicacionesAnualesService.alcanzoLimite(publicacion.getInvestigadorCorreo(), anio)) {
            throw new LimitePublicacionesAlcanzadoException(
                    "El investigador alcanzó el límite anual de " + LimitePublicacionesAnualesService.LIMITE_ANUAL
                            + " publicaciones para el año " + anio);
        }

        Publicacion guardada = publicacionRepository.save(publicacion);
        eventPublisher.publishEvent(new PublicacionRegistrada(
                guardada.getId(), guardada.getInvestigadorCorreo(), Instant.now()));
        return guardada;
    }

    @Override
    public List<Publicacion> listarPorInvestigador(String investigadorCorreo) {
        return publicacionRepository.findByInvestigadorCorreo(investigadorCorreo);
    }

    @Override
    public Publicacion buscarPorId(String id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una publicación con id " + id));
    }

}
