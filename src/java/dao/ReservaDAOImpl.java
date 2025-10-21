package dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Reserva;
import util.DBConnection;

public class ReservaDAOImpl implements ReservaDAO {

    private static final String BASE_SELECT =
        "SELECT r.id, r.area_id, a.nombre AS area_nombre, r.usuario_id, r.fecha, " +
        "r.hora_inicio, r.hora_fin, r.estado " +
        "FROM reserva r JOIN area_comun a ON a.id = r.area_id ";

    // 👉 YA NO FILTRA POR ESTADO. Trae TODAS (CREADA y CANCELADA)
    private static final String SQL_LISTAR_USUARIO =
        BASE_SELECT + "WHERE r.usuario_id = ? " +
        "ORDER BY r.fecha DESC, r.hora_inicio DESC";

    // Para validar disponibilidad, solo consideramos reservas activas (CREADAS)
    private static final String SQL_SOLAPE =
        "SELECT COUNT(*) FROM reserva r " +
        "WHERE r.area_id = ? AND r.fecha = ? AND r.estado = 'CREADA' " +
        "AND ? < r.hora_fin AND ? > r.hora_inicio";

    private static final String SQL_INSERT =
        "INSERT INTO reserva (area_id, usuario_id, fecha, hora_inicio, hora_fin, estado) " +
        "VALUES (?, ?, ?, ?, ?, 'CREADA')";

    // Cancelación lógica (no borra)
    private static final String SQL_CANCELAR =
        "UPDATE reserva SET estado='CANCELADA' WHERE id=?";

    @Override
    public List<Reserva> listarPorUsuario(int usuarioId) throws Exception {
        List<Reserva> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR_USUARIO)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva r = new Reserva();
                    r.setId(rs.getInt("id"));
                    r.setAreaId(rs.getInt("area_id"));
                    r.setAreaNombre(rs.getString("area_nombre"));
                    r.setUsuarioId(rs.getInt("usuario_id"));
                    r.setFecha(rs.getDate("fecha").toLocalDate());
                    r.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
                    r.setHoraFin(rs.getTime("hora_fin").toLocalTime());
                    r.setEstado(rs.getString("estado"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    @Override
    public boolean existeSolapamiento(int areaId, LocalDate fecha, LocalTime ini, LocalTime fin) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_SOLAPE)) {
            ps.setInt(1, areaId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(ini));
            ps.setTime(4, Time.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public int crear(Reserva r) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getAreaId());
            ps.setInt(2, r.getUsuarioId());
            ps.setDate(3, Date.valueOf(r.getFecha()));
            ps.setTime(4, Time.valueOf(r.getHoraInicio()));
            ps.setTime(5, Time.valueOf(r.getHoraFin()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public void cancelar(int reservaId) throws Exception {
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_CANCELAR)) {
            ps.setInt(1, reservaId);
            ps.executeUpdate();
        }
    }
}
