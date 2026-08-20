package pe.edu.upeu.menu.controller;

import pe.edu.upeu.menu.dto.PlatoRequest;
import pe.edu.upeu.menu.dto.PlatoResponse;
import pe.edu.upeu.menu.service.PlatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platos")
@RequiredArgsConstructor
public class PlatoController {

    private final PlatoService platoService;

    @GetMapping
    public List<PlatoResponse> listar() {
        return platoService.listar();
    }

    @GetMapping("/{id}")
    public PlatoResponse obtener(@PathVariable Long id) {
        return platoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatoResponse crear(@Valid @RequestBody PlatoRequest request) {
        return platoService.crear(request);
    }

    @PutMapping("/{id}")
    public PlatoResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlatoRequest request) {
        return platoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        platoService.eliminar(id);
    }
}