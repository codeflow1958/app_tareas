package com.umg.gestiontareas.repositorio;

import com.umg.gestiontareas.modelo.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepositoryMySQL extends JpaRepository<Tarea, Long> {
    // JpaRepository nos proporciona métodos básicos para CRUD (Crear, Leer, Actualizar, Eliminar)
    // para la entidad Tarea que tiene un ID de tipo Long.

    // Métodos personalizados para buscar y clasificar tareas

    /**
     * Busca tareas por un estado específico.
     * @param estado El estado de la tarea (ej. "PENDIENTE", "COMPLETADA").
     * @return Una lista de tareas que coinciden con el estado.
     */
    List<Tarea> findByEstado(String estado);

    /**
     * Busca tareas por una prioridad específica.
     * @param prioridad La prioridad de la tarea (ej. "ALTA", "MEDIA", "BAJA").
     * @return Una lista de tareas que coinciden con la prioridad.
     */
    List<Tarea> findByPrioridad(String prioridad);

    /**
     * Busca tareas por un tipo específico.
     * @param tipo El tipo de tarea (ej. "PERSONAL", "TRABAJO", "ESTUDIO").
     * @return Una lista de tareas que coinciden con el tipo.
     */
    List<Tarea> findByTipo(String tipo);

    /**
     * Busca tareas por un estado y las ordena por fecha de creación (ascendente).
     * @param estado El estado de la tarea.
     * @return Una lista de tareas ordenadas por fecha de creación.
     */
    List<Tarea> findByEstadoOrderByFechaCreacionAsc(String estado);

    /**
     * Busca tareas por una prioridad y las ordena por fecha de creación (descendente).
     * @param prioridad La prioridad de la tarea.
     * @return Una lista de tareas ordenadas por fecha de creación.
     */
    List<Tarea> findByPrioridadOrderByFechaCreacionDesc(String prioridad);

    // Puedes añadir más métodos combinando criterios o de ordenación si lo necesitas.
    // Ej: List<Tarea> findByEstadoAndPrioridad(String estado, String prioridad);
}
