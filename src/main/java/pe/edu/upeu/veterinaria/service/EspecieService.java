package pe.edu.upeu.veterinaria.service;

import pe.edu.upeu.veterinaria.dto.EspecieRequest;
import pe.edu.upeu.veterinaria.dto.EspecieResponse;
import pe.edu.upeu.veterinaria.entity.Especie;
import pe.edu.upeu.veterinaria.exception.ResourceNotFoundException;
import pe.edu.upeu.veterinaria.mapper.EspecieMapper;
import pe.edu.upeu.veterinaria.repository.EspecieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecieService {

    private final EspecieRepository especieRepository;
    private final EspecieMapper especieMapper;

    public List<EspecieResponse> listar() {
        return especieRepository.findAll().stream()
                .map(especieMapper::toResponse)
                .toList();
    }

    public EspecieResponse obtener(Long id) {
        return especieMapper.toResponse(buscarOFallar(id));
    }

    public EspecieResponse crear(EspecieRequest request) {
        Especie especie = especieMapper.toEntity(request);
        return especieMapper.toResponse(especieRepository.save(especie));
    }

    public EspecieResponse actualizar(Long id, EspecieRequest request) {
        Especie especie = buscarOFallar(id);
        especie.setNombre(request.getNombre());
        especie.setDescripcion(request.getDescripcion());
        return especieMapper.toResponse(especieRepository.save(especie));
    }

    public void eliminar(Long id) {
        especieRepository.delete(buscarOFallar(id));
    }

    private Especie buscarOFallar(Long id) {
        return especieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especie no encontrada: " + id));
    }
}
