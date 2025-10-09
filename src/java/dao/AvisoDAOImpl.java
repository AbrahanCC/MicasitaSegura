package dao;

import model.Aviso;
import util.DBConnection;

import java.sql.*;

public class AvisoDAOImpl implements AvisoDAO {

@Override
    public int crear(Aviso a) {
        String sql = "INSERT INTO aviso(asunto, mensaje, destinatario_tipo, destinatario_email, creado_por) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getAsunto());
            ps.setString(2, a.getMensaje());
            ps.setString(3, a.getDestinatarioTipo());
            if (a.getDestinatarioEmail() == null) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, a.getDestinatarioEmail());
            if (a.getCreadoPor() == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, a.getCreadoPor());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void registrarEnvio(int avisoId, String email, String estado, String detalle) {
        String sql = "INSERT INTO aviso_envio(aviso_id, email, estado, detalle) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, avisoId);
            ps.setString(2, email);
            ps.setString(3, estado);
            if (detalle == null) ps.setNull(4, Types.VARCHAR);
            else ps.setString(4, detalle);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
