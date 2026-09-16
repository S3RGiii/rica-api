package rica_api.publicaciones;

import java.util.Map;

public class PublicacionRequest {

    private String titulo;
    private String tipo;
    private Integer anio;
    private String investigadorCorreo;
    private Map<String,String> detalles;

    public PublicacionRequest() {}
    public PublicacionRequest(String titulo, String tipo, Integer anio, String investigadorCorreo, String detalles) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.anio = anio;
        this.investigadorCorreo = investigadorCorreo;
    }
    public PublicacionRequest(String titulo, String tipo, Integer anio, String detalles) {
        this.titulo = titulo;
        this.tipo = tipo;
    }
    public PublicacionRequest(String titulo, String tipo, Integer anio) {
        this.titulo = titulo;
    }
    public PublicacionRequest(String titulo, String tipo) {
        this.titulo = titulo;
    }
    public PublicacionRequest(String titulo) {
        this.titulo = titulo;
    }
    public PublicacionRequest(String titulo, Integer anio) {
        this.titulo = titulo;
    }


   public Map<String,String> getDetalles(){
        return detalles;

   }
    public void setDetalles(Map<String,String> detalles){
        this.detalles = detalles;
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
    public String toString() {
        return "PublicacionRequest [titulo=" + titulo + ", tipo=" + tipo + ", anio=" + anio + ", detalles=" + detalles + "]";
    }

}
