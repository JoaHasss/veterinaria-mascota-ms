package pe.edu.upeu.cliente.event;

import lombok.*;

/**
 * Contrato del evento que CONSUME veterinaria-cliente-ms (topico usuario-eventos),
 * copia propia del publicado por veterinaria-auth-ms:
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
