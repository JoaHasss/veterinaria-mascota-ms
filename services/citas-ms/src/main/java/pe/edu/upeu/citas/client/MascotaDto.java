package pe.edu.upeu.citas.client;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal peso;
    private Boolean activo;

    /**
     * Falso solo cuando el Circuit Breaker está abierto y la respuesta
     * proviene del método de fallback, no del servicio real.
     */
    @Builder.Default
    private Boolean disponible = true;
}
