package pe.edu.upeu.menu.mapper;

import pe.edu.upeu.menu.dto.CategoriaMenuRequest;
import pe.edu.upeu.menu.dto.CategoriaMenuResponse;
import pe.edu.upeu.menu.entity.CategoriaMenu;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMenuMapper {

    public CategoriaMenu toEntity(CategoriaMenuRequest request) {
        return CategoriaMenu.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
    }

    public CategoriaMenuResponse toResponse(CategoriaMenu categoriaMenu) {
        return CategoriaMenuResponse.builder()
                .id(categoriaMenu.getId())
                .nombre(categoriaMenu.getNombre())
                .descripcion(categoriaMenu.getDescripcion())
                .build();
    }
}