package pe.edu.upeu.menu.mapper;

import pe.edu.upeu.menu.dto.PlatoRequest;
import pe.edu.upeu.menu.dto.PlatoResponse;
import pe.edu.upeu.menu.entity.Plato;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlatoMapper {

    private final CategoriaMenuMapper categoriaMenuMapper;

    public Plato toEntity(PlatoRequest request) {
        return Plato.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .activo(request.getActivo())
                .build();
    }

    public PlatoResponse toResponse(Plato plato) {
        return PlatoResponse.builder()
                .id(plato.getId())
                .nombre(plato.getNombre())
                .descripcion(plato.getDescripcion())
                .precio(plato.getPrecio())
                .activo(plato.getActivo())
                .categoriaMenu(categoriaMenuMapper.toResponse(plato.getCategoriaMenu()))
                .build();
    }
}