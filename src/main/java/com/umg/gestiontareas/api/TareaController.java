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
    private TareaService tareaService;

    @GetMapping
    public ResponseEntity<List<Tarea>> obtenerTodasLasTareas() {
        LOGGER.log(Level.INFO, "Obteniendo todas las tareas.");
        List<Tarea> tareas = tareaService.obtenerTodasLasTareas();
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerTareaPorId(@PathVariable Long id) {
        LOGGER.log(Level.INFO, "Obteniendo tarea con ID: {0}", id);
        Tarea tarea = tareaService.obtenerTareaPorId(id);
        if (tarea != null) {
            return new ResponseEntity<>(tarea, HttpStatus.OK);
        } else {
            LOGGER.log(Level.WARNING, "Tarea con ID {0} no encontrada.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Tarea> crearTarea(@RequestBody Tarea tarea) {
        LOGGER.log(Level.INFO, "Creando nueva tarea: {0}", tarea.getTitulo());
        Tarea nuevaTarea = tareaService.crearTarea(tarea);
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizarTarea(@PathVariable Long id, @RequestBody Tarea tareaActualizada) {
        LOGGER.log(Level.INFO, "Actualizando tarea con ID: {0}", id);
        Tarea tarea = tareaService.actualizarTarea(id, tareaActualizada);
        if (tarea != null) {
            return new ResponseEntity<>(tarea, HttpStatus.OK);
        } else {
            LOGGER.log(Level.WARNING, "Tarea con ID {0} no encontrada para actualizar.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        LOGGER.log(Level.INFO, "Eliminando tarea con ID: {0}", id);
        tareaService.eliminarTarea(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<Void> marcarComoCompletada(@PathVariable Long id) {
        LOGGER.log(Level.INFO, "Marcando tarea con ID: {0} como completada", id);
        tareaService.marcarComoCompletada(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/deshacer")
    public ResponseEntity<String> deshacerUltimaAccion() {
        LOGGER.log(Level.INFO, "Deshaciendo la última acción.");
        String mensaje = tareaService.deshacerUltimaAccion();
        return new ResponseEntity<>(mensaje, HttpStatus.OK);
    }

    @PostMapping("/{idPadre}/subtarea")
    public ResponseEntity<Tarea> crearSubtarea(@PathVariable Long idPadre, @RequestBody Tarea tarea) {
        LOGGER.log(Level.INFO, "Creando subtarea de la tarea con ID: {0}", idPadre);
        Tarea tareaPadre = tareaService.obtenerTareaPorId(idPadre);
        if (tareaPadre == null) {
            LOGGER.log(Level.WARNING, "Tarea padre con ID {0} no encontrada.", idPadre);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Tarea nuevaSubtarea = tareaService.crearSubtarea(tareaPadre, tarea);
        return new ResponseEntity<>(nuevaSubtarea, HttpStatus.CREATED);
    }

    @GetMapping("/jerarquia")
    public ResponseEntity<List<Tarea>> obtenerJerarquiaTareas() {
        LOGGER.log(Level.INFO, "Obteniendo la jerarquía de tareas.");
        List<Tarea> tareas = tareaService.obtenerJerarquiaTareas();
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }
}
 
