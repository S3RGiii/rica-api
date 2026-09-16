package rica_api.publicaciones;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PublicacionRepository extends MongoRepository<Publicacion, String> {

    List<Publicacion> findByInvestigadorCorreo(String investigadorCorreo);

}