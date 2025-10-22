package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pago {
    private int id;
    private int usuarioId;
    private int tipoId;
    private String tipoNombre;          // salida
    private LocalDate mesAPagar;        // YYYY-MM-01
    private LocalDateTime fechaPago;
    private double montoBase;
    private double recargo;
    private double total;
    private String observaciones;
    private Integer metodoPagoId;       // nullable
    private String metodo;              // "TARJETA"
    private String status; 
    
    public Pago(){}

    public Pago(int id, int usuarioId, int tipoId, String tipoNombre, LocalDate mesAPagar, LocalDateTime fechaPago, double montoBase, double recargo, double total, String observaciones, Integer metodoPagoId, String metodo, String status) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tipoId = tipoId;
        this.tipoNombre = tipoNombre;
        this.mesAPagar = mesAPagar;
        this.fechaPago = fechaPago;
        this.montoBase = montoBase;
        this.recargo = recargo;
        this.total = total;
        this.observaciones = observaciones;
        this.metodoPagoId = metodoPagoId;
        this.metodo = metodo;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getTipoId() {
        return tipoId;
    }

    public void setTipoId(int tipoId) {
        this.tipoId = tipoId;
    }

    public String getTipoNombre() {
        return tipoNombre;
    }

    public void setTipoNombre(String tipoNombre) {
        this.tipoNombre = tipoNombre;
    }

    public LocalDate getMesAPagar() {
        return mesAPagar;
    }

    public void setMesAPagar(LocalDate mesAPagar) {
        this.mesAPagar = mesAPagar;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public double getMontoBase() {
        return montoBase;
    }

    public void setMontoBase(double montoBase) {
        this.montoBase = montoBase;
    }

    public double getRecargo() {
        return recargo;
    }

    public void setRecargo(double recargo) {
        this.recargo = recargo;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(Integer metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}