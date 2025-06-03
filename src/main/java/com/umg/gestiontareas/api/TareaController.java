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
            LOGGER.log(Level.WARNING, "Tarea con ID {0} no encontrada.", id);
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
            LOGGER.log(Level.WARNING, "Tarea con ID {0} no encontrada para actualizar.", id);
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
        return ResponseEntity.ok().build();
    }

    // Endpoint para deshacer la última acción
    @PostMapping("/deshacer")
    public ResponseEntity<String> deshacerUltimaAccion() {
        LOGGER.log(Level.INFO, "Solicitud para deshacer la última acción.");
        String mensaje = tareaService.deshacerUltimaAccion();
        return ResponseEntity.ok(mensaje);
    }

    // Nuevos endpoints para la jerarquía de tareas
    @PostMapping("/{idPadre}/subtarea")
    public ResponseEntity<Tarea> crearSubtarea(@PathVariable Long idPadre, @RequestBody Tarea tarea) {
        LOGGER.log(Level.INFO, "Solicitud para crear subtarea de la tarea con ID: {0}", idPadre);
        Tarea nuevaSubtarea = tareaService.crearSubtarea(tarea, idPadre);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSubtarea);
    }

    @GetMapping("/jerarquia")
    public ResponseEntity<List<Tarea>> obtenerJerarquiaTareas() {
        LOGGER.log(Level.INFO, "Solicitud para obtener la jerarquía de tareas.");
        List<Tarea> tareas = tareaService.obtenerJerarquiaTareas();
        return ResponseEntity.ok(tareas);
    }

    // Nuevos endpoints para la cola de tareas programadas
    @PostMapping("/programar")
    public ResponseEntity<String> programarTarea(@RequestBody Tarea tarea) {
        LOGGER.log(Level.INFO, "Solicitud para programar tarea: {0}", tarea.getTitulo());
        tareaService.programarTarea(tarea);
        return ResponseEntity.ok("Tarea programada exitosamente.");
    }

    @PostMapping("/procesar-siguiente")
    public ResponseEntity<Tarea> procesarSiguienteTareaProgramada() {
        LOGGER.log(Level.INFO, "Solicitud para procesar la siguiente tarea programada.");
        Tarea tareaProcesada = tareaService.procesarSiguienteTareaProgramada();
        if (tareaProcesada != null) {
            return ResponseEntity.ok(tareaProcesada);
        } else {
            LOGGER.log(Level.INFO, "No hay tareas en la cola para procesar.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content si está vacía
        }
    }

    @GetMapping("/siguiente-programada")
    public ResponseEntity<Tarea> verSiguienteTareaProgramada() {
        LOGGER.log(Level.INFO, "Solicitud para ver la siguiente tarea programada.");
        Tarea tarea = tareaService.verSiguienteTareaProgramada();
        if (tarea != null) {
            return ResponseEntity.ok(tarea);
        } else {
            LOGGER.log(Level.INFO, "No hay tareas en la cola para ver.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content si está vacía
        }
    }

    @GetMapping("/cola-vacia")
    public ResponseEntity<Boolean> estaColaTareasProgramadasVacia() {
        LOGGER.log(Level.INFO, "Solicitud para verificar si la cola de tareas programadas está vacía.");
        boolean estaVacia = tareaService.estaColaTareasProgramadasVacia();
        return ResponseEntity.ok(estaVacia);
    }

    // Nuevos endpoints para la clasificación de tareas

    /**
     * Obtiene tareas filtradas por estado.
     * Ejemplo: GET /api/tareas/estado?valor=PENDIENTE
     * @param valor El estado por el cual filtrar.
     * @return Lista de tareas que coinciden con el estado.
     */
    @GetMapping("/estado")
    public ResponseEntity<List<Tarea>> obtenerTareasPorEstado(@RequestParam String valor) {
        LOGGER.log(Level.INFO, "Solicitud para obtener tareas por estado: {0}", valor);
        List<Tarea> tareas = tareaService.findByEstado(valor);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtiene tareas filtradas por prioridad.
     * Ejemplo: GET /api/tareas/prioridad?valor=ALTA
     * @param valor La prioridad por la cual filtrar.
     * @return Lista de tareas que coinciden con la prioridad.
     */
    @GetMapping("/prioridad")
    public ResponseEntity<List<Tarea>> obtenerTareasPorPrioridad(@RequestParam String valor) {
        LOGGER.log(Level.INFO, "Solicitud para obtener tareas por prioridad: {0}", valor);
        List<Tarea> tareas = tareaService.findByPrioridad(valor);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtiene tareas filtradas por tipo.
     * Ejemplo: GET /api/tareas/tipo?valor=TRABAJO
     * @param valor El tipo por el cual filtrar.
     * @return Lista de tareas que coinciden con el tipo.
     */
    @GetMapping("/tipo")
    public ResponseEntity<List<Tarea>> obtenerTareasPorTipo(@RequestParam String valor) {
        LOGGER.log(Level.INFO, "Solicitud para obtener tareas por tipo: {0}", valor);
        List<Tarea> tareas = tareaService.findByTipo(valor);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtiene tareas filtradas por estado y ordenadas por fecha de creación ascendente.
     * Ejemplo: GET /api/tareas/estado-ordenado?valor=PENDIENTE
     * @param valor El estado por el cual filtrar.
     * @return Lista de tareas que coinciden con el estado, ordenadas.
     */
    @GetMapping("/estado-ordenado")
    public ResponseEntity<List<Tarea>> obtenerTareasPorEstadoOrdenado(@RequestParam String valor) {
        LOGGER.log(Level.INFO, "Solicitud para obtener tareas por estado ordenado: {0}", valor);
        List<Tarea> tareas = tareaService.findByEstadoOrderByFechaCreacionAsc(valor);
        return ResponseEntity.ok(tareas);
    }

    /**
     * Obtiene tareas filtradas por prioridad y ordenadas por fecha de creación descendente.
     * Ejemplo: GET /api/tareas/prioridad-ordenada?valor=ALTA
     * @param valor La prioridad por la cual filtrar.
     * @return Lista de tareas que coinciden con la prioridad, ordenadas.
     */
    @GetMapping("/prioridad-ordenada")
    public ResponseEntity<List<Tarea>> obtenerTareasPorPrioridadOrdenada(@RequestParam String valor) {
        LOGGER.log(Level.INFO, "Solicitud para obtener tareas por prioridad ordenada: {0}", valor);
        List<Tarea> tareas = tareaService.findByPrioridadOrderByFechaCreacionDesc(valor);
        return ResponseEntity.ok(tareas);
    }
}
