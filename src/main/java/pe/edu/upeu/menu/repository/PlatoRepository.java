package pe.edu.upeu.menu.repository;

import pe.edu.upeu.menu.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Long> {

    @Query("SELECT p FROM Plato p JOIN FETCH p.categoriaMenu")
    List<Plato> findAllConCategoriaMenu();
}