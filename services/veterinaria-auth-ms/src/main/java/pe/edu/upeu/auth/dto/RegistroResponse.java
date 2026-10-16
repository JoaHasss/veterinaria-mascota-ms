package pe.edu.upeu.auth.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroResponse {
    private Long usuarioId;
    private String email;
    private String estado;
    private Long idCliente;
    private LocalDateTime fechaRegistro;
}
