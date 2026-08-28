package pe.edu.upeu.veterinaria.service;

import pe.edu.upeu.veterinaria.dto.MascotaRequest;
import pe.edu.upeu.veterinaria.dto.MascotaResponse;
import pe.edu.upeu.veterinaria.entity.Especie;
import pe.edu.upeu.veterinaria.entity.Mascota;
import pe.edu.upeu.veterinaria.exception.ResourceNotFoundException;
import pe.edu.upeu.veterinaria.mapper.MascotaMapper;
import pe.edu.upeu.veterinaria.repository.EspecieRepository;
import pe.edu.upeu.veterinaria.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final MascotaMapper mascotaMapper;
    private final EspecieRepository especieRepository;

    public List<MascotaResponse> listar() {
        return mascotaRepository.findAllConEspecie().stream()
                .map(mascotaMapper::toResponse)
                .toList();
    }

    public MascotaResponse obtener(Long id) {
        return mascotaMapper.toResponse(buscarOFallar(id));
    }

    public MascotaResponse crear(MascotaRequest request) {
        Mascota mascota = mascotaMapper.toEntity(request);
        mascota.setEspecie(buscarEspecieOFallar(request.getEspecieId()));
        return mascotaMapper.toResponse(mascotaRepository.save(mascota));
    }

    public MascotaResponse actualizar(Long id, MascotaRequest request) {
        Mascota mascota = buscarOFallar(id);
        mascota.setNombre(request.getNombre());
        mascota.setDescripcion(request.getDescripcion());
        mascota.setPeso(request.getPeso());
        mascota.setActivo(request.getActivo());
        mascota.setEspecie(buscarEspecieOFallar(request.getEspecieId()));
        return mascotaMapper.toResponse(mascotaRepository.save(mascota));
    }

    public void eliminar(Long id) {
        mascotaRepository.delete(buscarOFallar(id));
    }

    private Mascota buscarOFallar(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada: " + id));
    }

    private Especie buscarEspecieOFallar(Long especieId) {
        return especieRepository.findById(especieId)
                .orElseThrow(() -> new ResourceNotFoundException("Especie no encontrada: " + especieId));
    }
}
