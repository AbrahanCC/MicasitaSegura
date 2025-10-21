package dao;

import java.sql.*;
import java.util.*;
import model.Mensaje;
import util.DBConnection;

public class MensajeDAOImpl implements MensajeDAO {

    @Override
    public Mensaje create(Mensaje m) {
        String sql = "INSERT INTO mensajes (id_conversacion, id_emisor, contenido) VALUES (?,?,?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getIdConversacion());
            ps.setInt(2, m.getIdEmisor());
            ps.setString(3, m.getContenido());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) m.setId(rs.getInt(1));
            }
            return m;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Mensaje> findByConversacion(int idConversacion, int limit, int offset) {
        String sql = "SELECT * FROM mensajes WHERE id_conversacion=? ORDER BY fecha_envio ASC LIMIT ? OFFSET ?";
        List<Mensaje> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConversacion);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mensaje m = new Mensaje();
                    m.setId(rs.getInt("id"));
                    m.setIdConversacion(rs.getInt("id_conversacion"));
                    m.setIdEmisor(rs.getInt("id_emisor"));
                    m.setContenido(rs.getString("contenido"));
                    m.setFechaEnvio(rs.getTimestamp("fecha_envio"));
                    m.setLeido(rs.getBoolean("leido"));
                    list.add(m);
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public void marcarLeidos(int idConversacion, int userId) {
        // Marca como leídos los mensajes de la conversación enviados por "el otro"
        String sql = "UPDATE mensajes SET leido=1 " +
                     "WHERE id_conversacion=? AND id_emisor<>? AND leido=0";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConversacion);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
