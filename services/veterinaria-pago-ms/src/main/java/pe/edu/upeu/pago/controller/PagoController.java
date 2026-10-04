package pe.edu.upeu.pago.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.service.PagoService;

import java.util.List;

/**
 * Solo lectura: los pagos se crean al consumir el evento cita.creada,
 * nunca por una llamada HTTP.
 */
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public List<PagoResponse> listar() {
        return pagoService.listar();
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtener(id);
    }
}
