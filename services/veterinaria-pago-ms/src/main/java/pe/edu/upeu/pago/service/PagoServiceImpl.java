package pe.edu.upeu.pago.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.entity.EstadoPago;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.CitaCreadaEvento;
import pe.edu.upeu.pago.exception.ResourceNotFoundException;
import pe.edu.upeu.pago.kafka.PagoEventosProducer;
import pe.edu.upeu.pago.mapper.PagoMapper;
import pe.edu.upeu.pago.repository.PagoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PagoMapper pagoMapper;
    private final PagoEventosProducer pagoEventosProducer;

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoMapper.toResponseList(pagoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        return pagoRepository.findById(id)
                .map(pagoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
    }

    /**
     * Registra el pago de la cita y publica pago.validado. La publicacion se
     * difiere hasta el commit (afterCommit en el productor) para no anunciar
     * un pago que luego se revierta. Si el evento llega repetido no se crea
     * un segundo pago: se reutiliza el existente y se vuelve a anunciar.
     */
    @Override
    @Transactional
    public void procesar(CitaCreadaEvento evento) {
        Pago pago = pagoRepository.findByCitaId(evento.getCitaId()).orElse(null);
        if (pago != null) {
            log.warn("component=pago-service eventType={} citaId={} status=DUPLICADO pagoId={}",
                    evento.getTipoEvento(), evento.getCitaId(), pago.getId());
        } else {
            pago = pagoRepository.save(Pago.builder()
                    .citaId(evento.getCitaId())
                    .idCliente(evento.getIdCliente())
                    .monto(evento.getMonto())
                    .metodoPago(evento.getMetodoPago())
                    .estado(EstadoPago.VALIDADO)
                    .fechaPago(LocalDateTime.now())
                    .build());
            log.info("component=pago-service eventType={} citaId={} status=PAGO_REGISTRADO pagoId={} monto={} metodoPago={}",
                    evento.getTipoEvento(), evento.getCitaId(), pago.getId(), pago.getMonto(), pago.getMetodoPago());
        }
        pagoEventosProducer.publicarPagoValidado(pago);
    }
}
