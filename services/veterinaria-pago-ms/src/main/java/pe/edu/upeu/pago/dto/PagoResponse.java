package pe.edu.upeu.pago.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {
    private Long id;
    private Long citaId;
    private Long idCliente;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private LocalDateTime fechaPago;
}
