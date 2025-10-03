package dao;

import java.sql.*;
import model.Incidente;
import util.DBConnection;

public class IncidenteDAOImpl implements IncidenteDAO {

    @Override
    public Incidente create(Incidente inc) {
        String sql = "INSERT INTO incidentes (id_residente, tipo, fecha_hora, descripcion) VALUES (?,?,?,?)";
        try (Connection cn = DBConnection.getConnectionStatic();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inc.getIdResidente());
            ps.setString(2, inc.getTipo());
            ps.setTimestamp(3, inc.getFechaHora());
            ps.setString(4, inc.getDescripcion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) inc.setId(rs.getInt(1));
            }
            return inc;
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
