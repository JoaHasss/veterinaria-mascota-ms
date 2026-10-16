package pe.edu.upeu.cliente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pe.edu.upeu.cliente.entity.AltaCliente;
import pe.edu.upeu.cliente.event.UsuarioRegistradoEvento;
import pe.edu.upeu.cliente.kafka.ClienteEventosProducer;
import pe.edu.upeu.cliente.repository.AltaClienteRepository;

/**
 * S9: consumidor idempotente del paso 2 de la saga de alta de cliente.
 * Capa 1: revision explicita en altas_cliente (el evento ya se proceso).
 * Capa 2: uk_altas_cliente_usuario rechaza el segundo INSERT si dos entregas
 * pasan la revision al mismo tiempo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AltaClienteService {

    private final AltaClienteRepository altaClienteRepository;
    private final AltaClienteTx altaClienteTx;
    private final ClienteEventosProducer clienteEventosProducer;

    public void procesar(UsuarioRegistradoEvento evento) {
        AltaCliente previa = altaClienteRepository.findByUsuarioId(evento.getUsuarioId()).orElse(null);
        if (previa != null) {
            log.warn("component=alta-cliente-service eventType={} usuarioId={} status=IGNORADO motivo=\"alta ya procesada (evento duplicado)\" resultadoPrevio={}",
                    evento.getTipoEvento(), evento.getUsuarioId(), previa.getResultado());
            return;
        }

        AltaCliente alta;
        try {
            alta = altaClienteTx.registrar(evento);
        } catch (DataIntegrityViolationException ex) {
            if (altaClienteRepository.findByUsuarioId(evento.getUsuarioId()).isPresent()) {
                log.warn("component=alta-cliente-service eventType={} usuarioId={} status=IGNORADO motivo=\"alta ya procesada (condicion de carrera, uk_altas_cliente_usuario)\"",
                        evento.getTipoEvento(), evento.getUsuarioId());
                return;
            }
            throw ex;
        }

        log.info("component=alta-cliente-service eventType={} usuarioId={} status=PROCESADO resultado={} clienteId={} email={}",
                evento.getTipoEvento(), evento.getUsuarioId(), alta.getResultado(), alta.getClienteId(), alta.getEmail());
        // La transaccion local ya hizo commit: el evento sale despues, nunca antes.
        clienteEventosProducer.publicarResultado(alta);
    }
}
