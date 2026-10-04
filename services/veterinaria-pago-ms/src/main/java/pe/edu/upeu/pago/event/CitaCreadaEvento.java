package pe.edu.upeu.pago.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Contrato del evento que CONSUME veterinaria-pago-ms (topico cita-eventos).
 * Es una copia propia del contrato publicado por citas-ms: no se comparte
 * ninguna libreria entre servicios para que cada uno evolucione por separado.
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
