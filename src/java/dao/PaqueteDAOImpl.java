package dao;

import model.Paquete;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaqueteDAOImpl implements PaqueteDAO {

    private Paquete map(ResultSet rs) throws Exception {
        Paquete p = new Paquete();
        p.setId(rs.getLong("id"));
        p.setNumeroGuia(rs.getString("numero_guia"));
        p.setDestinatarioId(rs.getInt("destinatario_id"));
        p.setCasaDestinatario(rs.getString("casa_destinatario"));
        p.setLoteDestinatario(rs.getString("lote_destinatario"));
        p.setEstado(rs.getString("estado"));
        p.setRecibidoPor(rs.getInt("recibido_por"));
        Timestamp fr = rs.getTimestamp("fecha_recepcion");
        if (fr != null) p.setFechaRecepcion(fr.toLocalDateTime());
        Timestamp fe = rs.getTimestamp("fecha_entrega");
        if (fe != null) p.setFechaEntrega(fe.toLocalDateTime());
        int entregadoPor = rs.getInt("entregado_por");
        p.setEntregadoPor(rs.wasNull() ? null : entregadoPor);
        p.setObservaciones(rs.getString("observaciones"));
        return p;
    }

    private static final String SQL_INSERT =
        "INSERT INTO paquete (numero_guia, destinatario_id, casa_destinatario, lote_destinatario, " +
        "estado, recibido_por, fecha_recepcion, observaciones) " +
        "VALUES (?, ?, ?, ?, 'PENDIENTE', ?, NOW(), ?)";

    @Override
    public long crear(Paquete p) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            ps.setString(i++, p.getNumeroGuia());
            ps.setInt(i++, p.getDestinatarioId());
            ps.setString(i++, p.getCasaDestinatario());
            ps.setString(i++, p.getLoteDestinatario());
            ps.setInt(i++, p.getRecibidoPor());
            ps.setString(i++, p.getObservaciones());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0L;
    }

    private static final String BASE_SELECT =
        "SELECT p.* FROM paquete p ";

    // Filtro sobre numero_guia, casa/lote (snapshot) y nombre/apellidos actuales del usuario
    private static final String SQL_LISTAR_PENDIENTES =
        BASE_SELECT +
        "JOIN usuarios u ON u.id = p.destinatario_id " +
        "WHERE p.estado='PENDIENTE' " +
        "AND ( ? IS NULL OR ? = '' " +
        "      OR p.numero_guia LIKE CONCAT('%', ?, '%') " +
        "      OR u.nombre LIKE CONCAT('%', ?, '%') " +
        "      OR u.apellidos LIKE CONCAT('%', ?, '%') " +
        "      OR p.casa_destinatario = ? " +
        "      OR p.lote_destinatario = ? ) " +
        "ORDER BY p.fecha_recepcion DESC";

    @Override
    public List<Paquete> listarPendientes(String filtro) throws Exception {
        List<Paquete> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR_PENDIENTES)) {
            for (int i = 1; i <= 7; i++) ps.setString(i, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    private static final String SQL_ENTREGAR =
        "UPDATE paquete SET estado='ENTREGADO', fecha_entrega=NOW(), entregado_por=? " +
        "WHERE id=? AND estado='PENDIENTE'";

    @Override
    public boolean marcarEntregado(long paqueteId, int guardiaId) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_ENTREGAR)) {
            ps.setInt(1, guardiaId);
            ps.setLong(2, paqueteId);
            return ps.executeUpdate() == 1;
        }
    }

    private static final String SQL_GET =
        "SELECT * FROM paquete WHERE id=?";

    @Override
    public Paquete obtener(long id) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_GET)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }
}
