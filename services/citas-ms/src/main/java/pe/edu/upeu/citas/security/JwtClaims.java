package pe.edu.upeu.citas.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtClaims {

    public static final String ID_CLIENTE = "idCliente";

    private JwtClaims() {
    }

    /**
     * Obtiene el idCliente del token ya validado. Un usuario sin ese claim
     * (p. ej. ADMIN) no es dueño de ninguna cita, por eso se responde 403.
     */
    public static Long idCliente(Jwt jwt) {
        Number idCliente = jwt.getClaim(ID_CLIENTE);
        if (idCliente == null) {
            throw new AccessDeniedException("El token no contiene el claim idCliente");
        }
        return idCliente.longValue();
    }
}
