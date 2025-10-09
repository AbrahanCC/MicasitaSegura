package dao;

import model.Rol;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDAOImpl implements RolDAO {
    @Override
    public List<Rol> listar() {
        List<Rol> out = new ArrayList<>();
        String sql = "SELECT id, nombre FROM roles ORDER BY nombre";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rol r = new Rol();
                r.setId(rs.getInt("id"));
                r.setNombre(rs.getString("nombre"));
                out.add(r);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
