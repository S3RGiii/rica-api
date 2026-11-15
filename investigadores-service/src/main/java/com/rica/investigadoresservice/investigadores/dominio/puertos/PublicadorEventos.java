package com.rica.investigadoresservice.investigadores.dominio.puertos;

import java.util.Map;

public interface PublicadorEventos {

    void publicar(String topico, String clave, Map<String, Object> contenido);
}
