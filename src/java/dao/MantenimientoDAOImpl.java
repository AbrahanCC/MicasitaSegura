package dao;

import model.Mantenimiento;
import model.Usuario;
import util.DBConnection;
import service.MailService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MantenimientoDAOImpl implements MantenimientoDAO {

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl(); // para obtener correos de admins

    // --- Mapear ResultSet a objeto Mantenimiento ---
    private Mantenimiento map(ResultSet rs) throws Exception {
        Mantenimiento m = new Mantenimiento();
        m.setId(rs.getInt("id"));
        m.setIdResidente(rs.getInt("id_residente"));
        m.setTipoInconveniente(rs.getString("tipo_inconveniente"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setFechaHora(rs.getTimestamp("fecha_hora"));
        m.setActivo(rs.getBoolean("activo"));

        // Campos opcionales (si la consulta incluye JOIN con usuarios)
        try { m.setNombreResidente(rs.getString("nombre_residente")); } catch (SQLException ignore) {}
        try { m.setNumeroCasa(rs.getString("casa")); } catch (SQLException ignore) {}
        try { m.setLote(rs.getString("lote")); } catch (SQLException ignore) {}

        return m;
    }

    // --- Crear nuevo reporte de mantenimiento ---
    @Override
    public boolean crear(Mantenimiento m) {
        String sql = "INSERT INTO mantenimiento " +
                     "(id_residente, tipo_inconveniente, descripcion, fecha_hora, activo) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, m.getIdResidente());
            ps.setString(2, m.getTipoInconveniente());
            ps.setString(3, m.getDescripcion());
            ps.setTimestamp(4, m.getFechaHora());
            ps.setBoolean(5, m.isActivo());

            boolean ok = ps.executeUpdate() == 1;

            // Obtener el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) m.setId(rs.getInt(1));
            }

            // Si se insertó correctamente, enviar notificación a los administradores
            if (ok) {
                List<String> correosAdmins = usuarioDAO.listarCorreosAdminsActivos();
                if (!correosAdmins.isEmpty()) {
                    new MailService().sendNotificacionMantenimientoToMany(
                        correosAdmins,
                        m.getNombreResidente(),
                        m.getNumeroCasa(),
                        m.getLote(),
                        m.getTipoInconveniente(),
                        m.getFechaHora(),
                        m.getDescripcion()
                    );
                }
            }

            return ok;
        } catch (Exception e) {
            throw new RuntimeException("[Error] crear mantenimiento → " + e.getMessage(), e);
        }
    }

    // --- Obtener un reporte por ID ---
    @Override
    public Mantenimiento obtener(int id) {
        String sql = "SELECT m.*, u.nombre AS nombre_residente, u.casa, u.lote " +
                     "FROM mantenimiento m " +
                     "JOIN usuarios u ON u.id = m.id_residente " +
                     "WHERE m.id = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("[Error] obtener mantenimiento → " + e.getMessage(), e);
        }
    }

    // --- Listar todos los reportes activos ---
    @Override
    public List<Mantenimiento> listar() {
        List<Mantenimiento> list = new ArrayList<>();
        String sql = "SELECT m.*, u.nombre AS nombre_residente, u.casa, u.lote " +
                     "FROM mantenimiento m " +
                     "JOIN usuarios u ON u.id = m.id_residente " +
                     "WHERE m.activo = 1 ORDER BY m.fecha_hora DESC";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (Exception e) {
            throw new RuntimeException("[Error] listar mantenimientos → " + e.getMessage(), e);
        }
    }

    // --- Listar reportes por residente ---
    @Override
    public List<Mantenimiento> listarPorResidente(int idResidente) {
        List<Mantenimiento> list = new ArrayList<>();
        String sql = "SELECT m.*, u.nombre AS nombre_residente, u.casa, u.lote " +
                     "FROM mantenimiento m " +
                     "JOIN usuarios u ON u.id = m.id_residente " +
                     "WHERE m.activo = 1 AND m.id_residente = ? ORDER BY m.fecha_hora DESC";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idResidente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("[Error] listarPorResidente mantenimiento → " + e.getMessage(), e);
        }
    }

    // --- Eliminar reporte ---
    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM mantenimiento WHERE id = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException("[Error] eliminar mantenimiento → " + e.getMessage(), e);
        }
    }
}
