package pe.edu.upeu.citas.service;

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

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;

    public List<CitaResponse> listar() {
        return citaRepository.findAll().stream()
                .map(citaMapper::toResponse)
                .toList();
    }

    public CitaResponse obtener(Long id) {
        return citaMapper.toResponse(buscarOFallar(id));
    }

    public CitaResponse crear(CitaRequest request) {
        Cita cita = citaMapper.toEntity(request);
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    public CitaResponse actualizar(Long id, CitaRequest request) {
        Cita cita = buscarOFallar(id);
        cita.setMascotaId(request.getMascotaId());
        cita.setMotivo(request.getMotivo());
        cita.setFechaHora(request.getFechaHora());
        cita.setEstado(request.getEstado());
        return citaMapper.toResponse(citaRepository.save(cita));
    }

    public void eliminar(Long id) {
        citaRepository.delete(buscarOFallar(id));
    }

    private Cita buscarOFallar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));
    }
}
