package pe.edu.upeu.auth.event;

import lombok.*;

/**
 * Contrato del evento que PUBLICA veterinaria-auth-ms (topico usuario-eventos):
 * se registro una cuenta PENDIENTE y hace falta crear su perfil de cliente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsuarioRegistradoEvento {
    public static final String TIPO = "usuario.registrado";

    private String tipoEvento;
    private Long usuarioId;
    private String email;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String direccion;
    private String origen;
    private Long timestamp;
}
