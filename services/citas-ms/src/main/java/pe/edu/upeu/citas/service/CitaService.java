package pe.edu.upeu.citas.service;

import pe.edu.upeu.citas.client.InsumoClientService;
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
    /** S9: estado final de la compensacion (pago rechazado). */
    public static final String ESTADO_CANCELADA = "CANCELADA";
    /** S9 (Parte E): el insumo pedido no se pudo reservar; la cita no avanza a pago. */
    public static final String ESTADO_PENDIENTE_INSUMO = "PENDIENTE_INSUMO";
    private static final BigDecimal TARIFA_CONSULTA = new BigDecimal("80.00");
    private static final String METODO_PAGO_POR_DEFECTO = "EFECTIVO";
    private static final String NOMBRE_NO_DISPONIBLE = "Pendiente de validación (servicio de mascotas no disponible)";

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MascotaClientService mascotaClientService;
    private final CitaEventosProducer citaEventosProducer;
    private final InsumoClientService insumoClientService;

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
        reservarInsumo(cita, request);
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

    /**
     * S9: paso compensatorio de la Saga coreografiada, reaccion a pago.fallido.
     * Misma guardia de idempotencia que marcarPagada: solo una cita que sigue
     * PENDIENTE_PAGO se compensa; un pago.fallido repetido se ignora. No borra
     * la cita: la pasa a CANCELADA (rastro auditable) y devuelve el insumo
     * reservado al crearla (Parte E).
     */
    @Transactional
    public void compensar(Long citaId, Long pagoId, String motivo) {
        Cita cita = citaRepository.findById(citaId).orElse(null);
        if (cita == null || !ESTADO_PENDIENTE_PAGO.equals(cita.getEstado())) {
            log.warn("component=cita-service eventType=pago.fallido citaId={} status=IGNORADO motivo=\"la cita no existe o ya no esta pendiente de pago\" estadoActual={}",
                    citaId, cita == null ? "-" : cita.getEstado());
            return;
        }
        if (cita.getInsumoId() != null && cita.getCantidadInsumo() != null) {
            insumoClientService.restaurarStock(cita.getInsumoId(), cita.getCantidadInsumo());
        }
        cita.setEstado(ESTADO_CANCELADA);
        citaRepository.save(cita);
        log.info("component=cita-service eventType=pago.fallido citaId={} status=COMPENSADA pagoId={} estado={}->{} motivoPago=\"{}\"",
                citaId, pagoId, ESTADO_PENDIENTE_PAGO, ESTADO_CANCELADA, motivo);
    }

    /**
     * S9 (Parte E): si la cita pide un insumo y la mascota ya se valido, se
     * descuenta su stock en veterinaria-mascota-ms. Si no alcanza (409) o el
     * servicio no responde, la cita queda PENDIENTE_INSUMO y no se publica
     * cita.creada: no se cobra una atencion sin el insumo reservado.
     */
    private void reservarInsumo(Cita cita, CitaRequest request) {
        if (request.getInsumoId() == null || !ESTADO_PENDIENTE_PAGO.equals(cita.getEstado())) {
            return;
        }
        int cantidad = request.getCantidadInsumo() != null ? request.getCantidadInsumo() : 1;
        cita.setInsumoId(request.getInsumoId());
        cita.setCantidadInsumo(cantidad);
        if (!insumoClientService.descontarStock(request.getInsumoId(), cantidad)) {
            cita.setEstado(ESTADO_PENDIENTE_INSUMO);
            cita.setCantidadInsumo(null);
        }
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
