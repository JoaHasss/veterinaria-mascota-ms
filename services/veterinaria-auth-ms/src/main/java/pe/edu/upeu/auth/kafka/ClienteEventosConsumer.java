package pe.edu.upeu.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.auth.event.ClienteResultadoEvento;
import pe.edu.upeu.auth.service.AuthService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventosConsumer {

    private final AuthService authService;

    @KafkaListener(topics = "${app.kafka.topics.cliente-eventos}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(ConsumerRecord<String, ClienteResultadoEvento> record) {
        ClienteResultadoEvento evento = record.value();
        if (evento == null) {
            log.warn("component=cliente-consumer eventType=desconocido usuarioId={} status=DESCARTADO motivo=mensaje_vacio partition={} offset={}",
                    record.key(), record.partition(), record.offset());
            return;
        }
        log.info("component=cliente-consumer eventType={} usuarioId={} status=RECIBIDO partition={} offset={}",
                evento.getTipoEvento(), evento.getUsuarioId(), record.partition(), record.offset());
        switch (evento.getTipoEvento()) {
            case ClienteResultadoEvento.TIPO_CREADO -> authService.activar(evento.getUsuarioId(), evento.getClienteId());
            case ClienteResultadoEvento.TIPO_RECHAZADO -> authService.compensarRegistro(evento.getUsuarioId(), evento.getMotivo());
            case null, default -> log.info("component=cliente-consumer eventType={} usuarioId={} status=IGNORADO motivo=tipo_no_soportado",
                    evento.getTipoEvento(), evento.getUsuarioId());
        }
    }
}
