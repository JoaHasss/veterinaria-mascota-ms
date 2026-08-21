package pe.edu.upeu.veterinaria.repository;

import pe.edu.upeu.veterinaria.entity.Especie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecieRepository extends JpaRepository<Especie, Long> {
}
