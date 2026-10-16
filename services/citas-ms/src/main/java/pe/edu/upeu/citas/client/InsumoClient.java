package pe.edu.upeu.citas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * S9 (Parte E): reserva y devolucion de stock de insumos clinicos en
 * veterinaria-mascota-ms. PATCH necesita feign-hc5: el cliente HTTP por
 * defecto (HttpURLConnection del JDK) no soporta ese metodo.
 */
@FeignClient(name = "veterinaria-mascota-ms", contextId = "insumoClient", path = "/api/v1/insumos")
public interface InsumoClient {

    @PatchMapping("/{id}/descontar-stock")
    void descontarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);

    @PatchMapping("/{id}/restaurar-stock")
    void restaurarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);
}
