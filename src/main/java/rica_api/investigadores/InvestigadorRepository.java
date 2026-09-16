package rica_api.investigadores;

import java.util.List;
import java.util.Optional;

public interface InvestigadorRepository {

    List<Investigador> findAll();

    Optional<Investigador> findById(Long id);

    Investigador save(Investigador investigador);

    boolean existsByCorreoInstitucional(String correoInstitucional);

}