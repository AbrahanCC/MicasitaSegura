package dao;

import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResidenteDAOImpl implements ResidenteDAO {

    @Override
    public List<String> listarEmailsResidentes() {
        List<String> emails = new ArrayList<>();
        // Ajusta el filtro de rol si lo deseas (ej. rolId=3). Por ahora: usuarios activos con correo.
        String sql = "SELECT correo FROM usuario WHERE correo IS NOT NULL AND correo <> '' AND activo = 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) emails.add(rs.getString(1));
        } catch (Exception e) { e.printStackTrace(); }
        return emails;
    }
}
