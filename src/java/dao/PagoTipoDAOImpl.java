package dao;

import model.PagoTipo;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoTipoDAOImpl implements PagoTipoDAO {

    private static final String BASE =
        "SELECT id, codigo, nombre, monto, activo FROM pago_tipo ";

    @Override
    public List<PagoTipo> listarActivos() {
        List<PagoTipo> out = new ArrayList<>();
        String sql = BASE + "WHERE activo=1 ORDER BY nombre";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public PagoTipo obtenerPorCodigo(String codigo) {
        String sql = BASE + "WHERE codigo=? AND activo=1 LIMIT 1";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public PagoTipo obtenerPorId(int id) {
        String sql = BASE + "WHERE id=? LIMIT 1";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private PagoTipo map(ResultSet rs) throws Exception {
        PagoTipo t = new PagoTipo();
        t.setId(rs.getInt("id"));
        t.setCodigo(rs.getString("codigo"));
        t.setNombre(rs.getString("nombre"));
        t.setMonto(rs.getDouble("monto"));
        t.setActivo(rs.getBoolean("activo"));
        return t;
    }
}
