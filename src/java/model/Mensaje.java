package model;

import java.sql.Timestamp;

public class Mensaje {
    private int id;
    private int idConversacion;
    private int idEmisor;
    private String contenido; // máx 200
    private Timestamp fechaEnvio;
    private boolean leido;

    public Mensaje() {}

    public Mensaje(int idConversacion, int idEmisor, String contenido) {
        this.idConversacion = idConversacion;
        this.idEmisor = idEmisor;
        this.contenido = contenido;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdConversacion() {
        return idConversacion;
    }

    public void setIdConversacion(int idConversacion) {
        this.idConversacion = idConversacion;
    }

    public int getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(int idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Timestamp getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Timestamp fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }
    
}
