package pe.edu.upeu.citas.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaResponse {
    private Long id;
    private Long mascotaId;
    private String mascotaNombre;
    private String motivo;
    private LocalDateTime fechaHora;
    private String estado;
}
