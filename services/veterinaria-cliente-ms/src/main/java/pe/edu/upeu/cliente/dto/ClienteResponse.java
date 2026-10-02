package pe.edu.upeu.cliente.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String direccion;
    private Boolean activo;
}
