package pe.edu.upeu.veterinaria.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.veterinaria.entity.Insumo;
import pe.edu.upeu.veterinaria.service.InsumoService;

import java.util.List;

/**
 * S9 (Parte E): inventario de insumos clinicos. descontar-stock y
 * restaurar-stock los invoca citas-ms por Feign al crear y al compensar una cita.
 */
@Validated
@RestController
@RequestMapping("/api/v1/insumos")
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService insumoService;

    @GetMapping
    public List<Insumo> listar() {
        return insumoService.listar();
    }

    @GetMapping("/{id}")
    public Insumo obtener(@PathVariable Long id) {
        return insumoService.obtener(id);
    }

    @PatchMapping("/{id}/descontar-stock")
    public Insumo descontarStock(@PathVariable Long id, @RequestParam @Min(1) Integer cantidad) {
        return insumoService.descontarStock(id, cantidad);
    }

    @PatchMapping("/{id}/restaurar-stock")
    public Insumo restaurarStock(@PathVariable Long id, @RequestParam @Min(1) Integer cantidad) {
        return insumoService.restaurarStock(id, cantidad);
    }
}
