package pe.edu.upeu.citas.repository;

import pe.edu.upeu.citas.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {
}
