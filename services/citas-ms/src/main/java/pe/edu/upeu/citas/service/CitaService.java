package pe.edu.upeu.citas.service;

import pe.edu.upeu.citas.client.MascotaClientService;
import pe.edu.upeu.citas.client.MascotaDto;
import pe.edu.upeu.citas.dto.CitaRequest;
import pe.edu.upeu.citas.dto.CitaResponse;
import pe.edu.upeu.citas.entity.Cita;
import pe.edu.upeu.citas.exception.ResourceNotFoundException;
import pe.edu.upeu.citas.mapper.CitaMapper;
import pe.edu.upeu.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaService {

    public static final String ESTADO_PENDIENTE_VALIDACION = "PENDIENTE_VALIDACION";
    private static final String NOMBRE_NO_DISPONIBLE = "Pendiente de validación (servicio de mascotas no disponible)";

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MascotaClientService mascotaClientService;

    public List<CitaResponse> listar(Long idCliente) {
        return citaRepository.findByIdCliente(idCliente).stream()
                .map(citaMapper::toResponse)
                .toList();
    }

    public CitaResponse obtener(Long id, Long idCliente) {
        return citaMapper.toResponse(buscarPropia(id, idCliente));
    }

    public CitaResponse crear(Long idCliente, CitaRequest request) {
        log.info("Creando cita para idCliente={} (tomado del JWT), mascotaId={}, consultando veterinaria-mascota-ms...",
                idCliente, request.getMascotaId());
        MascotaDto mascota = validarMascota(request.getMascotaId());
        Cita cita = citaMapper.toEntity(request);
        cita.setIdCliente(idCliente);
        aplicarDatosMascota(cita, mascota, request.getEstado());
        Cita guardada = citaRepository.save(cita);
        log.info("Cita {} creada para idCliente={} con mascotaNombre='{}' y estado='{}'",
                guardada.getId(), guardada.getIdCliente(), guardada.getMascotaNombre(), guardada.getEstado());
        return citaMapper.toResponse(guardada);
    }

    public CitaResponse actualizar(Long id, Long idCliente, CitaRequest request) {
        Cita cita = buscarPropia(id, idCliente);
        MascotaDto mascota = validarMascota(request.getMascotaId());
        cita.setMascotaId(request.getMascotaId());
        cita.setMotivo(request.getMotivo());
        cita.setFechaHora(request.getFechaHora());
        aplicarDatosMascota(cita, mascota, request.getEstado());
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
