package pe.edu.upeu.veterinaria.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspecieResponse {
    private Long id;
    private String nombre;
    private String descripcion;
}
