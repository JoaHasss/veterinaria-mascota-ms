package pe.edu.upeu.pago.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.PagoValidadoEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventosProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.pago-eventos}")
    private String topico;

    @Value("${spring.application.name}")
    private String origen;

    /**
     * Se publica solo despues del commit de la transaccion de BD: si el
     * guardado falla y hace rollback, el evento nunca sale (sin eventos fantasma).
     */
    public void publicarPagoValidado(Pago pago) {
        PagoValidadoEvento evento = PagoValidadoEvento.builder()
                .tipoEvento(PagoValidadoEvento.TIPO)
                .citaId(pago.getCitaId())
                .pagoId(pago.getId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado().name())
                .origen(origen)
                .timestamp(System.currentTimeMillis())
                .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(evento);
                }
            });
        } else {
            enviar(evento);
        }
    }

    private void enviar(PagoValidadoEvento evento) {
        // La key es el citaId: todos los eventos de una misma cita caen en la misma particion.
        String key = String.valueOf(evento.getCitaId());
        kafkaTemplate.send(topico, key, evento).whenComplete((resultado, error) -> {
            if (error != null) {
                log.error("component=pago-producer eventType={} citaId={} status=ERROR_PUBLICACION error={}",
                        evento.getTipoEvento(), evento.getCitaId(), error.toString());
            } else {
                log.info("component=pago-producer eventType={} citaId={} status=PUBLICADO topic={} partition={} offset={}",
                        evento.getTipoEvento(), evento.getCitaId(), topico,
                        resultado.getRecordMetadata().partition(), resultado.getRecordMetadata().offset());
            }
        });
    }
}
