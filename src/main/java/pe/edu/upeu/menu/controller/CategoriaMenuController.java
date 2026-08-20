package pe.edu.upeu.menu.controller;

import pe.edu.upeu.menu.dto.CategoriaMenuRequest;
import pe.edu.upeu.menu.dto.CategoriaMenuResponse;
import pe.edu.upeu.menu.service.CategoriaMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias-menu")
@RequiredArgsConstructor
public class CategoriaMenuController {

    private final CategoriaMenuService categoriaMenuService;

    @GetMapping
    public List<CategoriaMenuResponse> listar() {
        return categoriaMenuService.listar();
    }

    @GetMapping("/{id}")
    public CategoriaMenuResponse obtener(@PathVariable Long id) {
        return categoriaMenuService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaMenuResponse crear(@Valid @RequestBody CategoriaMenuRequest request) {
        return categoriaMenuService.crear(request);
    }

    @PutMapping("/{id}")
    public CategoriaMenuResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaMenuRequest request) {
        return categoriaMenuService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaMenuService.eliminar(id);
    }
}