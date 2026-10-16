package pe.edu.upeu.pago.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    /** Valor reservado para simular, de forma determinista, el rechazo de la pasarela. */
    static final String METODO_RECHAZADO = "TARJETA_RECHAZADA";
    private static final String MOTIVO_RECHAZO = "Tarjeta rechazada por la pasarela (simulada)";

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
     * Consumidor idempotente (S9). Dos capas:
     * 1) revision explicita: si ya existe un pago para la cita, el evento es un
     *    duplicado (reentrega de Kafka) y se ignora sin volver a publicar nada;
     * 2) respaldo en BD: uk_pagos_cita_id rechaza el segundo INSERT si dos
     *    entregas pasan la revision a la vez (condicion de carrera).
     * No es @Transactional a proposito: save() confirma en su propia transaccion,
     * asi la violacion UNIQUE se captura aqui sin dejar una transaccion externa
     * marcada como rollback-only, y el evento sale solo despues de ese commit.
     * La pasarela se simula: metodoPago TARJETA_RECHAZADA siempre se rechaza.
     */
    @Override
    public void procesar(CitaCreadaEvento evento) {
        Pago existente = pagoRepository.findByCitaId(evento.getCitaId()).orElse(null);
        if (existente != null) {
            log.warn("component=pago-service eventType={} citaId={} status=IGNORADO motivo=\"pago ya procesado (evento duplicado)\" pagoId={} estado={}",
                    evento.getTipoEvento(), evento.getCitaId(), existente.getId(), existente.getEstado());
            return;
        }

        boolean rechazado = METODO_RECHAZADO.equalsIgnoreCase(evento.getMetodoPago());
        Pago pago;
        try {
            pago = pagoRepository.save(Pago.builder()
                    .citaId(evento.getCitaId())
                    .idCliente(evento.getIdCliente())
                    .monto(evento.getMonto())
                    .metodoPago(evento.getMetodoPago())
                    .estado(rechazado ? EstadoPago.FALLIDO : EstadoPago.VALIDADO)
                    .fechaPago(LocalDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException ex) {
            log.warn("component=pago-service eventType={} citaId={} status=IGNORADO motivo=\"pago ya procesado (condicion de carrera, uk_pagos_cita_id)\"",
                    evento.getTipoEvento(), evento.getCitaId());
            return;
        }

        String motivo = rechazado ? MOTIVO_RECHAZO : null;
        log.info("component=pago-service eventType={} citaId={} status=PROCESADO pagoId={} estado={} monto={} metodoPago={}",
                evento.getTipoEvento(), evento.getCitaId(), pago.getId(), pago.getEstado(), pago.getMonto(), pago.getMetodoPago());
        pagoEventosProducer.publicarResultado(pago, motivo);
    }
}
