package model;

import java.sql.Timestamp;

public class Visitante {
    private int id;
    private String nombre;
    private Integer usuarioId;        // FK al residente que crea la visita
    private String dpi;
    private String motivo;
    private String correo;
    private String token;
    private String estado;            // enum('emitido','activo','consumido','cancelado')
    private String tipoVisita;        // enum('por_intentos','visita')
    private String lote;
    private String casa;
    private Timestamp primerUsoEn;
    private Timestamp ultimoUsoEn;
    private Timestamp qrFin;
    private Timestamp creadoEn;
    private Integer usosRealizados;
    private Integer usosMax;

    public Visitante() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoVisita() { return tipoVisita; }
    public void setTipoVisita(String tipoVisita) { this.tipoVisita = tipoVisita; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public String getCasa() { return casa; }
    public void setCasa(String casa) { this.casa = casa; }

    public Timestamp getPrimerUsoEn() { return primerUsoEn; }
    public void setPrimerUsoEn(Timestamp primerUsoEn) { this.primerUsoEn = primerUsoEn; }

    public Timestamp getUltimoUsoEn() { return ultimoUsoEn; }
    public void setUltimoUsoEn(Timestamp ultimoUsoEn) { this.ultimoUsoEn = ultimoUsoEn; }

    public Timestamp getQrFin() { return qrFin; }
    public void setQrFin(Timestamp qrFin) { this.qrFin = qrFin; }

    public Timestamp getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Timestamp creadoEn) { this.creadoEn = creadoEn; }

    public Integer getUsosRealizados() { return usosRealizados; }
    public void setUsosRealizados(Integer usosRealizados) { this.usosRealizados = usosRealizados; }

    public Integer getUsosMax() { return usosMax; }
    public void setUsosMax(Integer usosMax) { this.usosMax = usosMax; }
}
