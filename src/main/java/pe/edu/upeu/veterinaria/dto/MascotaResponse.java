package pe.edu.upeu.veterinaria.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal peso;
    private Boolean activo;
    private EspecieResponse especie;
}
