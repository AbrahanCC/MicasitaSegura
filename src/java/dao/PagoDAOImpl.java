package dao;

import model.Pago;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagoDAOImpl implements PagoDAO {

    private static final String BASE_SELECT =
        "SELECT p.id, p.usuario_id, p.tipo_id, pt.nombre AS tipo_nombre, " +
        "p.mes_a_pagar, p.fecha_pago, p.monto_base, p.recargo, p.total, " +
        "p.observaciones, p.payment_method_id, p.metodo, p.status " +
        "FROM pago p JOIN pago_tipo pt ON pt.id = p.tipo_id ";

    private static final String SQL_LISTAR_USUARIO =
        BASE_SELECT + "WHERE p.usuario_id = ? ORDER BY p.fecha_pago DESC";

    private static final String SQL_INSERT =
        "INSERT INTO pago (usuario_id, tipo_id, mes_a_pagar, fecha_pago, " +
        "monto_base, recargo, total, observaciones, payment_method_id, metodo, status, creado_en) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?, NOW())";

    private static final String SQL_MAX_MES =
        "SELECT MAX(mes_a_pagar) FROM pago " +
        "WHERE usuario_id=? AND tipo_id=? AND status='PAGADO'";

    private static final String SQL_OBTENER =
        BASE_SELECT + "WHERE p.id = ?";

    @Override
    public List<Pago> listarPorUsuario(int usuarioId) throws Exception {
        List<Pago> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR_USUARIO)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    @Override
    public int crear(Pago p) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setInt(i++, p.getUsuarioId());
            ps.setInt(i++, p.getTipoId());
            ps.setDate(i++, Date.valueOf(p.getMesAPagar()));
            ps.setTimestamp(i++, Timestamp.valueOf(p.getFechaPago()));
            ps.setBigDecimal(i++, java.math.BigDecimal.valueOf(p.getMontoBase()));
            ps.setBigDecimal(i++, java.math.BigDecimal.valueOf(p.getRecargo()));
            ps.setBigDecimal(i++, java.math.BigDecimal.valueOf(p.getTotal()));
            ps.setString(i++, p.getObservaciones());
            if (p.getMetodoPagoId() == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, p.getMetodoPagoId());
            ps.setString(i++, p.getMetodo());
            ps.setString(i++, p.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public LocalDate ultimoMesPagado(int usuarioId, int tipoId) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_MAX_MES)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, tipoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate(1);
                    return (d != null) ? d.toLocalDate() : null;
                }
            }
        }
        return null;
    }

    @Override
    public Pago obtener(int id) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_OBTENER)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Pago map(ResultSet rs) throws Exception {
        Pago p = new Pago();
        p.setId(rs.getInt("id"));
        p.setUsuarioId(rs.getInt("usuario_id"));
        p.setTipoId(rs.getInt("tipo_id"));
        p.setTipoNombre(rs.getString("tipo_nombre"));
        Date mes = rs.getDate("mes_a_pagar");
        if (mes != null) p.setMesAPagar(mes.toLocalDate());
        Timestamp fp = rs.getTimestamp("fecha_pago");
        if (fp != null) p.setFechaPago(fp.toLocalDateTime());
        p.setMontoBase(rs.getBigDecimal("monto_base").doubleValue());
        p.setRecargo(rs.getBigDecimal("recargo").doubleValue());
        p.setTotal(rs.getBigDecimal("total").doubleValue());
        p.setObservaciones(rs.getString("observaciones"));
        int pm = rs.getInt("payment_method_id");
        p.setMetodoPagoId(rs.wasNull() ? null : pm);
        p.setMetodo(rs.getString("metodo"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}
