package com.umg.gestiontareas.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "event_logs") // Mapea esta clase a una colección llamada "event_logs" en MongoDB
public class LogEntry {

    @Id // Marca este campo como el ID del documento en MongoDB
    private String id; // Los IDs de MongoDB suelen ser Strings (UUIDs)

    private String message; // El mensaje del log o evento
    private String eventType; // Tipo de evento (ej. "TAREA_CREADA", "TAREA_ELIMINADA", "TAREA_PROGRAMADA")
    private LocalDateTime timestamp; // Marca de tiempo del evento

    public LogEntry() {
        this.id = UUID.randomUUID().toString(); // Genera un ID único para cada log
        this.timestamp = LocalDateTime.now(); // Establece la marca de tiempo actual
    }

    public LogEntry(String message, String eventType) {
        this(); // Llama al constructor por defecto para inicializar id y timestamp
        this.message = message;
        this.eventType = eventType;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Setters (solo si necesitas modificar estos campos después de la creación)
    public void setMessage(String message) {
        this.message = message;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
