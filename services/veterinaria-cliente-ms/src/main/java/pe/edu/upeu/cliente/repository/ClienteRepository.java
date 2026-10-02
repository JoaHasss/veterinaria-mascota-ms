package pe.edu.upeu.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.cliente.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);
}
