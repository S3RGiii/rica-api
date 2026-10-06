package rica_api.publicaciones;

import java.util.List;

public interface PublicacionUseCase {

    Publicacion registrar(Publicacion publicacion);

    List<Publicacion> listarPorInvestigador(String investigadorCorreo);

    Publicacion buscarPorId(String id);

}
