package dao;

import model.MetodoPago;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MetodoPagoDAOImpl implements MetodoPagoDAO {

    private static final String BASE =
        "SELECT id, id_usuario, marca, nombre_titular, ultimos4, " +
        "mes_expiracion, anio_expiracion, token, pan_cifrado, " +
        "vector_inicializacion, activo, fecha_creacion " +
        "FROM metodo_pago ";

    @Override
    public List<MetodoPago> listarActivosPorUsuario(int usuarioId) {
        List<MetodoPago> out = new ArrayList<>();
        String sql = BASE + "WHERE id_usuario=? AND activo=1 ORDER BY fecha_creacion DESC";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    @Override
    public int crear(MetodoPago mp) {
        String sql = "INSERT INTO metodo_pago " +
                "(id_usuario, marca, nombre_titular, ultimos4, " +
                " mes_expiracion, anio_expiracion, token, pan_cifrado, " +
                " vector_inicializacion, activo, fecha_creacion) " +
                "VALUES (?,?,?,?,?,?,?,?,?,1,NOW())";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            ps.setInt(i++, mp.getIdUsuario());
            ps.setString(i++, mp.getMarca());
            ps.setString(i++, mp.getNombreTitular());
            ps.setString(i++, mp.getUltimos4());
            ps.setInt(i++, mp.getMesExpiracion());
            ps.setInt(i++, mp.getAnioExpiracion());
            ps.setString(i++, mp.getToken());
            ps.setBytes(i++, mp.getPanCifrado());
            ps.setBytes(i++, mp.getVectorInicializacion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetodoPago obtener(int id) {
        String sql = BASE + "WHERE id=? LIMIT 1";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MetodoPago map(ResultSet rs) throws Exception {
        MetodoPago m = new MetodoPago();
        m.setId(rs.getInt("id"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setMarca(rs.getString("marca"));
        m.setNombreTitular(rs.getString("nombre_titular"));
        m.setUltimos4(rs.getString("ultimos4"));
        m.setMesExpiracion(rs.getInt("mes_expiracion"));
        m.setAnioExpiracion(rs.getInt("anio_expiracion"));
        m.setToken(rs.getString("token"));
        m.setPanCifrado(rs.getBytes("pan_cifrado"));
        m.setVectorInicializacion(rs.getBytes("vector_inicializacion"));
        m.setActivo(rs.getBoolean("activo"));
        m.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        return m;
    }
}
