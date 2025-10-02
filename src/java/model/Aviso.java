package model;

public class Aviso {
    private int id;
    private String asunto;
    private String mensaje;
    private String destinatarioTipo;  
    private String destinatarioEmail;
    private Integer creadoPor;        

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getDestinatarioTipo() { return destinatarioTipo; }
    public void setDestinatarioTipo(String destinatarioTipo) { this.destinatarioTipo = destinatarioTipo; }
    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }
    public Integer getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Integer creadoPor) { this.creadoPor = creadoPor; }
}
