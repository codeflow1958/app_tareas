package com.umg.gestiontareas.servicios;

import com.umg.estructuras.pila.PilaAcciones;
import com.umg.estructuras.arbol.ArbolJerarquicoTareas;
import com.umg.estructuras.cola.ColaTareasProgramadas;
import com.umg.estructuras.arbol.NodoArbolTarea;
import com.umg.gestiontareas.modelo.Tarea;
import com.umg.gestiontareas.repositorio.TareaRepositoryMySQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // Importa para el método PostConstruct

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
    private ColaTareasProgramadas<Tarea> colaTareasProgramadas = new ColaTareasProgramadas<>();

    // Clase interna AccionDeshacer DEFINIDA DENTRO de TareaService
    private static class AccionDeshacer {
        private String tipo;
        private Tarea tareaAnterior;
        private Long idPadreAsociado;

        public AccionDeshacer(String tipo, Tarea tareaAnterior) {
            this.tipo = tipo;
            this.tareaAnterior = tareaAnterior;
            this.idPadreAsociado = null;
        }

        public AccionDeshacer(String tipo, Tarea tareaAnterior, Long idPadreAsociado) {
            this.tipo = tipo;
            this.tareaAnterior = tareaAnterior;
            this.idPadreAsociado = idPadreAsociado;
        }

        public String getTipo() {
            return tipo;
        }

        public Tarea getTareaAnterior() {
            return tareaAnterior;
        }

        public Long getIdPadreAsociado() {
            return idPadreAsociado;
        }
    }

    // Método que se ejecuta automáticamente después de que se construye el bean
    @PostConstruct
    public void inicializarArbolDesdeDB() {
        LOGGER.log(Level.INFO, "Inicializando el árbol de tareas desde la base de datos al inicio de la aplicación.");
        List<Tarea> todasLasTareas = tareaRepository.findAll();
        // Limpiamos el árbol actual para reconstruirlo
        arbolTareas = new ArbolJerarquicoTareas<>();

        // Primero agregamos todas las tareas que no tienen padre (o null), para que sean raíces o hijos de la raíz principal
        for (Tarea tarea : todasLasTareas) {
            if (tarea.getIdTareaPadre() == null) {
                arbolTareas.agregarTarea(tarea, null); // Agrega como raíz o hijo de la raíz principal
            }
        }
        // Luego, agregamos las tareas que sí tienen padre, asegurándonos de que sus padres ya estén en el árbol
        // Puede que necesitemos varias pasadas si la jerarquía es profunda y no agregamos en orden topológico
        boolean algoAgregadoEnPasada;
        do {
            algoAgregadoEnPasada = false;
            for (Tarea tarea : todasLasTareas) {
                if (tarea.getIdTareaPadre() != null && arbolTareas.buscarNodoPorId(tarea.getId()) == null) { // Si tiene padre y no está ya en el árbol
                    // Intentamos agregarla como subtarea. Si el padre ya existe en el árbol, se agregará.
                    NodoArbolTarea<Tarea> padreEnArbol = arbolTareas.buscarNodoPorId(tarea.getIdTareaPadre());
                    if (padreEnArbol != null) {
                        arbolTareas.agregarTarea(tarea, tarea.getIdTareaPadre());
                        algoAgregadoEnPasada = true;
                    }
                }
            }
        } while (algoAgregadoEnPasada); // Repetimos hasta que no se agregue nada en una pasada completa (o todos se agreguen)
        LOGGER.log(Level.INFO, "Árbol de tareas reconstruido con {0} elementos desde la base de datos.", arbolTareas.obtenerTareasDelArbol().size());
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
        tarea.setIdTareaPadre(null); // Aseguramos que la tarea principal no tenga padre
        Tarea nuevaTarea = tareaRepository.save(tarea);
        arbolTareas.agregarTarea(nuevaTarea, null); // Agrega la nueva tarea al árbol (como raíz si no hay padre)
        pilaDeshacer.push(new AccionDeshacer("CREAR", nuevaTarea)); // Guarda la tarea creada para deshacer
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
            // Si el idTareaPadre cambia, el árbol deberá ser actualizado (PENDIENTE)
            Tarea tareaGuardada = tareaRepository.save(tareaActualizada);
            // Por simplicidad, si la tarea padre se actualiza, el árbol se reconstruye al reiniciar la aplicación
            // Para actualizaciones de jerarquía en tiempo real, necesitarías métodos en ArbolJerarquicoTareas para mover nodos.
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
            // Antes de eliminar de DB, guardamos la tarea y su posible padre para deshacer
            Long idPadre = tareaEliminada.getIdTareaPadre(); // Obtenemos el idPadre de la tarea eliminada
            pilaDeshacer.push(new AccionDeshacer("ELIMINAR", tareaEliminada, idPadre));

            tareaRepository.deleteById(id);
            arbolTareas.eliminarNodoPorId(id); // Eliminar la tarea del árbol en memoria
            String mensaje = "Tarea eliminada: ID " + tareaEliminada.getId() + ", Título: " + tareaEliminada.getTitulo();
            rabbitMQSender.sendTareaEvent(mensaje);
        }
    }

    // Método auxiliar para obtener el padre de un nodo en el árbol (recursivo)
    // Este método ya no es estrictamente necesario si guardamos idTareaPadre en la Tarea
    /*
    private NodoArbolTarea<Tarea> obtenerPadreDelArbol(NodoArbolTarea<Tarea> nodoActual, NodoArbolTarea<Tarea> nodoBuscado) {
        if (nodoActual == null || nodoBuscado == null || nodoActual == nodoBuscado) {
            return null;
        }
        for (NodoArbolTarea<Tarea> hijo : nodoActual.getHijos()) {
            if (hijo == nodoBuscado) {
                return nodoActual;
            }
            NodoArbolTarea<Tarea> padreEncontrado = obtenerPadreDelArbol(hijo, nodoBuscado);
            if (padreEncontrado != null) {
                return padreEncontrado;
            }
        }
        return null;
    }
    */

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
            Long idPadreAsociado = accion.getIdPadreAsociado(); // Este es el idPadre de la tarea anterior, si aplica

            LOGGER.log(Level.INFO, "Deshaciendo acción tipo: {0} para tarea ID: {1}", new Object[]{tipoAccion, tareaAnterior != null ? tareaAnterior.getId() : "N/A"});

            switch (tipoAccion) {
                case "CREAR":
                    // Para deshacer la creación, eliminamos la tarea de la DB y del árbol
                    if (tareaAnterior != null && tareaAnterior.getId() != null) {
                        tareaRepository.deleteById(tareaAnterior.getId());
                        arbolTareas.eliminarNodoPorId(tareaAnterior.getId()); // Eliminar del árbol
                        String mensaje = "Deshecha la creación de la tarea con ID: " + tareaAnterior.getId();
                        rabbitMQSender.sendTareaEvent(mensaje);
                        return mensaje;
                    }
                    return "Deshecha la creación. No se pudo obtener el ID de la tarea creada para borrarla.";
                case "ELIMINAR":
                    if (tareaAnterior != null) {
                        tareaRepository.save(tareaAnterior); // Restaurar tarea en DB
                        arbolTareas.agregarTarea(tareaAnterior, idPadreAsociado); // Re-insertar en el árbol usando su idPadre original
                        String mensaje = "Deshecha eliminación: ID " + tareaAnterior.getId();
                        rabbitMQSender.sendTareaEvent(mensaje);
                        return mensaje;
                    }
                    return "No se pudo deshacer eliminación: tarea anterior nula.";
                case "ACTUALIZAR":
                    if (tareaAnterior != null) {
                        tareaRepository.save(tareaAnterior); // Restaurar estado anterior en DB
                        // Si la jerarquía cambió con la actualización, aquí también se debería revertir el árbol (PENDIENTE)
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
                case "CREAR_SUBTAREA": // Manejar el deshacer de la creación de subtareas
                    if (tareaAnterior != null && tareaAnterior.getId() != null) {
                        tareaRepository.deleteById(tareaAnterior.getId());
                        arbolTareas.eliminarNodoPorId(tareaAnterior.getId()); // Eliminar del árbol
                        String mensaje = "Deshecha la creación de la subtarea con ID: " + tareaAnterior.getId();
                        rabbitMQSender.sendTareaEvent(mensaje);
                        return mensaje;
                    }
                    return "Deshecha la creación de subtarea. No se pudo obtener el ID de la subtarea creada para borrarla.";
                default:
                    return "No se pudo deshacer la acción desconocida: " + tipoAccion;
            }
        }
        return "No hay acciones para deshacer.";
    }

    public Tarea crearSubtarea(Tarea tarea, Long idPadre) {
        LOGGER.log(Level.INFO, "Creando subtarea de la tarea con ID: {0}", idPadre);
        tarea.setIdTareaPadre(idPadre); // <--- Asignamos el ID del padre a la tarea antes de guardar
        Tarea nuevaSubtarea = tareaRepository.save(tarea);
        arbolTareas.agregarTarea(nuevaSubtarea, idPadre);
        pilaDeshacer.push(new AccionDeshacer("CREAR_SUBTAREA", nuevaSubtarea, idPadre));
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
