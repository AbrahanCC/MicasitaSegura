package model;

import java.sql.Timestamp;

public class Visitante {

    private int id;
    private String nombre;
    private String dpi;
    private String motivo;
    private Timestamp fechaHora;
    private String destinoNumeroCasa;
    private Integer creadoPorGuardiaId;   // mejor como Integer, puede ser null

    // Nuevos campos (pases)
    private String email;
    private String token;
    private Timestamp expiraEn;
    private String estado;   // emitido | consumido | caducado
    private Timestamp creadoEn;

    public Visitante() {}

    public Visitante(int id, String nombre, String dpi, String motivo, Timestamp fechaHora,
                     String destinoNumeroCasa, Integer creadoPorGuardiaId,
                     String email, String token, Timestamp expiraEn, String estado, Timestamp creadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.dpi = dpi;
        this.motivo = motivo;
        this.fechaHora = fechaHora;
        this.destinoNumeroCasa = destinoNumeroCasa;
        this.creadoPorGuardiaId = creadoPorGuardiaId;
        this.email = email;
        this.token = token;
        this.expiraEn = expiraEn;
        this.estado = estado;
        this.creadoEn = creadoEn;
    }

    // ===== Getters & Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }

    public String getDestinoNumeroCasa() { return destinoNumeroCasa; }
    public void setDestinoNumeroCasa(String destinoNumeroCasa) { this.destinoNumeroCasa = destinoNumeroCasa; }

    public Integer getCreadoPorGuardiaId() { return creadoPorGuardiaId; }
    public void setCreadoPorGuardiaId(Integer creadoPorGuardiaId) { this.creadoPorGuardiaId = creadoPorGuardiaId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Timestamp getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Timestamp expiraEn) { this.expiraEn = expiraEn; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Timestamp creadoEn) { this.creadoEn = creadoEn; }

    @Override
    public String toString() {
        return "Visitante{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", dpi='" + dpi + '\'' +
                ", motivo='" + motivo + '\'' +
                ", fechaHora=" + fechaHora +
                ", destinoNumeroCasa='" + destinoNumeroCasa + '\'' +
                ", creadoPorGuardiaId=" + creadoPorGuardiaId +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", expiraEn=" + expiraEn +
                ", estado='" + estado + '\'' +
                ", creadoEn=" + creadoEn +
                '}';
    }
}
