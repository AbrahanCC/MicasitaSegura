package model;

public class Usuario {
    private int id;
    private String nombre;
    private String apellidos;
    private String dpi;
    private String numeroCasa;
    private String lote;
    private String correo;
    private String username;
    private String passHash;
    private int rolId;
    private boolean activo;

    public Usuario() { }

    public Usuario(int id, String nombre, String apellidos, String dpi, String numeroCasa, String lote, String correo, String username, String passHash, int rolId, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dpi = dpi;
        this.numeroCasa = numeroCasa;
        this.lote = lote;
        this.correo = correo;
        this.username = username;
        this.passHash = passHash;
        this.rolId = rolId;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDpi() {
        return dpi;
    }
    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getNumeroCasa() {
        return numeroCasa;
    }
    public void setNumeroCasa(String numeroCasa) {
        this.numeroCasa = numeroCasa;
    }
    
    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassHash() {
        return passHash;
    }
    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public int getRolId() {
        return rolId;
    }
    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public boolean isActivo() {
        return activo;
    }
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
