package pe.edu.upeu.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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

    @NotBlank
    @Size(max = 20)
    private String estado;
}
