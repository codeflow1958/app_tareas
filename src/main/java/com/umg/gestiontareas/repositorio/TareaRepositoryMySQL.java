package com.umg.gestiontareas.repositorio;

import com.umg.gestiontareas.modelo.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TareaRepositoryMySQL extends JpaRepository<Tarea, Long> {
    // JpaRepository nos proporciona métodos básicos para CRUD (Crear, Leer, Actualizar, Eliminar)
    // para la entidad Tarea que tiene un ID de tipo Long.

    // Aquí puedes agregar métodos personalizados para consultas más específicas si las necesitas.
    // Por ejemplo:
    // List<Tarea> findByEstado(String estado);
    // List<Tarea> findByPrioridadOrderByFechaCreacion(String prioridad);
}