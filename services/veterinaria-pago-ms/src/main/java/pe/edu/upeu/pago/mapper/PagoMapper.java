package pe.edu.upeu.pago.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.entity.Pago;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    PagoResponse toResponse(Pago pago);

    List<PagoResponse> toResponseList(List<Pago> pagos);
}
