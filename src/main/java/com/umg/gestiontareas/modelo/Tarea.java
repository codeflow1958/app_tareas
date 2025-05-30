package com.umg.gestiontareas.modelo;

import jakarta.persistence.*; // Si estás usando Spring Data JPA
import java.time.LocalDateTime; // Para la fecha y hora de creación

@Entity // Indica que esta clase es una entidad JPA (para la base de datos)
@Table(name = "tareas") // Especifica el nombre de la tabla en la base de datos
public class Tarea {

    @Id // Indica que este atributo es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Genera el ID automáticamente
    private Long id;

    private String titulo;

    private String descripcion;

    private String estado; // Por ejemplo: "PENDIENTE", "EN_PROGRESO", "COMPLETADA"

    private String prioridad; // Por ejemplo: "ALTA", "MEDIA", "BAJA"

    private String tipo; // Por ejemplo: "PERSONAL", "TRABAJO", "ESTUDIO"

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaCompletada;

    private Long idTareaPadre; // Para almacenar el ID de la tarea padre en la DB

    // ¡NUEVO ATRIBUTO PARA LA RELACIÓN PADRE-HIJO!
    // Usamos @ManyToOne para indicar que muchas subtareas pueden tener un solo padre.
    // @JoinColumn especifica la columna de la clave foránea en la tabla 'tareas'
    // que apunta al ID de la tarea padre.
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading para evitar cargar el padre si no se necesita
    @JoinColumn(name = "tarea_padre_id") // Nombre de la columna en la BD que guarda el ID del padre
    private Tarea tareaPadre; // Referencia a la tarea padre

    // Necesitamos constructores, getters y setters. Los generaremos a continuación.

    public Tarea() {
        this.fechaCreacion = LocalDateTime.now(); // Establecer la fecha de creación al crear una nueva tarea
    }

    public Tarea(String titulo, String descripcion, String estado, String prioridad, String tipo) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.prioridad = prioridad;
        this.tipo = tipo;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters para todos los atributos
    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCompletada() {
        return fechaCompletada;
    }

    // ¡NUEVO GETTER PARA TAREA PADRE!
    public Tarea getTareaPadre() {
        return tareaPadre;
    }
    public Long getIdTareaPadre() {
        return idTareaPadre;
    }

    public void setIdTareaPadre(Long idTareaPadre) {
        this.idTareaPadre = idTareaPadre;
    }

    // Setters para los atributos que necesites modificar
    // ¡AÑADE ESTE SETTER!
    public void setId(Long id) { // Este ya lo debiste haber añadido en el paso anterior
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setFechaCompletada(LocalDateTime fechaCompletada) {
        this.fechaCompletada = fechaCompletada;
    }

    // ¡NUEVO SETTER PARA TAREA PADRE!
    public void setTareaPadre(Tarea tareaPadre) {
        this.tareaPadre = tareaPadre;
    }

    // Opcional: Si quieres tener las subtareas referenciadas desde el padre
    // Es una relación bidireccional, pero a menudo no es estrictamente necesaria
    // para tu caso de uso actual de agregar al árbol.
    /*
    @OneToMany(mappedBy = "tareaPadre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarea> subtareas = new ArrayList<>();

    public List<Tarea> getSubtareas() {
        return subtareas;
    }

    public void setSubtareas(List<Tarea> subtareas) {
        this.subtareas = subtareas;
    }

    public void addSubtarea(Tarea subtarea) {
        this.subtareas.add(subtarea);
        subtarea.setTareaPadre(this);
    }

    public void removeSubtarea(Tarea subtarea) {
        this.subtareas.remove(subtarea);
        subtarea.setTareaPadre(null);
    }
    */
}