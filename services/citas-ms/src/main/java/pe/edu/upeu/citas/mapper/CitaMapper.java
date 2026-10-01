package pe.edu.upeu.citas.mapper;

import pe.edu.upeu.citas.dto.CitaRequest;
import pe.edu.upeu.citas.dto.CitaResponse;
import pe.edu.upeu.citas.entity.Cita;
import org.springframework.stereotype.Component;

@Component
public class CitaMapper {

    public Cita toEntity(CitaRequest request) {
        return Cita.builder()
                .mascotaId(request.getMascotaId())
                .motivo(request.getMotivo())
                .fechaHora(request.getFechaHora())
                .estado(request.getEstado())
                .build();
    }

    public CitaResponse toResponse(Cita cita) {
        return CitaResponse.builder()
                .id(cita.getId())
                .mascotaId(cita.getMascotaId())
                .mascotaNombre(cita.getMascotaNombre())
                .motivo(cita.getMotivo())
                .fechaHora(cita.getFechaHora())
                .estado(cita.getEstado())
                .build();
    }
}
