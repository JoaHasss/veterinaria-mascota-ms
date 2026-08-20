package pe.edu.upeu.menu.service;

import pe.edu.upeu.menu.dto.CategoriaMenuRequest;
import pe.edu.upeu.menu.dto.CategoriaMenuResponse;
import pe.edu.upeu.menu.entity.CategoriaMenu;
import pe.edu.upeu.menu.exception.ResourceNotFoundException;
import pe.edu.upeu.menu.mapper.CategoriaMenuMapper;
import pe.edu.upeu.menu.repository.CategoriaMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaMenuService {

    private final CategoriaMenuRepository categoriaMenuRepository;
    private final CategoriaMenuMapper categoriaMenuMapper;

    public List<CategoriaMenuResponse> listar() {
        return categoriaMenuRepository.findAll().stream()
                .map(categoriaMenuMapper::toResponse)
                .toList();
    }

    public CategoriaMenuResponse obtener(Long id) {
        return categoriaMenuMapper.toResponse(buscarOFallar(id));
    }

    public CategoriaMenuResponse crear(CategoriaMenuRequest request) {
        CategoriaMenu categoriaMenu = categoriaMenuMapper.toEntity(request);
        return categoriaMenuMapper.toResponse(categoriaMenuRepository.save(categoriaMenu));
    }

    public CategoriaMenuResponse actualizar(Long id, CategoriaMenuRequest request) {
        CategoriaMenu categoriaMenu = buscarOFallar(id);
        categoriaMenu.setNombre(request.getNombre());
        categoriaMenu.setDescripcion(request.getDescripcion());
        return categoriaMenuMapper.toResponse(categoriaMenuRepository.save(categoriaMenu));
    }

    public void eliminar(Long id) {
        categoriaMenuRepository.delete(buscarOFallar(id));
    }

    private CategoriaMenu buscarOFallar(Long id) {
        return categoriaMenuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría del menú no encontrada: " + id));
    }
}