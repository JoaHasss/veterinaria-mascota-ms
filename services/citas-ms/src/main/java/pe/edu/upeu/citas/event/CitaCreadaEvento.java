package pe.edu.upeu.citas.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Contrato del evento que PUBLICA citas-ms (topico cita-eventos).
 * veterinaria-pago-ms mantiene su propia copia: no hay libreria compartida.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CitaCreadaEvento {
    public static final String TIPO = "cita.creada";

    private String tipoEvento;
    private Long citaId;
    private Long idCliente;
    private Long mascotaId;
    private BigDecimal monto;
    private String metodoPago;
    private String origen;
    private Long timestamp;
}
