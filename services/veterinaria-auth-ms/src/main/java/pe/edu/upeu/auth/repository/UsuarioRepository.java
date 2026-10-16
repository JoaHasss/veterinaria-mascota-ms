package pe.edu.upeu.auth.repository;

import pe.edu.upeu.auth.entity.EstadoUsuario;
import pe.edu.upeu.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Cuenta vigente del email. Desde S9 puede haber filas RECHAZADAS con el
     * mismo email (rastro de compensaciones); el indice parcial garantiza que
     * a lo sumo una no este RECHAZADA.
     */
    Optional<Usuario> findByEmailAndEstadoNot(String email, EstadoUsuario estado);

    default Optional<Usuario> findVigenteByEmail(String email) {
        return findByEmailAndEstadoNot(email, EstadoUsuario.RECHAZADO);
    }
}
