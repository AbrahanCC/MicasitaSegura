package dao;

import java.sql.*;
import java.util.*;
import model.Conversacion;
import util.DBConnection;

public class ConversacionDAOImpl implements ConversacionDAO {

    @Override
    public boolean existsActiva(int idResidente, int idGuardia) {
        String sql = "SELECT 1 FROM conversaciones WHERE id_residente=? AND id_guardia=? AND estado='ACTIVA' LIMIT 1";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idResidente);
            ps.setInt(2, idGuardia);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public int countActivasPorGuardia(int idGuardia) {
        String sql = "SELECT COUNT(*) FROM conversaciones WHERE id_guardia=? AND estado='ACTIVA'";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGuardia);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public Conversacion create(Conversacion c) {
        String sql = "INSERT INTO conversaciones (id_residente, id_guardia, estado) VALUES (?,?, 'ACTIVA')";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getIdResidente());
            ps.setInt(2, c.getIdGuardia());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
            return c;
        } catch (SQLIntegrityConstraintViolationException dup) {
            // por la unique de ACTIVA
            throw new RuntimeException("Ya existe una conversación con el usuario seleccionado");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public Conversacion findById(int id) {
        String sql = "SELECT * FROM conversaciones WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Conversacion c = new Conversacion();
                c.setId(rs.getInt("id"));
                c.setIdResidente(rs.getInt("id_residente"));
                c.setIdGuardia(rs.getInt("id_guardia"));
                c.setEstado(rs.getString("estado"));
                c.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                c.setFechaUltimoMensaje(rs.getTimestamp("fecha_ultimo_mensaje"));
                return c;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Conversacion> findActivasByUsuario(int idUsuario) {
        String sql = "SELECT * FROM conversaciones WHERE estado='ACTIVA' AND (id_residente=? OR id_guardia=?) ORDER BY fecha_ultimo_mensaje DESC, fecha_creacion DESC";
        List<Conversacion> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Conversacion c = new Conversacion();
                    c.setId(rs.getInt("id"));
                    c.setIdResidente(rs.getInt("id_residente"));
                    c.setIdGuardia(rs.getInt("id_guardia"));
                    c.setEstado(rs.getString("estado"));
                    c.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                    c.setFechaUltimoMensaje(rs.getTimestamp("fecha_ultimo_mensaje"));
                    list.add(c);
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public void updateFechaUltimoMensaje(int idConversacion) {
        String sql = "UPDATE conversaciones SET fecha_ultimo_mensaje=NOW() WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConversacion);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public void cerrar(int idConversacion) {
        String sql = "UPDATE conversaciones SET estado='CERRADA' WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConversacion);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
