package com.umg.gestiontareas.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descripcion;

    private String estado; // Ej: PENDIENTE, COMPLETADA...

    private String prioridad;

    private String tipo;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaCompletada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_padre_id")
    private Tarea tareaPadre;

    @OneToMany(mappedBy = "tareaPadre", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tarea> subtareas = new HashSet<>();

    public Tarea() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Tarea(String titulo, String descripcion, String estado, String prioridad, String tipo) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.prioridad = prioridad;
        this.tipo = tipo;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {  // Lo dejas para que JPA pueda setearlo
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCompletada() {
        return fechaCompletada;
    }

    public void setFechaCompletada(LocalDateTime fechaCompletada) {
        this.fechaCompletada = fechaCompletada;
    }

    public Tarea getTareaPadre() {
        return tareaPadre;
    }

    public void setTareaPadre(Tarea tareaPadre) {
        this.tareaPadre = tareaPadre;
    }

    public Set<Tarea> getSubtareas() {
        return subtareas;
    }

    public void setSubtareas(Set<Tarea> subtareas) {
        this.subtareas = subtareas;
    }

    // Métodos para agregar y quitar subtareas (opcional pero recomendado)

    public void agregarSubtarea(Tarea subtarea) {
        subtareas.add(subtarea);
        subtarea.setTareaPadre(this);
    }

    public void eliminarSubtarea(Tarea subtarea) {
        subtareas.remove(subtarea);
        subtarea.setTareaPadre(null);
    }
}
