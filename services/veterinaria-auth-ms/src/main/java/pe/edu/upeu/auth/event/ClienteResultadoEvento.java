package pe.edu.upeu.auth.event;

import lombok.*;

/**
 * Contrato del evento que CONSUME veterinaria-auth-ms (topico cliente-eventos),
 * copia propia del publicado por veterinaria-cliente-ms.
 * cliente.creado activa la cuenta; cliente.rechazado la compensa.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClienteResultadoEvento {
    public static final String TIPO_CREADO = "cliente.creado";
    public static final String TIPO_RECHAZADO = "cliente.rechazado";

    private String tipoEvento;
    private Long usuarioId;
    private Long clienteId;
    private String email;
    private String motivo;
    private String origen;
    private Long timestamp;
}
