package pe.edu.upeu.citas.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascotaClientService {

    private final MascotaClient mascotaClient;

    @CircuitBreaker(name = "mascotaService", fallbackMethod = "fallbackObtener")
    public MascotaDto obtener(Long mascotaId) {
        log.info("Consultando veterinaria-mascota-ms para mascotaId={} (circuit breaker CLOSED/HALF_OPEN)", mascotaId);
        MascotaDto mascota = mascotaClient.obtener(mascotaId);
        log.info("Respuesta de veterinaria-mascota-ms para mascotaId={}: nombre='{}', activo={}",
                mascotaId, mascota.getNombre(), mascota.getActivo());
        return mascota;
    }

    /**
     * Degradación controlada: en vez de propagar el error al cliente,
     * se responde con un marcador "no disponible" para que el llamador
     * decida cómo continuar (p. ej. dejar la cita en estado pendiente
     * de validación) sin interrumpir la operación en curso.
     */
    private MascotaDto fallbackObtener(Long mascotaId, Throwable throwable) {
        log.warn("Circuit breaker 'mascotaService' activo. No se pudo consultar la mascota {}: {}",
                mascotaId, throwable.toString());
        return MascotaDto.builder()
                .id(mascotaId)
                .disponible(false)
                .build();
    }
}
