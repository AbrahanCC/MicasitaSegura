package model;

import java.sql.Timestamp;

public class Visitante {

    private int id;
    private String nombre;
    private Integer residenteId;         
    private String dpi;
    private String motivo;
    private Timestamp fechaHora;         
    private String destinoNumeroCasa;
    private Integer creadoPorGuardiaId;

    // Pases / CU3
    private String email;
    private String token;
    private Timestamp expiraEn;           // para visitas por fecha (puede ser null)
    private String estado;                // enum('emitido','activo','consumido','cancelado',...)
    private Timestamp creadoEn;           // timestamp de creación
    private int usedCount;                // usos consumidos
    private String visitType;             // enum('por_intentos','visita')
    private Integer usosMax;              // máximo permitido
    private Timestamp firstUseAt;
    private Timestamp lastUseAt;

    public Visitante() {}

    public Visitante(int id, String nombre, String dpi, String motivo, Timestamp fechaHora,
                     String destinoNumeroCasa, Integer creadoPorGuardiaId,
                     String email, String token, Timestamp expiraEn,
                     String estado, Timestamp creadoEn, int usedCount) {
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
        this.usedCount = usedCount;
    }

    // ===== Getters & Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getResidenteId() { return residenteId; }      // <-- NUEVO
    public void setResidenteId(Integer residenteId) { this.residenteId = residenteId; }

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

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public String getVisitType() { return visitType; }           // opcional
    public void setVisitType(String visitType) { this.visitType = visitType; }

    public Integer getUsosMax() { return usosMax; }              // opcional
    public void setUsosMax(Integer usosMax) { this.usosMax = usosMax; }

    public Timestamp getFirstUseAt() { return firstUseAt; }      // opcional
    public void setFirstUseAt(Timestamp firstUseAt) { this.firstUseAt = firstUseAt; }

    public Timestamp getLastUseAt() { return lastUseAt; }        // opcional
    public void setLastUseAt(Timestamp lastUseAt) { this.lastUseAt = lastUseAt; }

    @Override
    public String toString() {
        return "Visitante{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", residenteId=" + residenteId +
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
                ", usedCount=" + usedCount +
                ", visitType='" + visitType + '\'' +
                ", usosMax=" + usosMax +
                ", firstUseAt=" + firstUseAt +
                ", lastUseAt=" + lastUseAt +
                '}';
    }
}
