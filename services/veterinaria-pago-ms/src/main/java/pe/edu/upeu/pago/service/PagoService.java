package pe.edu.upeu.pago.service;

import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.event.CitaCreadaEvento;

import java.util.List;
import java.util.Optional;

public interface PagoService {

    List<PagoResponse> listar();

    PagoResponse obtener(Long id);

    Optional<PagoResponse> buscarPorCita(Long citaId);

    void procesar(CitaCreadaEvento evento);
}
