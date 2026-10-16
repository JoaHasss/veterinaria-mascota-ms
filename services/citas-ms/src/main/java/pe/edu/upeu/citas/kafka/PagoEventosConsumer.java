package pe.edu.upeu.citas.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.citas.event.PagoValidadoEvento;
import pe.edu.upeu.citas.service.CitaService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventosConsumer {

    private final CitaService citaService;

    @KafkaListener(topics = "${app.kafka.topics.pago-eventos}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(ConsumerRecord<String, PagoValidadoEvento> record) {
        PagoValidadoEvento evento = record.value();
        if (evento == null) {
            // Mensaje sin cuerpo (tombstone). Un JSON mal formado no llega aqui: ErrorHandlingDeserializer
            // lo convierte en DeserializationException, DefaultErrorHandler lo registra y lo salta.
            log.warn("component=pago-consumer eventType=desconocido citaId={} status=DESCARTADO motivo=mensaje_vacio partition={} offset={}",
                    record.key(), record.partition(), record.offset());
            return;
        }
        log.info("component=pago-consumer eventType={} citaId={} status=RECIBIDO partition={} offset={}",
                evento.getTipoEvento(), evento.getCitaId(), record.partition(), record.offset());
        switch (evento.getTipoEvento()) {
            case PagoValidadoEvento.TIPO -> citaService.marcarPagada(evento.getCitaId(), evento.getPagoId());
            case PagoValidadoEvento.TIPO_FALLIDO -> citaService.compensar(evento.getCitaId(), evento.getPagoId(), evento.getMotivo());
            case null, default -> log.info("component=pago-consumer eventType={} citaId={} status=IGNORADO motivo=tipo_no_soportado",
                    evento.getTipoEvento(), evento.getCitaId());
        }
    }
}
