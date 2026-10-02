package pe.edu.upeu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
