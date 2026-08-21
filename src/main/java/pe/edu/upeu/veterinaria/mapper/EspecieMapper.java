package pe.edu.upeu.veterinaria.mapper;

import pe.edu.upeu.veterinaria.dto.EspecieRequest;
import pe.edu.upeu.veterinaria.dto.EspecieResponse;
import pe.edu.upeu.veterinaria.entity.Especie;
import org.springframework.stereotype.Component;

@Component
public class EspecieMapper {

    public Especie toEntity(EspecieRequest request) {
        return Especie.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
    }

    public EspecieResponse toResponse(Especie especie) {
        return EspecieResponse.builder()
                .id(especie.getId())
                .nombre(especie.getNombre())
                .descripcion(especie.getDescripcion())
                .build();
    }
}
