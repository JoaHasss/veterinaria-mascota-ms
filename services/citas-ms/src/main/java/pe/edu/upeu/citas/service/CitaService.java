package pe.edu.upeu.citas.service;

import pe.edu.upeu.citas.client.MascotaClientService;
import pe.edu.upeu.citas.client.MascotaDto;
import pe.edu.upeu.citas.dto.CitaRequest;
import pe.edu.upeu.citas.dto.CitaResponse;
import pe.edu.upeu.citas.entity.Cita;
import pe.edu.upeu.citas.exception.ResourceNotFoundException;
import pe.edu.upeu.citas.kafka.CitaEventosProducer;
import pe.edu.upeu.citas.mapper.CitaMapper;
import pe.edu.upeu.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaService {

    public static final String ESTADO_PENDIENTE_VALIDACION = "PENDIENTE_VALIDACION";
    public static final String ESTADO_PENDIENTE_PAGO = "PENDIENTE_PAGO";
    public static final String ESTADO_PAGADA = "PAGADA";
    private static final BigDecimal TARIFA_CONSULTA = new BigDecimal("80.00");
    private static final String METODO_PAGO_POR_DEFECTO = "EFECTIVO";
    private static final String NOMBRE_NO_DISPONIBLE = "Pendiente de validación (servicio de mascotas no disponible)";

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MascotaClientService mascotaClientService;
    private final CitaEventosProducer citaEventosProducer;

    public List<CitaResponse> listar(Long idCliente) {
        return citaRepository.findByIdCliente(idCliente).stream()
                .map(citaMapper::toResponse)
                .toList();
    }

    public CitaResponse obtener(Long id, Long idCliente) {
        return citaMapper.toResponse(buscarPropia(id, idCliente));
    }

    /**
     * Si la mascota se valido, la cita queda PENDIENTE_PAGO y se publica
     * cita.creada (despues del commit). No se espera a veterinaria-pago-ms:
     * la respuesta 201 sale aunque ese servicio este apagado.
     */
    @Transactional
    public CitaResponse crear(Long idCliente, CitaRequest request) {
        log.info("Creando cita para idCliente={} (tomado del JWT), mascotaId={}, consultando veterinaria-mascota-ms...",
                idCliente, request.getMascotaId());
        MascotaDto mascota = validarMascota(request.getMascotaId());
        Cita cita = citaMapper.toEntity(request);
        cita.setIdCliente(idCliente);
        cita.setMonto(request.getMonto() != null ? request.getMonto() : TARIFA_CONSULTA);
        cita.setMetodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : METODO_PAGO_POR_DEFECTO);
        aplicarDatosMascota(cita, mascota, ESTADO_PENDIENTE_PAGO);
        Cita guardada = citaRepository.save(cita);
        log.info("Cita {} creada para idCliente={} con mascotaNombre='{}' y estado='{}'",
                guardada.getId(), guardada.getIdCliente(), guardada.getMascotaNombre(), guardada.getEstado());
        if (ESTADO_PENDIENTE_PAGO.equals(guardada.getEstado())) {
            citaEventosProducer.publicarCitaCreada(guardada);
        }
        return citaMapper.toResponse(guardada);
    }

    /**
     * Reaccion al evento pago.validado. Solo una cita PENDIENTE_PAGO pasa a
     * PAGADA; si el evento llega repetido la cita ya esta PAGADA y no cambia
     * (consumo idempotente).
     */
    @Transactional
    public void marcarPagada(Long citaId, Long pagoId) {
        Cita cita = citaRepository.findById(citaId).orElse(null);
        if (cita == null) {
            log.warn("component=cita-service eventType=pago.validado citaId={} status=CITA_NO_ENCONTRADA", citaId);
            return;
        }
        if (!ESTADO_PENDIENTE_PAGO.equals(cita.getEstado())) {
            log.info("component=cita-service eventType=pago.validado citaId={} status=SIN_CAMBIO estadoActual={}",
                    citaId, cita.getEstado());
            return;
        }
        cita.setEstado(ESTADO_PAGADA);
        citaRepository.save(cita);
        log.info("component=cita-service eventType=pago.validado citaId={} status=CITA_PAGADA pagoId={} estado={}->{}",
                citaId, pagoId, ESTADO_PENDIENTE_PAGO, ESTADO_PAGADA);
    }

    public CitaResponse actualizar(Long id, Long idCliente, CitaRequest request) {
        Cita cita = buscarPropia(id, idCliente);
        MascotaDto mascota = validarMascota(request.getMascotaId());
        cita.setMascotaId(request.getMascotaId());
        cita.setMotivo(request.getMotivo());
        cita.setFechaHora(request.getFechaHora());
        aplicarDatosMascota(cita, mascota, request.getEstado() != null ? request.getEstado() : cita.getEstado());
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    /**
     * Consulta la mascota vía Feign/Circuit Breaker. Si el servicio no existe
     * o está inactiva (respuesta real del servicio), se rechaza la operación.
     * Si el Circuit Breaker está abierto (servicio caído), NO se rechaza:
     * se degrada con gracia dejando la cita en estado pendiente de validación.
     */
    private MascotaDto validarMascota(Long mascotaId) {
        MascotaDto mascota = mascotaClientService.obtener(mascotaId);
        if (Boolean.FALSE.equals(mascota.getDisponible())) {
            return mascota;
        }
        if (Boolean.FALSE.equals(mascota.getActivo())) {
            throw new ResourceNotFoundException("La mascota " + mascotaId + " no existe o no está activa");
        }
        return mascota;
    }

    private void aplicarDatosMascota(Cita cita, MascotaDto mascota, String estadoSolicitado) {
        if (Boolean.FALSE.equals(mascota.getDisponible())) {
            cita.setMascotaNombre(NOMBRE_NO_DISPONIBLE);
            cita.setEstado(ESTADO_PENDIENTE_VALIDACION);
        } else {
            cita.setMascotaNombre(mascota.getNombre());
            cita.setEstado(estadoSolicitado);
        }
    }

    public void eliminar(Long id, Long idCliente) {
        citaRepository.delete(buscarPropia(id, idCliente));
    }

    /**
     * ABAC: además del rol CLIENTE, el idCliente del token debe coincidir
     * con el dueño de la cita; si no, 403.
     */
    private Cita buscarPropia(Long id, Long idCliente) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));
        if (!idCliente.equals(cita.getIdCliente())) {
            log.warn("Acceso denegado: idCliente={} intentó acceder a la cita {} de idCliente={}",
                    idCliente, id, cita.getIdCliente());
            throw new AccessDeniedException("La cita " + id + " no pertenece al cliente autenticado");
        }
        return cita;
    }
}
