package pe.edu.upeu.pago.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Contrato del evento que PUBLICA veterinaria-pago-ms (topico pago-eventos).
 * Hecho de negocio en pasado: el pago de la cita ya fue validado.
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
