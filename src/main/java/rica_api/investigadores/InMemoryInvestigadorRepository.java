package rica_api.investigadores;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryInvestigadorRepository implements InvestigadorRepository {

    private final Map<Long, Investigador> almacen = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public List<Investigador> findAll() {
        return List.copyOf(almacen.values());
    }

    @Override
    public Optional<Investigador> findById(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public Investigador save(Investigador investigador) {
        if (investigador.getId() == null) {
            investigador.setId(secuencia.incrementAndGet());
        }
        almacen.put(investigador.getId(), investigador);
        return investigador;
    }

    @Override
    public boolean existsByCorreoInstitucional(String correoInstitucional) {
        return almacen.values().stream()
                .anyMatch(i -> i.getCorreoInstitucional().equalsIgnoreCase(correoInstitucional));
    }

}