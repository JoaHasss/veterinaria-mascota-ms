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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    public static final String ESTADO_PENDIENTE_VALIDACION = "PENDIENTE_VALIDACION";
    private static final String NOMBRE_NO_DISPONIBLE = "Pendiente de validación (servicio de mascotas no disponible)";

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MascotaClientService mascotaClientService;

    public List<CitaResponse> listar() {
        return citaRepository.findAll().stream()
                .map(citaMapper::toResponse)
                .toList();
    }

    public CitaResponse obtener(Long id) {
        return citaMapper.toResponse(buscarOFallar(id));
    }

    public CitaResponse crear(CitaRequest request) {
        MascotaDto mascota = validarMascota(request.getMascotaId());
        Cita cita = citaMapper.toEntity(request);
        aplicarDatosMascota(cita, mascota, request.getEstado());
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    public CitaResponse actualizar(Long id, CitaRequest request) {
        Cita cita = buscarOFallar(id);
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

    public void eliminar(Long id) {
        citaRepository.delete(buscarOFallar(id));
    }

    private Cita buscarOFallar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));
    }
}
