package model;

public class AccesoLog {
  private int id;
  private String tipo;        // VISITA | RESIDENTE
  private Integer visitanteId;
  private Integer usuarioId;
  private String token;
  private String resultado;   // OK | DENEGADO | ERROR
  private String motivo;      // texto corto
  private String origin;      // "qr"

  // Getters/Setters
  public String getTipo() { return tipo; }
  public void setTipo(String tipo) { this.tipo = tipo; }
  public Integer getVisitanteId() { return visitanteId; }
  public void setVisitanteId(Integer visitanteId) { this.visitanteId = visitanteId; }
  public Integer getUsuarioId() { return usuarioId; }
  public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
  public String getToken() { return token; }
  public void setToken(String token) { this.token = token; }
  public String getResultado() { return resultado; }
  public void setResultado(String resultado) { this.resultado = resultado; }
  public String getMotivo() { return motivo; }
  public void setMotivo(String motivo) { this.motivo = motivo; }
  public String getOrigin() { return origin; }
  public void setOrigin(String origin) { this.origin = origin; }
}
