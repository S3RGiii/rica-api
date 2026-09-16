package rica_api.publicaciones;

public class PublicacionMapper  {

    private PublicacionMapper() {

    }
    public static Publicacion aEntidad(PublicacionRequest request) {
        Publicacion  publicacion = new Publicacion();
        publicacion.setAnio(request.getAnio());
        publicacion.setTitulo(request.getTitulo());
        publicacion.setTipo(request.getTipo());
        publicacion.setInvestigadorCorreo(request.getInvestigadorCorreo());
        publicacion.setDetalles(request.getDetalles());
        return publicacion;
    }
    public static PublicacionResponse aResponse(Publicacion publicacion) {
        return new PublicacionResponse(
                publicacion.getId(),
                publicacion.getTitulo(),
                publicacion.getInvestigadorCorreo(),
                publicacion.getAnio()
        );
    }
}
