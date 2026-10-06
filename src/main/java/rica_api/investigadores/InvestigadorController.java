package rica_api.investigadores;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/investigadores")
public class InvestigadorController {

    private final InvestigadorUseCase investigadorUseCase;

    public InvestigadorController(InvestigadorUseCase investigadorUseCase) {
        this.investigadorUseCase = investigadorUseCase;
    }

    @GetMapping
    public List<InvestigadorResponse> listar() {
        return investigadorUseCase.listarTodos().stream()
                .map(InvestigadorMapper::aResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public InvestigadorResponse buscarPorId(@PathVariable Long id) {
        Investigador investigador = investigadorUseCase.buscarPorId(id);
        return InvestigadorMapper.aResponse(investigador);
    }

    @PostMapping
    public ResponseEntity<InvestigadorResponse> registrar(@Valid @RequestBody InvestigadorRequest request) {
        Investigador guardado = investigadorUseCase.registrar(
                request.getNombreCompleto(),
                request.getCorreoInstitucional(),
                request.getGrupoInvestigacion());
        InvestigadorResponse response = InvestigadorMapper.aResponse(guardado);
        return ResponseEntity
                .created(URI.create("/api/investigadores/" + guardado.getId()))
                .body(response);
    }

}
