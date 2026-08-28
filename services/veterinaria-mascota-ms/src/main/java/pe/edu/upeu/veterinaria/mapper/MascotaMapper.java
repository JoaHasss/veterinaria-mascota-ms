package pe.edu.upeu.veterinaria.mapper;

import pe.edu.upeu.veterinaria.dto.MascotaRequest;
import pe.edu.upeu.veterinaria.dto.MascotaResponse;
import pe.edu.upeu.veterinaria.entity.Mascota;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MascotaMapper {

    private final EspecieMapper especieMapper;

    public Mascota toEntity(MascotaRequest request) {
        return Mascota.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .peso(request.getPeso())
                .activo(request.getActivo())
                .build();
    }

    public MascotaResponse toResponse(Mascota mascota) {
        return MascotaResponse.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .descripcion(mascota.getDescripcion())
                .peso(mascota.getPeso())
                .activo(mascota.getActivo())
                .especie(especieMapper.toResponse(mascota.getEspecie()))
                .build();
    }
}
