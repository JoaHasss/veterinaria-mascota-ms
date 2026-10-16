package pe.edu.upeu.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.auth.dto.RegistroRequest;
import pe.edu.upeu.auth.entity.Usuario;
import pe.edu.upeu.auth.event.UsuarioRegistradoEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioEventosProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.usuario-eventos}")
    private String topico;

    @Value("${spring.application.name}")
    private String origen;

    /** Publica usuario.registrado solo despues del commit (sin cuentas fantasma). */
    public void publicarUsuarioRegistrado(Usuario usuario, RegistroRequest request) {
        UsuarioRegistradoEvento evento = UsuarioRegistradoEvento.builder()
                .tipoEvento(UsuarioRegistradoEvento.TIPO)
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
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

    private void enviar(UsuarioRegistradoEvento evento) {
        // key = usuarioId: todos los eventos de una misma cuenta van a la misma particion.
        String key = String.valueOf(evento.getUsuarioId());
        kafkaTemplate.send(topico, key, evento).whenComplete((resultado, error) -> {
            if (error != null) {
                log.error("component=usuario-producer eventType={} usuarioId={} status=ERROR_PUBLICACION error={}",
                        evento.getTipoEvento(), evento.getUsuarioId(), error.toString());
            } else {
                log.info("component=usuario-producer eventType={} usuarioId={} status=PUBLICADO topic={} partition={} offset={}",
                        evento.getTipoEvento(), evento.getUsuarioId(), topico,
                        resultado.getRecordMetadata().partition(), resultado.getRecordMetadata().offset());
            }
        });
    }
}
