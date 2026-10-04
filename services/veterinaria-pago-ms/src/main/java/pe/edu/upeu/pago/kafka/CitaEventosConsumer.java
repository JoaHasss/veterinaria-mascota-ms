package pe.edu.upeu.pago.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.pago.event.CitaCreadaEvento;
import pe.edu.upeu.pago.service.PagoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaEventosConsumer {

    private final PagoService pagoService;

    @KafkaListener(topics = "${app.kafka.topics.cita-eventos}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(ConsumerRecord<String, CitaCreadaEvento> record) {
        CitaCreadaEvento evento = record.value();
        if (evento == null) {
            // Mensaje sin cuerpo (tombstone). Un JSON mal formado no llega aqui: ErrorHandlingDeserializer
            // lo convierte en DeserializationException, DefaultErrorHandler lo registra y lo salta.
            log.warn("component=cita-consumer eventType=desconocido citaId={} status=DESCARTADO motivo=mensaje_vacio partition={} offset={}",
                    record.key(), record.partition(), record.offset());
            return;
        }
        log.info("component=cita-consumer eventType={} citaId={} status=RECIBIDO partition={} offset={}",
                evento.getTipoEvento(), evento.getCitaId(), record.partition(), record.offset());
        if (!CitaCreadaEvento.TIPO.equals(evento.getTipoEvento())) {
            log.info("component=cita-consumer eventType={} citaId={} status=IGNORADO motivo=tipo_no_soportado",
                    evento.getTipoEvento(), evento.getCitaId());
            return;
        }
        pagoService.procesar(evento);
    }
}
