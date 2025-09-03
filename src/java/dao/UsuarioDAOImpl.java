package dao;

import model.Usuario;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    private Usuario map(ResultSet rs) throws Exception {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setDpi(rs.getString("dpi"));
        u.setNombre(rs.getString("nombre"));
        u.setApellidos(rs.getString("apellidos"));
        u.setCorreo(rs.getString("correo"));
        u.setNumeroCasa(rs.getString("numero_casa"));
        u.setUsername(rs.getString("username"));
        u.setPassHash(rs.getString("password_hash"));
        u.setRolId(rs.getInt("rol_id"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> list = new ArrayList<>();
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,username,password_hash,rol_id,activo FROM usuarios WHERE activo=1 ORDER BY id DESC";
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
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,username,password_hash,rol_id,activo FROM usuarios WHERE id=?";
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

    @Override
    public boolean crear(Usuario u) {
        String sql = "INSERT INTO usuarios(dpi,nombre,apellidos,correo,numero_casa,username,password_hash,rol_id,activo) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getDpi());
            ps.setString(i++, u.getNombre());
            ps.setString(i++, u.getApellidos());
            ps.setString(i++, u.getCorreo());
            ps.setString(i++, u.getNumeroCasa());
            ps.setString(i++, u.getUsername());
            ps.setString(i++, u.getPassHash());
            ps.setInt(i++, u.getRolId());
            ps.setBoolean(i++, u.isActivo());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean actualizar(Usuario u) {
        String base = "UPDATE usuarios SET dpi=?, nombre=?, apellidos=?, correo=?, numero_casa=?, username=?, rol_id=?, activo=?";
        boolean conPass = u.getPassHash() != null && !u.getPassHash().isEmpty();
        String sql = conPass ? base + ", password_hash=? WHERE id=?" : base + " WHERE id=?";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getDpi());
            ps.setString(i++, u.getNombre());
            ps.setString(i++, u.getApellidos());
            ps.setString(i++, u.getCorreo());
            ps.setString(i++, u.getNumeroCasa());
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
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,username,password_hash,rol_id,activo FROM usuarios WHERE (username=? OR correo=?) LIMIT 1";
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
        String sql = "SELECT id,dpi,nombre,apellidos,correo,numero_casa,username,password_hash,rol_id,activo FROM usuarios WHERE correo=? LIMIT 1";
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
            "SELECT id,dpi,nombre,apellidos,correo,numero_casa,username,password_hash,rol_id,activo FROM usuarios WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (nombres != null && !nombres.trim().isEmpty()) {
            sql.append(" AND nombre LIKE ?");
            params.add("%" + nombres.trim() + "%");
        }
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            sql.append(" AND apellidos LIKE ?");
            params.add("%" + apellidos.trim() + "%");
        }
        if (lote != null && !lote.trim().isEmpty() && numeroCasa != null && !numeroCasa.trim().isEmpty()) {
            sql.append(" AND CONCAT(UPPER(SUBSTRING_INDEX(numero_casa,'-',1)), '-', CAST(SUBSTRING_INDEX(numero_casa,'-',-1) AS UNSIGNED)) = CONCAT(UPPER(?), '-', CAST(? AS UNSIGNED))");
            params.add(lote.trim());
            params.add(numeroCasa.trim());
        }
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
