package pe.edu.upeu.citas.controller;

import pe.edu.upeu.citas.dto.CitaRequest;
import pe.edu.upeu.citas.dto.CitaResponse;
import pe.edu.upeu.citas.security.JwtClaims;
import pe.edu.upeu.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * El idCliente nunca viaja en el body: se extrae del JWT validado, de modo
 * que un cliente no puede crear ni consultar citas a nombre de otro.
 */
@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping
    public List<CitaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return citaService.listar(JwtClaims.idCliente(jwt));
    }

    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return citaService.obtener(id, JwtClaims.idCliente(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse crear(@Valid @RequestBody CitaRequest request, @AuthenticationPrincipal Jwt jwt) {
        return citaService.crear(JwtClaims.idCliente(jwt), request);
    }

    @PutMapping("/{id}")
    public CitaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CitaRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return citaService.actualizar(id, JwtClaims.idCliente(jwt), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        citaService.eliminar(id, JwtClaims.idCliente(jwt));
    }
}
