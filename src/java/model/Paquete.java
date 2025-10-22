package model;

import java.time.LocalDateTime;

public class Paquete {
    private long id;
    private String numeroGuia;

    private int destinatarioId;        // FK a usuarios.id (residente)
    private String casaDestinatario;   // snapshot al registrar
    private String loteDestinatario;   // snapshot al registrar

    private String estado;             // 'PENDIENTE' | 'ENTREGADO'

    private int recibidoPor;           // id guardia que recibe
    private LocalDateTime fechaRecepcion;

    private Integer entregadoPor;      // id guardia que entrega (nullable)
    private LocalDateTime fechaEntrega;

    private String observaciones;

    // --- Getters/Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }

    public int getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(int destinatarioId) { this.destinatarioId = destinatarioId; }

    public String getCasaDestinatario() { return casaDestinatario; }
    public void setCasaDestinatario(String casaDestinatario) { this.casaDestinatario = casaDestinatario; }

    public String getLoteDestinatario() { return loteDestinatario; }
    public void setLoteDestinatario(String loteDestinatario) { this.loteDestinatario = loteDestinatario; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getRecibidoPor() { return recibidoPor; }
    public void setRecibidoPor(int recibidoPor) { this.recibidoPor = recibidoPor; }

    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }

    public Integer getEntregadoPor() { return entregadoPor; }
    public void setEntregadoPor(Integer entregadoPor) { this.entregadoPor = entregadoPor; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
