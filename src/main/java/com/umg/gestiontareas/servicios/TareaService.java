package com.umg.gestiontareas.servicios;

import com.umg.estructuras.pila.PilaAcciones;
import com.umg.estructuras.arbol.ArbolJerarquicoTareas;
import com.umg.gestiontareas.modelo.Tarea;
import com.umg.gestiontareas.repositorio.TareaRepositoryMySQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Asegúrate de importar LocalDateTime si no está
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TareaService {

    private static final Logger LOGGER = Logger.getLogger(TareaService.class.getName());

    @Autowired
    private TareaRepositoryMySQL tareaRepository;

    @Autowired // Inyectamos el RabbitMQSender aquí
    private RabbitMQSender rabbitMQSender;

    private PilaAcciones<AccionDeshacer> pilaDeshacer = new PilaAcciones<>();
    private ArbolJerarquicoTareas<Tarea> arbolTareas = new ArbolJerarquicoTareas<>();

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
        arbolTareas.agregarTarea(nuevaTarea, null);
        pilaDeshacer.push(new AccionDeshacer("CREAR", null));
        // Enviar evento de RabbitMQ para la creación de la tarea
        String mensaje = "Tarea creada: ID " + nuevaTarea.getId() + ", Título: " + nuevaTarea.getTitulo();
        rabbitMQSender.sendTareaEvent(mensaje);
        return nuevaTarea;
    }

    public Tarea actualizarTarea(Long id, Tarea tareaActualizada) {
        LOGGER.log(Level.INFO, "Actualizando tarea con ID: {0}", id);
        Tarea tareaExistente = tareaRepository.findById(id).orElse(null);
        if (tareaExistente != null) {
            pilaDeshacer.push(new AccionDeshacer("ACTUALIZAR", tareaExistente));
            tareaActualizada.setId(id);
            Tarea tareaGuardada = tareaRepository.save(tareaActualizada);
            // Enviar evento de RabbitMQ para la actualización de la tarea
            String mensaje = "Tarea actualizada: ID " + tareaGuardada.getId() + ", Título: " + tareaGuardada.getTitulo();
            rabbitMQSender.sendTareaEvent(mensaje);
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
            // Aquí necesitarías eliminar la tarea del árbol (PENDIENTE)
            // Enviar evento de RabbitMQ para la eliminación de la tarea
            String mensaje = "Tarea eliminada: ID " + tareaEliminada.getId() + ", Título: " + tareaEliminada.getTitulo();
            rabbitMQSender.sendTareaEvent(mensaje);
        }
    }

    public void marcarComoCompletada(Long id) {
        LOGGER.log(Level.INFO, "Marcando tarea con ID: {0} como completada", id);
        Tarea tareaAnterior = tareaRepository.findById(id).orElse(null);
        if (tareaAnterior != null) {
            pilaDeshacer.push(new AccionDeshacer("COMPLETAR", tareaAnterior));
            tareaAnterior.setEstado("COMPLETADA");
            tareaAnterior.setFechaCompletada(LocalDateTime.now()); // Opcional: Establecer fecha de completado
            tareaRepository.save(tareaAnterior);
            // Enviar evento de RabbitMQ para la tarea completada
            String mensaje = "Tarea completada: ID " + tareaAnterior.getId() + ", Título: " + tareaAnterior.getTitulo();
            rabbitMQSender.sendTareaEvent(mensaje);
        }
    }

    public String deshacerUltimaAccion() {
        AccionDeshacer accion = pilaDeshacer.pop();
        if (accion != null) {
            String tipoAccion = accion.getTipo();
            Tarea tareaAnterior = accion.getTareaAnterior();

            LOGGER.log(Level.INFO, "Deshaciendo acción tipo: {0} para tarea ID: {1}", new Object[]{tipoAccion, tareaAnterior != null ? tareaAnterior.getId() : "N/A"});

            switch (tipoAccion) {
                case "CREAR":
                    // Asumimos que tareaAnterior para CREAR es null, así que usamos el ID que se creó
                    // Nota: Si quieres deshacer la creación, necesitas el ID de la tarea creada.
                    // En tu implementación actual, solo guardas null. Si quieres el ID, debes modificar crearTarea.
                    // Por ahora, para evitar NPE, solo devolvemos un mensaje.
                    // Para una implementación robusta, crearTarea debería devolver la tarea creada
                    // y ese objeto Tarea (con ID) debería guardarse en la pila.
                    return "Deshecha la creación. Se requiere más lógica para obtener el ID de la tarea creada para borrarla.";
                case "ELIMINAR":
                    if (tareaAnterior != null) {
                        tareaRepository.save(tareaAnterior); // Restaurar tarea
                        // Aquí necesitarías re-insertar la tarea en el árbol (PENDIENTE)
                        String mensaje = "Deshecha eliminación: ID " + tareaAnterior.getId();
                        rabbitMQSender.sendTareaEvent(mensaje);
                        return mensaje;
                    }
                    return "No se pudo deshacer eliminación: tarea anterior nula.";
                case "ACTUALIZAR":
                    if (tareaAnterior != null) {
                        tareaRepository.save(tareaAnterior); // Restaurar estado anterior
                        // Aquí podrías necesitar actualizar la posición de la tarea en el árbol si la jerarquía cambió (PENDIENTE)
                        String mensaje = "Deshecha actualización: ID " + tareaAnterior.getId();
                        rabbitMQSender.sendTareaEvent(mensaje);
                        return mensaje;
                    }
                    return "No se pudo deshacer actualización: tarea anterior nula.";
                case "COMPLETAR":
                    if (tareaAnterior != null) {
                        Tarea tareaActual = tareaRepository.findById(tareaAnterior.getId()).orElse(null);
                        if (tareaActual != null) {
                            tareaActual.setEstado(tareaAnterior.getEstado()); // Restaurar estado original
                            tareaActual.setFechaCompletada(tareaAnterior.getFechaCompletada()); // Restaurar fecha completada original
                            tareaRepository.save(tareaActual);
                            String mensaje = "Deshecho completar: ID " + tareaAnterior.getId();
                            rabbitMQSender.sendTareaEvent(mensaje);
                            return mensaje;
                        }
                    }
                    return "No se pudo deshacer completar: tarea anterior nula o no encontrada.";
                default:
                    return "No se pudo deshacer la acción desconocida: " + tipoAccion;
            }
        }
        return "No hay acciones para deshacer.";
    }

    public Tarea crearSubtarea(Tarea tarea, Long idPadre) {
        LOGGER.log(Level.INFO, "Creando subtarea de la tarea con ID: {0}", idPadre);
        Tarea nuevaSubtarea = tareaRepository.save(tarea);
        arbolTareas.agregarTarea(nuevaSubtarea, idPadre);
        pilaDeshacer.push(new AccionDeshacer("CREAR_SUBTAREA", null)); //Podriamos guardar el id del padre (PENDIENTE)
        String mensaje = "Subtarea creada: ID " + nuevaSubtarea.getId() + ", Padre ID: " + idPadre;
        rabbitMQSender.sendTareaEvent(mensaje);
        return nuevaSubtarea;
    }

    public List<Tarea> obtenerJerarquiaTareas() {
        LOGGER.log(Level.INFO, "Obteniendo la jerarquía de tareas desde el árbol.");
        return arbolTareas.obtenerTareasDelArbol(); // Aquí usas tu método ya definido
    }

    // Aquí iría la lógica para clasificar tareas y para tareas programadas (usando tus colas).
}