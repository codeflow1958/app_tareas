package com.umg.gestiontareas.api;

import com.umg.gestiontareas.modelo.Tarea;
import com.umg.gestiontareas.servicios.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private static final Logger LOGGER = Logger.getLogger(TareaController.class.getName());

    @Autowired
    private TareaService tareaService; // Inyecta el servicio de tareas

    // Ya no inyectamos RabbitMQSender aquí

    // Endpoint para obtener todas las tareas
    @GetMapping
    public ResponseEntity<List<Tarea>> obtenerTodasLasTareas() {
        LOGGER.log(Level.INFO, "Solicitud para obtener todas las tareas.");
        List<Tarea> tareas = tareaService.obtenerTodasLasTareas();
        return ResponseEntity.ok(tareas);
    }

    // Endpoint para obtener una tarea por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerTareaPorId(@PathVariable Long id) {
        LOGGER.log(Level.INFO, "Solicitud para obtener tarea con ID: {0}", id);
        Tarea tarea = tareaService.obtenerTareaPorId(id);
        if (tarea != null) {
            return ResponseEntity.ok(tarea);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para crear una nueva tarea (el servicio se encarga de enviar el mensaje)
    @PostMapping
    public ResponseEntity<Tarea> crearTarea(@RequestBody Tarea tarea) {
        LOGGER.log(Level.INFO, "Solicitud para crear nueva tarea: {0}", tarea.getTitulo());
        Tarea nuevaTarea = tareaService.crearTarea(tarea);
        LOGGER.log(Level.INFO, "Tarea creada. El servicio envió el mensaje a RabbitMQ.");
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTarea);
    }

    // Endpoint para actualizar una tarea existente (el servicio se encarga de enviar el mensaje)
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizarTarea(@PathVariable Long id, @RequestBody Tarea tareaActualizada) {
        LOGGER.log(Level.INFO, "Solicitud para actualizar tarea con ID: {0}", id);
        Tarea tareaGuardada = tareaService.actualizarTarea(id, tareaActualizada);
        if (tareaGuardada != null) {
            LOGGER.log(Level.INFO, "Tarea actualizada. El servicio envió el mensaje a RabbitMQ.");
            return ResponseEntity.ok(tareaGuardada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para eliminar una tarea (el servicio se encarga de enviar el mensaje)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        LOGGER.log(Level.INFO, "Solicitud para eliminar tarea con ID: {0}", id);
        tareaService.eliminarTarea(id);
        LOGGER.log(Level.INFO, "Tarea eliminada. El servicio envió el mensaje a RabbitMQ.");
        return ResponseEntity.noContent().build();
    }

    // Endpoint para marcar una tarea como completada (el servicio se encarga de enviar el mensaje)
    @PutMapping("/{id}/completar")
    public ResponseEntity<Void> marcarComoCompletada(@PathVariable Long id) {
        LOGGER.log(Level.INFO, "Solicitud para marcar tarea con ID: {0} como completada.", id);
        tareaService.marcarComoCompletada(id);
        LOGGER.log(Level.INFO, "Tarea marcada como completada. El servicio envió el mensaje a RabbitMQ.");
        return ResponseEntity.ok().build(); // Cambiado a .build() ya que el cuerpo es void
    }

    // Endpoint para deshacer la última acción
    @PostMapping("/deshacer")
    public ResponseEntity<String> deshacerUltimaAccion() {
        LOGGER.log(Level.INFO, "Solicitud para deshacer la última acción.");
        String mensaje = tareaService.deshacerUltimaAccion();
        return ResponseEntity.ok(mensaje);
    }

    // Endpoint para crear una subtarea
    @PostMapping("/{idPadre}/subtarea")
    public ResponseEntity<Tarea> crearSubtarea(@PathVariable Long idPadre, @RequestBody Tarea tarea) {
        LOGGER.log(Level.INFO, "Solicitud para crear subtarea de la tarea con ID: {0}", idPadre);
        Tarea nuevaSubtarea = tareaService.crearSubtarea(tarea, idPadre);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSubtarea);
    }

    // Endpoint para obtener la jerarquía de tareas
    @GetMapping("/jerarquia")
    public ResponseEntity<List<Tarea>> obtenerJerarquiaTareas() {
        LOGGER.log(Level.INFO, "Solicitud para obtener la jerarquía de tareas.");
        List<Tarea> tareas = tareaService.obtenerJerarquiaTareas();
        return ResponseEntity.ok(tareas);
    }
}