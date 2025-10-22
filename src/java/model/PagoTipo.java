package model;

public class PagoTipo {
    private int id;
    private String codigo;      // MANTENIMIENTO / MULTA / REINSTALACION
    private String nombre;
    private double monto;
    private boolean activo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
