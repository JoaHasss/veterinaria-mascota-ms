package pe.edu.upeu.veterinaria.repository;

import pe.edu.upeu.veterinaria.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    @Query("SELECT m FROM Mascota m JOIN FETCH m.especie")
    List<Mascota> findAllConEspecie();
}
