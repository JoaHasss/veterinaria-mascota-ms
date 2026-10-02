package pe.edu.upeu.cliente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;
import pe.edu.upeu.cliente.security.ClienteAccessPolicy;
import pe.edu.upeu.cliente.service.ClienteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteAccessPolicy clienteAccess;

    /** RBAC: solo ADMIN ve el listado completo. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    /** Perfil propio: el id sale del claim idCliente, no de la URL. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse miPerfil(Authentication authentication) {
        Long idCliente = clienteAccess.idCliente(authentication);
        if (idCliente == null) {
            throw new AccessDeniedException("El token no contiene el claim idCliente");
        }
        return clienteService.obtener(idCliente);
    }

    /** ABAC: un CLIENTE solo puede ver su propio perfil; ADMIN ve cualquiera. */
    @GetMapping("/{id}")
    @PreAuthorize("@clienteAccess.esPropietarioOAdmin(authentication, #id)")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return clienteService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@clienteAccess.esPropietarioOAdmin(authentication, #id)")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }
}
