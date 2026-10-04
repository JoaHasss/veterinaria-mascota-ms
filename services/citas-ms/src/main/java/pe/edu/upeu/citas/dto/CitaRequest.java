package pe.edu.upeu.citas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CitaRequest {

    @NotNull
    private Long mascotaId;

    @NotBlank
    @Size(max = 255)
    private String motivo;

    @NotNull
    private LocalDateTime fechaHora;

    /**
     * Al crear, citas-ms fija el estado (PENDIENTE_PAGO o PENDIENTE_VALIDACION);
     * este valor solo se usa al actualizar.
     */
    @Size(max = 20)
    private String estado;

    /** Costo de la atencion; si no se envia se aplica la tarifa de consulta. */
    @DecimalMin(value = "0.01")
    private BigDecimal monto;

    @Pattern(regexp = "TARJETA|YAPE|EFECTIVO", message = "metodoPago debe ser TARJETA, YAPE o EFECTIVO")
    private String metodoPago;
}
