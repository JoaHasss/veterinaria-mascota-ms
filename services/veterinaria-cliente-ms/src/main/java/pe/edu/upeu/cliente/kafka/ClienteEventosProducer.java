package pe.edu.upeu.cliente.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.edu.upeu.cliente.entity.AltaCliente;
import pe.edu.upeu.cliente.event.ClienteResultadoEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventosProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.cliente-eventos}")
    private String topico;

    @Value("${spring.application.name}")
    private String origen;

    /** Publica cliente.creado o cliente.rechazado segun el resultado del alta. */
    public void publicarResultado(AltaCliente alta) {
        boolean creado = alta.getResultado() == AltaCliente.Resultado.CREADO;
        ClienteResultadoEvento evento = ClienteResultadoEvento.builder()
                .tipoEvento(creado ? ClienteResultadoEvento.TIPO_CREADO : ClienteResultadoEvento.TIPO_RECHAZADO)
                .usuarioId(alta.getUsuarioId())
                .clienteId(alta.getClienteId())
                .email(alta.getEmail())
                .motivo(alta.getMotivo())
                .origen(origen)
                .timestamp(System.currentTimeMillis())
                .build();

        // key = usuarioId: misma particion que el usuario.registrado que lo origino.
        String key = String.valueOf(evento.getUsuarioId());
        kafkaTemplate.send(topico, key, evento).whenComplete((resultado, error) -> {
            if (error != null) {
                log.error("component=cliente-producer eventType={} usuarioId={} status=ERROR_PUBLICACION error={}",
                        evento.getTipoEvento(), evento.getUsuarioId(), error.toString());
            } else {
                log.info("component=cliente-producer eventType={} usuarioId={} status=PUBLICADO topic={} partition={} offset={}",
                        evento.getTipoEvento(), evento.getUsuarioId(), topico,
                        resultado.getRecordMetadata().partition(), resultado.getRecordMetadata().offset());
            }
        });
    }
}
