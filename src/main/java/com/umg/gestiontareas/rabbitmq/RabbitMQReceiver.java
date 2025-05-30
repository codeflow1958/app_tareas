package com.umg.gestiontareas.rabbitmq; // Asegúrate de que este paquete sea el correcto

import com.umg.gestiontareas.config.RabbitMQConfig;
import com.umg.gestiontareas.modelo.LogEntry; // Importa la nueva entidad LogEntry
import com.umg.gestiontareas.repositorio.LogEntryRepository; // Importa el nuevo repositorio
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component // Indica que esta clase es un componente de Spring
public class RabbitMQReceiver {

    private static final Logger LOGGER = Logger.getLogger(RabbitMQReceiver.class.getName());

    @Autowired
    private LogEntryRepository logEntryRepository; // Inyecta el repositorio de logs de MongoDB

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        LOGGER.log(Level.INFO, "Mensaje recibido de RabbitMQ: {0}", message);

        // Opcional: Extraer el tipo de evento del mensaje si el formato lo permite
        // Por ahora, lo ponemos como un String simple, pero podrías parsear el mensaje
        String eventType = "TAREA_EVENTO";
        if (message.contains("Tarea creada")) {
            eventType = "TAREA_CREADA";
        } else if (message.contains("Tarea actualizada")) {
            eventType = "TAREA_ACTUALIZADA";
        } else if (message.contains("Tarea eliminada")) {
            eventType = "TAREA_ELIMINADA";
        } else if (message.contains("Tarea completada")) {
            eventType = "TAREA_COMPLETADA";
        } else if (message.contains("Tarea programada")) {
            eventType = "TAREA_PROGRAMADA";
        } else if (message.contains("Tarea procesada")) {
            eventType = "TAREA_PROCESADA";
        } else if (message.contains("Subtarea creada")) { // Para el nuevo evento de subtarea
            eventType = "SUBTAREA_CREADA";
        }

        // Crear y guardar el log en MongoDB
        LogEntry logEntry = new LogEntry(message, eventType);
        logEntryRepository.save(logEntry);
        LOGGER.log(Level.INFO, "Log de evento guardado en MongoDB: {0}", logEntry.getId());
    }
}
