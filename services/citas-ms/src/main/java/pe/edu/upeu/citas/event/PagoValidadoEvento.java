package pe.edu.upeu.citas.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Contrato del evento que CONSUME citas-ms (topico pago-eventos),
 * copia propia del contrato publicado por veterinaria-pago-ms.
 * tipoEvento: pago.validado (confirma la cita) o pago.fallido (la compensa, S9).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PagoValidadoEvento {
    public static final String TIPO = "pago.validado";
    public static final String TIPO_FALLIDO = "pago.fallido";

    private String tipoEvento;
    private Long citaId;
    private Long pagoId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String motivo;
    private String origen;
    private Long timestamp;
}
