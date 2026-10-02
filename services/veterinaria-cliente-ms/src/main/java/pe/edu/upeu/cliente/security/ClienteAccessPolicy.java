package pe.edu.upeu.cliente.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Política ABAC: compara el atributo idCliente del JWT con el dueño del
 * recurso solicitado. Se invoca desde @PreAuthorize como
 * {@code @clienteAccess.esPropietarioOAdmin(authentication, #id)}.
 */
@Slf4j
@Component("clienteAccess")
public class ClienteAccessPolicy {

    public static final String ID_CLIENTE = "idCliente";

    public boolean esPropietarioOAdmin(Authentication authentication, Long idSolicitado) {
        if (esAdmin(authentication)) {
            return true;
        }
        Long idCliente = idCliente(authentication);
        boolean permitido = idCliente != null && idCliente.equals(idSolicitado);
        if (!permitido) {
            log.warn("ABAC denegado: usuario {} (idCliente={}) solicitó el cliente {}",
                    authentication.getName(), idCliente, idSolicitado);
        }
        return permitido;
    }

    /** Devuelve null si el token no trae el claim (p. ej. un ADMIN). */
    public Long idCliente(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt
                && jwt.getClaim(ID_CLIENTE) instanceof Number numero) {
            return numero.longValue();
        }
        return null;
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
