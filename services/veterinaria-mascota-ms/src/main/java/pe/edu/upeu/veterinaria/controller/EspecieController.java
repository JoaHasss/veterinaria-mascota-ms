package pe.edu.upeu.veterinaria.controller;

import pe.edu.upeu.veterinaria.dto.EspecieRequest;
import pe.edu.upeu.veterinaria.dto.EspecieResponse;
import pe.edu.upeu.veterinaria.service.EspecieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especies")
@RequiredArgsConstructor
public class EspecieController {

    private final EspecieService especieService;

    @GetMapping
    public List<EspecieResponse> listar() {
        return especieService.listar();
    }

    @GetMapping("/{id}")
    public EspecieResponse obtener(@PathVariable Long id) {
        return especieService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspecieResponse crear(@Valid @RequestBody EspecieRequest request) {
        return especieService.crear(request);
    }

    @PutMapping("/{id}")
    public EspecieResponse actualizar(@PathVariable Long id, @Valid @RequestBody EspecieRequest request) {
        return especieService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        especieService.eliminar(id);
    }
}
