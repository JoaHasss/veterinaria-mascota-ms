package pe.edu.upeu.veterinaria.controller;

import pe.edu.upeu.veterinaria.dto.MascotaRequest;
import pe.edu.upeu.veterinaria.dto.MascotaResponse;
import pe.edu.upeu.veterinaria.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @GetMapping
    public List<MascotaResponse> listar() {
        return mascotaService.listar();
    }

    @GetMapping("/{id}")
    public MascotaResponse obtener(@PathVariable Long id) {
        return mascotaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MascotaResponse crear(@Valid @RequestBody MascotaRequest request) {
        return mascotaService.crear(request);
    }

    @PutMapping("/{id}")
    public MascotaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MascotaRequest request) {
        return mascotaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        mascotaService.eliminar(id);
    }
}
