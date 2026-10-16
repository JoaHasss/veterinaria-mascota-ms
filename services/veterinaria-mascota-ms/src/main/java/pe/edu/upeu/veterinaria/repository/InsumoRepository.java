package pe.edu.upeu.veterinaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.veterinaria.entity.Insumo;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
}
