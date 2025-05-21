package com.umg.gestiontareas.servicios;

import com.umg.estructuras.pila.PilaAcciones;
import com.umg.estructuras.arbol.ArbolJerarquicoTareas;
import com.umg.estructuras.cola.ColaTareasProgramadas; // Importa la clase de la cola
import com.umg.gestiontareas.modelo.Tarea;
import com.umg.gestiontareas.repositorio.TareaRepositoryMySQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TareaService {

    private static final Logger LOGGER = Logger.getLogger(TareaService.class.getName());

    @Autowired
    private TareaRepositoryMySQL tareaRepository;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    private PilaAcciones<AccionDeshacer> pilaDeshacer = new PilaAcciones<>();
    private ArbolJerarquicoTareas<Tarea> arbolTareas = new ArbolJerarquicoTareas<>();
    private ColaTareasProgramadas<Tarea> colaTareasProgramadas = new ColaTareasProgramadas<>(); // Instancia de la cola

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
            // Aquí podrías necesitar actualizar la posición de la tarea en el árbol si la jerarquía cambió
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
            tareaAnterior.setFechaCompletada(LocalDateTime.now());
            tareaRepository.save(tareaAnterior);
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
                    // Para deshacer la creación, necesitas el ID de la tarea creada.
                    // Si tareaAnterior es null aquí, es porque en crearTarea se pasó null.
                    // Para una implementación robusta, crearTarea debería guardar la Tarea creada en la pila.
                    return "Deshecha la creación. Se requiere más lógica para obtener el ID de la tarea creada para borrarla.";
                case "ELIMINAR":
                    if (tareaAnterior != null) {
                        tareaRepository.save(tareaAnterior);
                        // Aquí necesitarías re-insertar la tarea en el árbol (PENDIENTE)
                        String mensaje = "Deshecha eliminación: ID " + tareaAnterior.getId();
                        rabbitMQSender.sendTareaEvent(mensaje);
                        return mensaje;
                    }
                    return "No se pudo deshacer eliminación: tarea anterior nula.";
                case "ACTUALIZAR":
                    if (tareaAnterior != null) {
                        tareaRepository.save(tareaAnterior);
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
                            tareaActual.setEstado(tareaAnterior.getEstado());
                            tareaActual.setFechaCompletada(tareaAnterior.getFechaCompletada());
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
        pilaDeshacer.push(new AccionDeshacer("CREAR_SUBTAREA", null));
        String mensaje = "Subtarea creada: ID " + nuevaSubtarea.getId() + ", Padre ID: " + idPadre;
        rabbitMQSender.sendTareaEvent(mensaje);
        return nuevaSubtarea;
    }

    public List<Tarea> obtenerJerarquiaTareas() {
        LOGGER.log(Level.INFO, "Obteniendo la jerarquía de tareas desde el árbol.");
        return arbolTareas.obtenerTareasDelArbol();
    }

    /**
     * Agrega una tarea a la cola de tareas programadas.
     * @param tarea La tarea a programar.
     */
    public void programarTarea(Tarea tarea) {
        LOGGER.log(Level.INFO, "Programando tarea: {0}", tarea.getTitulo());
        colaTareasProgramadas.enqueue(tarea);
        String mensaje = "Tarea programada: ID " + tarea.getId() + ", Título: " + tarea.getTitulo();
        rabbitMQSender.sendTareaEvent(mensaje);
    }

    /**
     * Procesa la siguiente tarea en la cola de tareas programadas.
     * @return La tarea procesada, o null si la cola está vacía.
     */
    public Tarea procesarSiguienteTareaProgramada() {
        LOGGER.log(Level.INFO, "Procesando la siguiente tarea programada.");
        Tarea tareaProcesada = colaTareasProgramadas.dequeue();
        if (tareaProcesada != null) {
            // Aquí podrías añadir lógica para "ejecutar" la tarea,
            // por ejemplo, cambiar su estado a "EN_PROCESO" o "FINALIZADA"
            // y guardarla en la base de datos.
            LOGGER.log(Level.INFO, "Tarea programada procesada: {0}", tareaProcesada.getTitulo());
            String mensaje = "Tarea procesada: ID " + tareaProcesada.getId() + ", Título: " + tareaProcesada.getTitulo();
            rabbitMQSender.sendTareaEvent(mensaje);
        } else {
            LOGGER.log(Level.INFO, "No hay tareas en la cola para procesar.");
        }
        return tareaProcesada;
    }

    /**
     * Obtiene (sin eliminar) la siguiente tarea en la cola de tareas programadas.
     * @return La tarea en el frente de la cola, o null si la cola está vacía.
     */
    public Tarea verSiguienteTareaProgramada() {
        LOGGER.log(Level.INFO, "Viendo la siguiente tarea programada (peek).");
        return colaTareasProgramadas.peek();
    }

    /**
     * Verifica si la cola de tareas programadas está vacía.
     * @return true si la cola está vacía, false en caso contrario.
     */
    public boolean estaColaTareasProgramadasVacia() {
        LOGGER.log(Level.INFO, "Verificando si la cola de tareas programadas está vacía.");
        return colaTareasProgramadas.isEmpty();
    }
}
