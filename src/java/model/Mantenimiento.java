package model;

import java.sql.Timestamp;

// Modelo para los reportes de mantenimiento del sistema
public class Mantenimiento {
    private int id;                   // Identificador único del reporte
    private int idResidente;          // ID del residente que reporta
    private String tipoInconveniente; // Tipo de problema (RN1)
    private String descripcion;       // Detalle del problema
    private Timestamp fechaHora;      // Fecha y hora del incidente
    private boolean activo;           // Por si se requiere desactivar el reporte

    // --- Datos auxiliares (no obligatorios en BD) ---
    private String nombreResidente;   // Nombre completo (para notificación)
    private String numeroCasa;        // Casa del residente
    private String lote;              // Lote del residente

    // --- Constructores ---
    public Mantenimiento() {}

    public Mantenimiento(int idResidente, String tipoInconveniente, String descripcion, Timestamp fechaHora) {
        this.idResidente = idResidente;
        this.tipoInconveniente = tipoInconveniente;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.activo = true;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdResidente() { return idResidente; }
    public void setIdResidente(int idResidente) { this.idResidente = idResidente; }

    public String getTipoInconveniente() { return tipoInconveniente; }
    public void setTipoInconveniente(String tipoInconveniente) { this.tipoInconveniente = tipoInconveniente; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombreResidente() { return nombreResidente; }
    public void setNombreResidente(String nombreResidente) { this.nombreResidente = nombreResidente; }

    public String getNumeroCasa() { return numeroCasa; }
    public void setNumeroCasa(String numeroCasa) { this.numeroCasa = numeroCasa; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
}
