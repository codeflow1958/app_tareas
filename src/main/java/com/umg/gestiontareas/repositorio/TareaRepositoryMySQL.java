package com.umg.gestiontareas.repositorio;

import com.umg.gestiontareas.modelo.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TareaRepositoryMySQL extends JpaRepository<Tarea, Long> {

}