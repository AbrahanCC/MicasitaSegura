package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.AreaComun;
import util.DBConnection;

public class AreaComunDAOImpl implements AreaComunDAO {

    private static final String SQL_LISTAR =
        "SELECT id, nombre, activo FROM area_comun WHERE activo = 1 ORDER BY nombre";

    @Override
    public List<AreaComun> listarActivas() throws Exception {
        List<AreaComun> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AreaComun(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getInt("activo") == 1
                ));
            }
        } catch (Exception e) {
            System.err.println("[AreaComunDAOImpl] listarActivas error: " + e.getMessage());
            throw e;
        }
        // log simple de verificación (puedes quitarlo si no lo quieres)
        System.out.println("[AreaComunDAOImpl] areas activas: " + list.size());
        return list;
    }
}
