package pe.edu.upeu.menu.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaMenuResponse {
    private Long id;
    private String nombre;
    private String descripcion;
}