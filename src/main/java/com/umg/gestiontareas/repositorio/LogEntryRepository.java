package com.umg.gestiontareas.repositorio;

import com.umg.gestiontareas.modelo.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {
    // MongoRepository proporciona métodos CRUD básicos para la entidad LogEntry
    // que tiene un ID de tipo String.
}
