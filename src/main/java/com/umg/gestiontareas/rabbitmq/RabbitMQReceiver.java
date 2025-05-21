package com.umg.gestiontareas.rabbitmq;

import com.umg.gestiontareas.config.RabbitMQConfig; // Importa la configuración
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class RabbitMQReceiver {

    private static final Logger LOGGER = Logger.getLogger(RabbitMQReceiver.class.getName());

    // @RabbitListener hace que este método escuche mensajes de la cola especificada
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        LOGGER.log(Level.INFO, "Mensaje recibido de RabbitMQ: {0}", message);
        // Aquí puedes añadir la lógica para procesar el mensaje
        // Por ejemplo, actualizar una tarea, notificar a un usuario, etc.
    }

    // Opcional: para recibir objetos (si habilitas el JsonMessageConverter en RabbitMQConfig)
    // @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    // public void receiveObject(Object object) {
    //     LOGGER.log(Level.INFO, "Objeto recibido de RabbitMQ: {0}", object.toString());
    //     // Procesa el objeto deserializado
    // }
}