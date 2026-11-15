package com.rica.consultasservice.consultas;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Estadísticas del lado de lectura (Lección 9): total de altas contabilizadas. */
@RestController
@RequestMapping("/api/consultas")
public class EstadisticasController {

    private final ContadorAltasService contador;

    public EstadisticasController(ContadorAltasService contador) {
        this.contador = contador;
    }

    @GetMapping("/estadisticas")
    public EstadisticasResponse estadisticas() {
        return new EstadisticasResponse(contador.total());
    }
}
