package pe.edu.upeu.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.cliente.entity.AltaCliente;

import java.util.Optional;

public interface AltaClienteRepository extends JpaRepository<AltaCliente, Long> {
    Optional<AltaCliente> findByUsuarioId(Long usuarioId);
}
