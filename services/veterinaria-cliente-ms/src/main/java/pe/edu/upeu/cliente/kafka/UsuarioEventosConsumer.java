package pe.edu.upeu.cliente.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.cliente.event.UsuarioRegistradoEvento;
import pe.edu.upeu.cliente.service.AltaClienteService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioEventosConsumer {

    private final AltaClienteService altaClienteService;

    @KafkaListener(topics = "${app.kafka.topics.usuario-eventos}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(ConsumerRecord<String, UsuarioRegistradoEvento> record) {
        UsuarioRegistradoEvento evento = record.value();
        if (evento == null) {
            log.warn("component=usuario-consumer eventType=desconocido usuarioId={} status=DESCARTADO motivo=mensaje_vacio partition={} offset={}",
                    record.key(), record.partition(), record.offset());
            return;
        }
        log.info("component=usuario-consumer eventType={} usuarioId={} status=RECIBIDO partition={} offset={}",
                evento.getTipoEvento(), evento.getUsuarioId(), record.partition(), record.offset());
        if (!UsuarioRegistradoEvento.TIPO.equals(evento.getTipoEvento())) {
            log.info("component=usuario-consumer eventType={} usuarioId={} status=IGNORADO motivo=tipo_no_soportado",
                    evento.getTipoEvento(), evento.getUsuarioId());
            return;
        }
        altaClienteService.procesar(evento);
    }
}
