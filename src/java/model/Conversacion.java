package model;

import java.sql.Timestamp;

public class Conversacion {
    public static final String ESTADO_ACTIVA = "ACTIVA";
    public static final String ESTADO_CERRADA = "CERRADA";

    private int id;
    private int idResidente;
    private int idGuardia;
    private String estado;
    private Timestamp fechaCreacion;
    private Timestamp fechaUltimoMensaje;

    public Conversacion() {}

    public Conversacion(int idResidente, int idGuardia) {
        this.idResidente = idResidente;
        this.idGuardia = idGuardia;
        this.estado = ESTADO_ACTIVA;
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

    public int getIdGuardia() {
        return idGuardia;
    }

    public void setIdGuardia(int idGuardia) {
        this.idGuardia = idGuardia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getFechaUltimoMensaje() {
        return fechaUltimoMensaje;
    }

    public void setFechaUltimoMensaje(Timestamp fechaUltimoMensaje) {
        this.fechaUltimoMensaje = fechaUltimoMensaje;
    }

}

