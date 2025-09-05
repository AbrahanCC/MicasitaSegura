package dao;

import model.Usuario;
import util.DBConnection;
import util.QRUtil;
import util.PasswordUtil;
import util.TokenUtil;            
import service.MailService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    // Mapea una fila a Usuario
    private Usuario map(ResultSet rs) throws Exception {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setDpi(rs.getString("dpi"));
        u.setNombre(rs.getString("nombre"));
        u.setApellidos(rs.getString("apellidos"));
        u.setCorreo(rs.getString("correo"));
        u.setNumeroCasa(rs.getString("numero_casa"));
        u.setLote(rs.getString("lote"));
        u.setUsername(rs.getString("username"));
        u.setPassHash(rs.getString("password_hash"));
        u.setRolId(rs.getInt("rol_id"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> list = new ArrayList<>();
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,lote,username,password_hash,rol_id,activo " +
                     "FROM usuarios WHERE activo=1 ORDER BY id DESC";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Usuario obtener(int id) {
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,lote,username,password_hash,rol_id,activo FROM usuarios WHERE id=?";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Crea usuario. Acepta numero_casa / lote NULL para guardia (RN1)
    @Override
    public boolean crear(Usuario u) {
        String sql = "INSERT INTO usuarios(dpi,nombre,apellidos,correo,numero_casa,lote,username,password_hash,rol_id,activo) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, u.getDpi());
            ps.setString(i++, u.getNombre());
            ps.setString(i++, u.getApellidos());
            ps.setString(i++, u.getCorreo());
            ps.setString(i++, u.getNumeroCasa()); // puede ser null
            ps.setString(i++, u.getLote());       // puede ser null
            ps.setString(i++, u.getUsername());
            ps.setString(i++, u.getPassHash());
            ps.setInt(i++, u.getRolId());
            ps.setBoolean(i++, u.isActivo());

            boolean ok = ps.executeUpdate() == 1;

            // ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }

            // RN3: enviar QR solo a RESIDENTE (rol_id = 2)
            if (ok && u.getRolId() == 2) {
                try {
                    // ← MISMO token que servirá /qr (centralizado)
                    String token = TokenUtil.generateResidentToken(u.getId());

                    // Base configurable (evita hardcode). Defaults: http://localhost:8080/MiCasitaSegura
                    String base = System.getProperty("APP_BASE_URL", "http://localhost:8080/MiCasitaSegura");
                    String url  = base + "/api/validate?token=" + token;

                    byte[] png = QRUtil.makeQRPng(url, 400);

                    String body = "<p>¡Hola!</p>"
                            + "<p>Se ha generado exitosamente tu <b>código QR de acceso</b> al residencial.</p>"
                            + "<p><b>Nombre del Residente:</b> " + u.getNombre() + " " + u.getApellidos() + "</p>"
                            + "<p><b>Validez del código QR:</b> ILIMITADO</p>"
                            + "<p><b>Instrucciones importantes:</b><br>"
                            + "Guarda este correo o el QR adjunto.<br>"
                            + "Preséntalo al llegar al residencial para que el personal de seguridad lo valide.</p>";

                    new MailService().sendWithInlinePng(u.getCorreo(),
                            "Notificación de accesos creados", body, png);
                } catch (Exception ex) {
                    System.err.println("Error enviando QR residente: " + ex.getMessage());
                }
            }
            return ok;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean actualizar(Usuario u) {
        String base = "UPDATE usuarios SET dpi=?, nombre=?, apellidos=?, correo=?, numero_casa=?, lote=?, username=?, rol_id=?, activo=?";
        boolean conPass = u.getPassHash() != null && !u.getPassHash().isEmpty();
        String sql = conPass ? base + ", password_hash=? WHERE id=?" : base + " WHERE id=?";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getDpi());
            ps.setString(i++, u.getNombre());
            ps.setString(i++, u.getApellidos());
            ps.setString(i++, u.getCorreo());
            ps.setString(i++, u.getNumeroCasa()); // puede ser null
            ps.setString(i++, u.getLote());       // puede ser null
            ps.setString(i++, u.getUsername());
            ps.setInt(i++, u.getRolId());
            ps.setBoolean(i++, u.isActivo());
            if (conPass) ps.setString(i++, u.getPassHash());
            ps.setInt(i, u.getId());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id=?";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Usuario obtenerPorUsuarioOCorreo(String userOrMail) {
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,lote,username,password_hash,rol_id,activo " +
                     "FROM usuarios WHERE (username=? OR correo=?) LIMIT 1";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, userOrMail);
            ps.setString(2, userOrMail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,lote,username,password_hash,rol_id,activo FROM usuarios WHERE correo=? LIMIT 1";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Usuario> buscarDirectorio(String nombres, String apellidos, String lote, String numeroCasa) {
        List<Usuario> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id,dpi,nombre,apellidos,correo,numero_casa,lote,username,password_hash,rol_id,activo FROM usuarios WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (nombres != null && !nombres.trim().isEmpty()) { sql.append(" AND nombre LIKE ?"); params.add("%" + nombres.trim() + "%"); }
        if (apellidos != null && !apellidos.trim().isEmpty()) { sql.append(" AND apellidos LIKE ?"); params.add("%" + apellidos.trim() + "%"); }
        if (lote != null && !lote.trim().isEmpty()) { sql.append(" AND lote = ?"); params.add(lote.trim()); }
        if (numeroCasa != null && !numeroCasa.trim().isEmpty()) { sql.append(" AND numero_casa = ?"); params.add(numeroCasa.trim()); }
        sql.append(" ORDER BY apellidos, nombre");
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    @Override
    public Usuario buscarPorIdentificador(String ident) {
        return obtenerPorUsuarioOCorreo(ident);
    }

    @Override
    public void actualizarPassword(int id, String nuevoHash) {
        String sql = "UPDATE usuarios SET password_hash=? WHERE id=?";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
