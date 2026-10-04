package pe.edu.upeu.citas.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Contrato del evento que CONSUME citas-ms (topico pago-eventos),
 * copia propia del contrato publicado por veterinaria-pago-ms.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PagoValidadoEvento {
    public static final String TIPO = "pago.validado";

    private String tipoEvento;
    private Long citaId;
    private Long pagoId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String origen;
    private Long timestamp;
}
