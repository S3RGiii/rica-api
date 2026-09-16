package rica_api.publicaciones;

import java.util.Map;

public class PublicacionResponse {

    private String id;
    private String titulo;
    private String tipo;
    private Integer anio;
    private String investigadorCorreo;
    private Map<String, String> detalles;

    public PublicacionResponse(String id, String titulo, String investigadorCorreo, Integer anio) {

        this.id = id;
        this.titulo = titulo;
        this.investigadorCorreo = investigadorCorreo;
        this.anio = anio;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getInvestigadorCorreo() {
        return investigadorCorreo;
    }

    public void setInvestigadorCorreo(String investigadorCorreo) {
        this.investigadorCorreo = investigadorCorreo;
    }

    public Map<String, String> getDetalles() {
        return detalles;
    }

    public void setDetalles(Map<String, String> detalles) {
        this.detalles = detalles;
    }

    @Override
    public String toString() {
        return "PublicacionResponse{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tipo='" + tipo + '\'' +
                ", anio=" + anio +
                ", investigadorCorreo='" + investigadorCorreo + '\'' +
                ", detalles=" + detalles +
                '}';
    }
}