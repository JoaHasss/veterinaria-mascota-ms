package pe.edu.upeu.pago.service;

import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.event.CitaCreadaEvento;

import java.util.List;

public interface PagoService {

    List<PagoResponse> listar();

    PagoResponse obtener(Long id);

    void procesar(CitaCreadaEvento evento);
}
