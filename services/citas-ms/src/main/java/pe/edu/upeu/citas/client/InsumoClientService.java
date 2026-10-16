package pe.edu.upeu.citas.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mismo Circuit Breaker que la consulta de mascotas (S6): es el mismo
 * servicio remoto, si esta caido lo esta para las dos operaciones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsumoClientService {

    private final InsumoClient insumoClient;

    /**
     * Devuelve true si el stock quedo reservado. El motivo de un fallo
     * (409 por stock insuficiente, servicio caido, circuito abierto) no le
     * importa a CitaService: solo necesita saber si la reserva se hizo.
     */
    @CircuitBreaker(name = "mascotaService", fallbackMethod = "fallbackDescontar")
    public boolean descontarStock(Long insumoId, Integer cantidad) {
        insumoClient.descontarStock(insumoId, cantidad);
        log.info("component=insumo-client insumoId={} cantidad={} status=STOCK_RESERVADO", insumoId, cantidad);
        return true;
    }

    private boolean fallbackDescontar(Long insumoId, Integer cantidad, Throwable ex) {
        log.warn("component=insumo-client insumoId={} cantidad={} status=RESERVA_FALLIDA motivo=\"{}\"",
                insumoId, cantidad, ex.getMessage());
        return false;
    }

    @CircuitBreaker(name = "mascotaService", fallbackMethod = "fallbackRestaurar")
    public void restaurarStock(Long insumoId, Integer cantidad) {
        insumoClient.restaurarStock(insumoId, cantidad);
        log.info("component=insumo-client insumoId={} cantidad={} status=STOCK_RESTAURADO", insumoId, cantidad);
    }

    /**
     * No relanza la excepcion a proposito: la cita queda CANCELADA igual y el
     * stock queda desincronizado hasta una correccion manual (limitacion conocida).
     */
    private void fallbackRestaurar(Long insumoId, Integer cantidad, Throwable ex) {
        log.error("component=insumo-client insumoId={} cantidad={} status=RESTAURACION_FALLIDA motivo=\"{}\" accion=requiere_correccion_manual",
                insumoId, cantidad, ex.getMessage());
    }
}
