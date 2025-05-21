package com.umg.gestiontareas.servicios; // Asegúrate de que este paquete sea el correcto

import com.umg.gestiontareas.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component // Indica que esta clase es un componente de Spring
public class RabbitMQSender {

    private static final Logger LOGGER = Logger.getLogger(RabbitMQSender.class.getName());

    @Autowired
    private RabbitTemplate rabbitTemplate; // Spring Boot autoconfigura esto con tus propiedades

    /**
     * Envía un mensaje de texto a RabbitMQ.
     * @param message El mensaje a enviar.
     */
    public void sendTareaEvent(String message) {
        LOGGER.log(Level.INFO, "Enviando mensaje a RabbitMQ: {0}", message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
        LOGGER.log(Level.INFO, "Mensaje enviado exitosamente.");
    }

    // Opcional: Si quieres enviar objetos Tarea directamente (requiere configuración de Jackson en RabbitMQConfig)
    // public void sendTareaObject(Tarea tarea) {
    //     LOGGER.log(Level.INFO, "Enviando objeto Tarea a RabbitMQ: {0}", tarea.getTitulo());
    //     rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, tarea);
    //     LOGGER.log(Level.INFO, "Objeto Tarea enviado exitosamente.");
    // }
}