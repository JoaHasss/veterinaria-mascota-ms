package pe.edu.upeu.citas.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.citas.entity.Cita;
import pe.edu.upeu.citas.event.CitaCreadaEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaEventosProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.cita-eventos}")
    private String topico;

    @Value("${spring.application.name}")
    private String origen;

    /**
     * Publica cita.creada solo despues del commit: si la insercion de la cita
     * hace rollback, el evento no sale (sin pagos de citas inexistentes).
     */
    public void publicarCitaCreada(Cita cita) {
        CitaCreadaEvento evento = CitaCreadaEvento.builder()
                .tipoEvento(CitaCreadaEvento.TIPO)
                .citaId(cita.getId())
                .idCliente(cita.getIdCliente())
                .mascotaId(cita.getMascotaId())
                .monto(cita.getMonto())
                .metodoPago(cita.getMetodoPago())
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

    private void enviar(CitaCreadaEvento evento) {
        // La key es el citaId: todos los eventos de una misma cita caen en la misma particion.
        String key = String.valueOf(evento.getCitaId());
        kafkaTemplate.send(topico, key, evento).whenComplete((resultado, error) -> {
            if (error != null) {
                log.error("component=cita-producer eventType={} citaId={} status=ERROR_PUBLICACION error={}",
                        evento.getTipoEvento(), evento.getCitaId(), error.toString());
            } else {
                log.info("component=cita-producer eventType={} citaId={} status=PUBLICADO topic={} partition={} offset={}",
                        evento.getTipoEvento(), evento.getCitaId(), topico,
                        resultado.getRecordMetadata().partition(), resultado.getRecordMetadata().offset());
            }
        });
    }
}
