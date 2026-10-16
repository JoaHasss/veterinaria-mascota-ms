package pe.edu.upeu.cliente.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** S9: topicos de la saga de alta de cliente (ambos servicios los declaran). */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic usuarioEventosTopic(@Value("${app.kafka.topics.usuario-eventos}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic clienteEventosTopic(@Value("${app.kafka.topics.cliente-eventos}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
}
