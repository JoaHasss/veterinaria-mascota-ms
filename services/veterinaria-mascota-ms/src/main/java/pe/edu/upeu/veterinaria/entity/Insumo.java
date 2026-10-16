package pe.edu.upeu.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "insumos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "stock", nullable = false)
    private Integer stock;
}
