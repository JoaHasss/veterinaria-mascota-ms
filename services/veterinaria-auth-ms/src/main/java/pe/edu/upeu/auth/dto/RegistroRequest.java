package pe.edu.upeu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * S9: el registro tambien trae los datos del perfil; viajan en el evento
 * usuario.registrado para que veterinaria-cliente-ms cree el cliente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Size(max = 100)
    private String nombres;

    @NotBlank
    @Size(max = 100)
    private String apellidos;

    @Size(max = 20)
    private String telefono;

    @Size(max = 255)
    private String direccion;
}
