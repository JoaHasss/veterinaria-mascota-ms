package pe.edu.upeu.citas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "veterinaria-mascota-ms", path = "/api/v1/mascotas")
public interface MascotaClient {

    @GetMapping("/{id}")
    MascotaDto obtener(@PathVariable Long id);
}
