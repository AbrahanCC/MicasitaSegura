package dao;

import model.Usuario;
import util.DBConnection;
import util.QRUtil;
import util.TokenUtil;
import service.MailService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    // ---------- Mapper ----------
    private Usuario map(ResultSet rs) throws Exception {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setDpi(rs.getString("dpi"));
        u.setNombre(rs.getString("nombre"));
        u.setApellidos(rs.getString("apellidos"));
        u.setCorreo(rs.getString("correo"));
        u.setNumeroCasa(rs.getString("casa"));
        try { u.setLote(rs.getString("lote")); } catch (SQLException ignore) { u.setLote(null); }
        u.setUsername(rs.getString("username"));
        u.setPassHash(rs.getString("password_hash"));
        u.setRolId(rs.getInt("rol_id"));
        try { u.setEstado(rs.getInt("estado")); } catch (SQLException ignore) {}
        u.setActivo(rs.getBoolean("activo"));
        try { u.setRolNombre(rs.getString("rol_nombre")); } catch (SQLException ignore) {} 
        return u;
    }

    // ---------- CRUD ----------
    //Listar usuarios activos
    @Override
    public List<Usuario> listar() {
        List<Usuario> list = new ArrayList<>();
        String sql =
            "SELECT u.id,u.dpi,u.nombre,u.apellidos,u.correo,u.casa,u.lote,u.username,u.password_hash," +
            "u.rol_id,u.activo,r.nombre AS rol_nombre " +
            "FROM usuarios u JOIN roles r ON r.id = u.rol_id " +
            "WHERE u.activo=1 ORDER BY u.id DESC";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //Obtiene un usuario por id FA1 editar
    @Override
    public Usuario obtener(int id) {
        String sql =
            "SELECT u.id,u.dpi,u.nombre,u.apellidos,u.correo,u.casa,u.lote,u.username,u.password_hash," +
            "u.rol_id,u.activo,r.nombre AS rol_nombre " +
            "FROM usuarios u JOIN roles r ON r.id = u.rol_id WHERE u.id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //FA1 RN1, RN2, FA3 tras insertar si el rol es RESIDENTE=3 SE ENVIA QR RN3
    @Override
    public boolean crear(Usuario u) {
        String sql = "INSERT INTO usuarios " +
                     "(dpi,nombre,apellidos,correo,casa,lote,username,password_hash,rol_id,activo) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, u.getDpi());
            ps.setString(i++, u.getNombre());
            ps.setString(i++, u.getApellidos());
            ps.setString(i++, u.getCorreo());
            ps.setString(i++, u.getNumeroCasa());  // RN1: puede ser null si es guardia
            ps.setString(i++, u.getLote());        // RN1: puede ser null si es guardia
            ps.setString(i++, u.getUsername());
            ps.setString(i++, u.getPassHash());
            ps.setInt(i++, u.getRolId());
            ps.setBoolean(i++, u.isActivo());

            boolean ok = ps.executeUpdate() == 1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }

            // RN3 creación y envío de QR si es RESIDENTE (rol_id = 3)
            if (ok && u.getRolId() == 3) {
                try {
                    String token = TokenUtil.generateResidentToken(u.getId());
                    String base  = System.getProperty("APP_BASE_URL", "http://localhost:8080/MiCasitaSegura");
                    String url   = base + "/api/validate?token=" + token;
                    byte[] png   = QRUtil.makeQRPng(url, 400);
                    String body  = "<p>¡Hola!</p>"
                                 + "<p>Se ha generado tu <b>código QR de acceso</b>.</p>"
                                 + "<p><b>Nombre:</b> " + u.getNombre() + " " + u.getApellidos() + "</p>"
                                 + "<p><b>Validez del código QR:</b> Permanente</p>"
                                 + "<p><b>Instrucciones importantes:</b> Guarda este correo o el QR adjunto y preséntalo al ingresar para escaneo.</p>";
                    new MailService().sendWithInlinePng(u.getCorreo(),
                            "Notificación de accesos creados", body, png);
                } catch (Exception ex) {
                    // No detiene FA1; solo registra el error del QR o correo.
                    System.err.println("Error enviando QR: " + ex.getMessage());
                }
            }
            return ok;

        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //Actualiza datos FA1, si viene passHash, actualiza contraseña.
    @Override
    public boolean actualizar(Usuario u) {
        String base = "UPDATE usuarios SET dpi=?, nombre=?, apellidos=?, correo=?, casa=?, lote=?, username=?, rol_id=?, activo=?";
        boolean conPass = u.getPassHash() != null && !u.getPassHash().isEmpty();
        String sql = conPass ? base + ", password_hash=? WHERE id=?" : base + " WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getDpi());
            ps.setString(i++, u.getNombre());
            ps.setString(i++, u.getApellidos());
            ps.setString(i++, u.getCorreo());
            ps.setString(i++, u.getNumeroCasa()); // RN1
            ps.setString(i++, u.getLote());       // RN1
            ps.setString(i++, u.getUsername());
            ps.setInt(i++, u.getRolId());
            ps.setBoolean(i++, u.isActivo());
            if (conPass) ps.setString(i++, u.getPassHash());
            ps.setInt(i, u.getId());
            return ps.executeUpdate() == 1;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // Elimina usuario (FA2)
    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ---------- Login / búsquedas ----------
    @Override
    public Usuario obtenerPorUsuarioOCorreo(String userOrMail) {
        String sql =
            "SELECT u.id,u.dpi,u.nombre,u.apellidos,u.correo,u.casa,u.username,u.password_hash," +
            "u.rol_id,u.estado,u.activo,r.nombre AS rol_nombre " +
            "FROM usuarios u JOIN roles r ON r.id = u.rol_id " +
            "WHERE (u.username=? OR u.correo=?) LIMIT 1";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, userOrMail);
            ps.setString(2, userOrMail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //Búsqueda por correo FA3: duplicados
    @Override
    public Usuario buscarPorCorreo(String correo) {
        String sql =
            "SELECT u.id,u.dpi,u.nombre,u.apellidos,u.correo,u.casa,u.lote,u.username,u.password_hash," +
            "u.rol_id,u.estado,u.activo,r.nombre AS rol_nombre " +
            "FROM usuarios u JOIN roles r ON r.id = u.rol_id " +
            "WHERE u.correo=? LIMIT 1";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //Búsqueda para directorio
    @Override
    public List<Usuario> buscarDirectorio(String nombres, String apellidos, String lote, String numeroCasa) {
        List<Usuario> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT u.id,u.dpi,u.nombre,u.apellidos,u.correo,u.casa,u.lote,u.username,u.password_hash," +
            "u.rol_id,u.estado,u.activo,r.nombre AS rol_nombre " +
            "FROM usuarios u JOIN roles r ON r.id = u.rol_id " +
            "WHERE u.activo=1 AND u.rol_id=3"
        );
        List<Object> params = new ArrayList<>();

        if (nombres != null && !nombres.trim().isEmpty()) {
            sql.append(" AND UPPER(u.nombre) LIKE ?");
            params.add("%" + nombres.trim().toUpperCase() + "%");
        }
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            sql.append(" AND UPPER(u.apellidos) LIKE ?");
            params.add("%" + apellidos.trim().toUpperCase() + "%");
        }
        if (lote != null && !lote.trim().isEmpty()) {
            sql.append(" AND u.lote = ?");
            params.add(lote.trim());
        }
        if (numeroCasa != null && !numeroCasa.trim().isEmpty()) {
            sql.append(" AND u.casa = ?");
            params.add(numeroCasa.trim());
        }
        sql.append(" ORDER BY u.apellidos, u.nombre");

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    //Alias práctico usuario o correo
    @Override
    public Usuario buscarPorIdentificador(String ident) {
        return obtenerPorUsuarioOCorreo(ident);
    }

    //Actualiza hash de contraseña edición FA1
    @Override
    public void actualizarPassword(int id, String nuevoHash) {
        String sql = "UPDATE usuarios SET password_hash=? WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ---------- Catálogo de correos ----------
    //Devuelve correos de residentes activos RN3 notificacion
    @Override
    public List<String> listarCorreosResidentesActivos() {
        String sql = "SELECT correo FROM usuarios " +
                     "WHERE rol_id = 3 AND activo = 1 AND correo IS NOT NULL AND correo <> '' " +
                     "ORDER BY correo";
        List<String> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getString(1));
            return out;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // Obtener ID de residente por casa / lote ----------
    @Override
    public Integer findResidenteId(String numeroCasa, String lote) {
        String sqlConLote =
            "SELECT id FROM usuarios WHERE rol_id=3 AND activo=1 AND casa=? AND lote=? LIMIT 1";
        String sqlSoloCasa =
            "SELECT id FROM usuarios WHERE rol_id=3 AND activo=1 AND casa=? LIMIT 1";

        try (Connection cn = DBConnection.getConnection()) {
            if (lote != null && !lote.trim().isEmpty()) {
                try (PreparedStatement ps = cn.prepareStatement(sqlConLote)) {
                    ps.setString(1, numeroCasa);
                    ps.setString(2, lote);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) return rs.getInt(1);
                    }
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlSoloCasa)) {
                ps.setString(1, numeroCasa);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
