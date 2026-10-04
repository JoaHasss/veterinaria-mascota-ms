package pe.edu.upeu.citas.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * citas-ms tambien declara los topicos: asi puede publicar aunque
 * veterinaria-pago-ms nunca haya arrancado (desacoplamiento).
 */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic citaEventosTopic(@Value("${app.kafka.topics.cita-eventos}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic pagoEventosTopic(@Value("${app.kafka.topics.pago-eventos}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
}
