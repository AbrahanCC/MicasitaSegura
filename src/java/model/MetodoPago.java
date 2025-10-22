package model;

import java.sql.Timestamp;

public class MetodoPago {
    private int id;
    private int idUsuario;
    private String marca;
    private String nombreTitular;
    private String ultimos4;
    private int mesExpiracion;
    private int anioExpiracion;
    private String token;                 // opcional (gateway)
    private byte[] panCifrado;            // opcional DEV
    private byte[] vectorInicializacion;  // opcional DEV
    private boolean activo;
    private Timestamp fechaCreacion;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getUltimos4() { return ultimos4; }
    public void setUltimos4(String ultimos4) { this.ultimos4 = ultimos4; }

    public int getMesExpiracion() { return mesExpiracion; }
    public void setMesExpiracion(int mesExpiracion) { this.mesExpiracion = mesExpiracion; }

    public int getAnioExpiracion() { return anioExpiracion; }
    public void setAnioExpiracion(int anioExpiracion) { this.anioExpiracion = anioExpiracion; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public byte[] getPanCifrado() { return panCifrado; }
    public void setPanCifrado(byte[] panCifrado) { this.panCifrado = panCifrado; }

    public byte[] getVectorInicializacion() { return vectorInicializacion; }
    public void setVectorInicializacion(byte[] vectorInicializacion) { this.vectorInicializacion = vectorInicializacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
