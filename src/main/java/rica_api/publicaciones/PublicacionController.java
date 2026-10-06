package rica_api.publicaciones;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionUseCase publicacionUseCase;

    public PublicacionController(PublicacionUseCase publicacionUseCase) {
        this.publicacionUseCase = publicacionUseCase;
    }

    @GetMapping
    public List<PublicacionResponse> listarPorInvestigador(@RequestParam String investigadorCorreo) {
        return publicacionUseCase.listarPorInvestigador(investigadorCorreo).stream()
                .map(PublicacionMapper::aResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public PublicacionResponse buscarPorId(@PathVariable String id) {
        Publicacion publicacion = publicacionUseCase.buscarPorId(id);
        return PublicacionMapper.aResponse(publicacion);
    }

    @PostMapping
    public ResponseEntity<PublicacionResponse> registrar(@Valid @RequestBody PublicacionRequest request) {
        Publicacion publicacion = PublicacionMapper.aEntidad(request);
        Publicacion guardada = publicacionUseCase.registrar(publicacion);
        PublicacionResponse response = PublicacionMapper.aResponse(guardada);
        return ResponseEntity
                .created(URI.create("/api/publicaciones/" + guardada.getId()))
                .body(response);
    }

}
