package pe.edu.upeu.menu.service;

import pe.edu.upeu.menu.dto.PlatoRequest;
import pe.edu.upeu.menu.dto.PlatoResponse;
import pe.edu.upeu.menu.entity.CategoriaMenu;
import pe.edu.upeu.menu.entity.Plato;
import pe.edu.upeu.menu.exception.ResourceNotFoundException;
import pe.edu.upeu.menu.mapper.PlatoMapper;
import pe.edu.upeu.menu.repository.CategoriaMenuRepository;
import pe.edu.upeu.menu.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatoService {

    private final PlatoRepository platoRepository;
    private final PlatoMapper platoMapper;
    private final CategoriaMenuRepository categoriaMenuRepository;

    public List<PlatoResponse> listar() {
        return platoRepository.findAllConCategoriaMenu().stream()
                .map(platoMapper::toResponse)
                .toList();
    }

    public PlatoResponse obtener(Long id) {
        return platoMapper.toResponse(buscarOFallar(id));
    }

    public PlatoResponse crear(PlatoRequest request) {
        Plato plato = platoMapper.toEntity(request);
        plato.setCategoriaMenu(buscarCategoriaMenuOFallar(request.getCategoriaMenuId()));
        return platoMapper.toResponse(platoRepository.save(plato));
    }

    public PlatoResponse actualizar(Long id, PlatoRequest request) {
        Plato plato = buscarOFallar(id);
        plato.setNombre(request.getNombre());
        plato.setDescripcion(request.getDescripcion());
        plato.setPrecio(request.getPrecio());
        plato.setActivo(request.getActivo());
        plato.setCategoriaMenu(buscarCategoriaMenuOFallar(request.getCategoriaMenuId()));
        return platoMapper.toResponse(platoRepository.save(plato));
    }

    public void eliminar(Long id) {
        platoRepository.delete(buscarOFallar(id));
    }

    private Plato buscarOFallar(Long id) {
        return platoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + id));
    }

    private CategoriaMenu buscarCategoriaMenuOFallar(Long categoriaMenuId) {
        return categoriaMenuRepository.findById(categoriaMenuId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría del menú no encontrada: " + categoriaMenuId));
    }
}