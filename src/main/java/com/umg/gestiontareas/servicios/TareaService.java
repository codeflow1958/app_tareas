package com.umg.gestiontareas.servicios;

import com.umg.estructuras.pila.PilaAcciones;
import com.umg.gestiontareas.modelo.Tarea;
import com.umg.gestiontareas.repositorio.TareaRepositoryMySQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TareaService {

    private static final Logger LOGGER = Logger.getLogger(TareaService.class.getName());

    @Autowired
    private TareaRepositoryMySQL tareaRepository;

    private PilaAcciones<AccionDeshacer> pilaDeshacer = new PilaAcciones<>();

    // Clase interna AccionDeshacer DEFINIDA DENTRO de TareaService
    private static class AccionDeshacer {
        private String tipo;
        private Tarea tareaAnterior;

        public AccionDeshacer(String tipo, Tarea tareaAnterior) {
            this.tipo = tipo;
            this.tareaAnterior = tareaAnterior;
        }

        public String getTipo() {
            return tipo;
        }

        public Tarea getTareaAnterior() {
            return tareaAnterior;
        }
    }

    public List<Tarea> obtenerTodasLasTareas() {
        LOGGER.log(Level.INFO, "Obteniendo todas las tareas.");
        return tareaRepository.findAll();
    }

    public Tarea obtenerTareaPorId(Long id) {
        LOGGER.log(Level.INFO, "Obteniendo tarea con ID: {0}", id);
        return tareaRepository.findById(id).orElse(null);
    }

    public Tarea crearTarea(Tarea tarea) {
        LOGGER.log(Level.INFO, "Creando nueva tarea: {0}", tarea.getTitulo());
        Tarea nuevaTarea = tareaRepository.save(tarea);
        pilaDeshacer.push(new AccionDeshacer("CREAR", null));
        return nuevaTarea;
    }

    public Tarea actualizarTarea(Long id, Tarea tareaActualizada) {
        LOGGER.log(Level.INFO, "Actualizando tarea con ID: {0}", id);
        Tarea tareaExistente = tareaRepository.findById(id).orElse(null);
        if (tareaExistente != null) {
            pilaDeshacer.push(new AccionDeshacer("ACTUALIZAR", tareaExistente));
            tareaActualizada.setId(id);
            Tarea tareaGuardada = tareaRepository.save(tareaActualizada);
            return tareaGuardada;
        }
        return null; // O lanzar una excepción
    }

    public void eliminarTarea(Long id) {
        LOGGER.log(Level.INFO, "Eliminando tarea con ID: {0}", id);
        Tarea tareaEliminada = tareaRepository.findById(id).orElse(null);
        if (tareaEliminada != null) {
            pilaDeshacer.push(new AccionDeshacer("ELIMINAR", tareaEliminada));
            tareaRepository.deleteById(id);
        }
    }

    public void marcarComoCompletada(Long id) {
        LOGGER.log(Level.INFO, "Marcando tarea con ID: {0} como completada", id);
        Tarea tareaAnterior = tareaRepository.findById(id).orElse(null);
        if (tareaAnterior != null) {
            pilaDeshacer.push(new AccionDeshacer("COMPLETAR", tareaAnterior));
            tareaAnterior.setEstado("COMPLETADA");
            tareaRepository.save(tareaAnterior);
        }
    }

    public String deshacerUltimaAccion() {
        AccionDeshacer accion = pilaDeshacer.pop();
        if (accion != null) {
            String tipoAccion = accion.getTipo();
            Tarea tareaAnterior = accion.getTareaAnterior();

            switch (tipoAccion) {
                case "CREAR":
                    if (tareaAnterior != null) {  // Agregada esta condición
                        tareaRepository.deleteById(tareaAnterior.getId());
                        return "Deshecha la creación de la tarea con ID: " + tareaAnterior.getId();
                    } else {
                        // Manejar el caso donde tareaAnterior es null para CREAR
                        return "Deshecha la creación. No se pudo obtener el ID de la tarea creada.";
                    }
                case "ELIMINAR":
                    tareaRepository.save(tareaAnterior);
                    return "Deshecha la eliminación de la tarea con ID: " + tareaAnterior.getId();
                case "ACTUALIZAR":
                    tareaRepository.save(tareaAnterior);
                    return "Deshecha la actualización de la tarea con ID: " + tareaAnterior.getId();
                case "COMPLETAR":
                    Tarea tarea = tareaRepository.findById(tareaAnterior.getId()).orElse(null);
                    if (tarea != null) {
                        tarea.setEstado(tareaAnterior.getEstado());
                        tareaRepository.save(tarea);
                        return "Deshecho el marcar como completada de la tarea con ID: " + tareaAnterior.getId();
                    }
                    break;
                default:
                    return "No se pudo deshacer la acción: " + tipoAccion;
            }
        }
        return "No hay acciones para deshacer.";
    }

    // Aquí iría la lógica para clasificar tareas, jerarquizarlas (usando tus árboles),
    // y para tareas programadas (usando tus colas).
}
