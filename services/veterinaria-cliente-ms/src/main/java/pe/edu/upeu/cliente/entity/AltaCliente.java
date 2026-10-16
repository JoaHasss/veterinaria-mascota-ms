package pe.edu.upeu.cliente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** S9: un evento usuario.registrado ya procesado, con su resultado. */
@Entity
@Table(name = "altas_cliente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AltaCliente {

    public enum Resultado { CREADO, RECHAZADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Resultado resultado;

    @Column(length = 255)
    private String motivo;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "fecha_proceso", nullable = false)
    private LocalDateTime fechaProceso;
}
