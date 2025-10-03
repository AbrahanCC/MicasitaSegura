package model;

import java.sql.Timestamp;

public class Incidente {
    public enum Tipo {
        DISTURBIOS, RUIDO, ACCIDENTE_VEHICULAR, DAÑOS_INMOBILIARIOS, OTROS
    }

    private int id;
    private int idResidente;
    private String tipo;           // guarda el nombre del enum en BD
    private Timestamp fechaHora;
    private String descripcion;
    private Timestamp fechaCreacion;

    public Incidente() {}

    public Incidente(int idResidente, String tipo, Timestamp fechaHora, String descripcion) {
        this.idResidente = idResidente;
        this.tipo = tipo;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdResidente() {
        return idResidente;
    }

    public void setIdResidente(int idResidente) {
        this.idResidente = idResidente;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Timestamp getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Timestamp fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    }