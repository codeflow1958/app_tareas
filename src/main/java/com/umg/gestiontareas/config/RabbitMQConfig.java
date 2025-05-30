package com.umg.gestiontareas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombre de la cola donde se enviarán/recibirán los mensajes
    public static final String QUEUE_NAME = "gestionTareasQueue";

    // Nombre del intercambiador (exchange)
    public static final String EXCHANGE_NAME = "gestionTareasExchange";

    // Clave de enrutamiento (routing key)
    public static final String ROUTING_KEY = "tareas.creacion";

    // 1. Definir la Cola (Queue)
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, false); // 'false' indica que la cola no es durable (se borra al reiniciar RabbitMQ si no hay consumidores)
        // Para producción, a menudo se usa 'true' para durabilidad.
    }

    // 2. Definir el Intercambiador (Exchange)
    // Usamos TopicExchange, que permite enrutamiento basado en patrones de routing key
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 3. Unir la Cola al Intercambiador con una Clave de Enrutamiento (Binding)
    // Esto le dice a RabbitMQ: "envía los mensajes del exchange al queue si la routing key coincide"
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY); // La clave de enrutamiento que usará el productor
    }

    // Opcional: Configuración para el convertidor de mensajes JSON (recomendado para objetos Java)
    // @Bean
    // public MessageConverter jsonMessageConverter() {
    //     return new Jackson2JsonMessageConverter();
    // }

    // Opcional: Plantilla AMQP para enviar mensajes
    // @Bean
    // public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
    //     final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    //     rabbitTemplate.setMessageConverter(jsonMessageConverter());
    //     return rabbitTemplate;
    // }
}