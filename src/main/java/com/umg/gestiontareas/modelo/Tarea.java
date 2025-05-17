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

    // Setters para los atributos que necesites modificar
    public void setId(Long id) { // Agregado el método setId
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
}
