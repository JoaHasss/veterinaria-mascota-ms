package pe.edu.upeu.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unico solo entre cuentas no RECHAZADAS (indice parcial uk_usuarios_email_vigente). */
    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private boolean habilitado = true;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();
}
