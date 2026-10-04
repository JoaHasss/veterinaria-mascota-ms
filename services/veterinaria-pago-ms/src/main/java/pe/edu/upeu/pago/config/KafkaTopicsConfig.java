package pe.edu.upeu.pago.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara los topicos con 3 particiones. KafkaAdmin los crea al arrancar si
 * no existen; si ya existen no modifica sus particiones.
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
