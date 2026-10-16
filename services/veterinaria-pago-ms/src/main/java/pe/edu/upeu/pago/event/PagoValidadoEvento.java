package pe.edu.upeu.pago.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Contrato del evento que PUBLICA veterinaria-pago-ms (topico pago-eventos).
 * Hecho de negocio en pasado: el pago de la cita fue validado (pago.validado)
 * o rechazado por la pasarela (pago.fallido, S9). Ambos comparten la misma
 * forma; el consumidor decide que hacer segun tipoEvento.
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
    /** Solo en pago.fallido: motivo del rechazo. */
    private String motivo;
    private String origen;
    private Long timestamp;
}
