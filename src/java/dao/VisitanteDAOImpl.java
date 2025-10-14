package dao;

import model.Visitante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAOImpl implements VisitanteDAO {

    // Mapea una fila a Visitante
    private Visitante map(ResultSet rs) throws Exception {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setMotivo(rs.getString("motivo"));
        v.setDestinoNumeroCasa(rs.getString("destino_numero_casa"));
        v.setEmail(rs.getString("email"));
        v.setToken(rs.getString("token"));
        v.setExpiraEn(rs.getTimestamp("expira_en"));
        v.setEstado(rs.getString("estado"));
        v.setCreadoEn(rs.getTimestamp("creado_en"));
        v.setUsedCount(rs.getInt("used_count"));
        return v;
    }

    // Crear visitante (estado inicial: emitido)
    @Override
    public boolean crear(Visitante v) {
        String sql = "INSERT INTO visitantes " +
                "(nombre, dpi, motivo, destino_numero_casa, email, token, qr_fin, estado) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, v.getNombre());
            ps.setString(2, v.getDpi());
            ps.setString(3, v.getMotivo());
            ps.setString(4, v.getDestinoNumeroCasa());

            if (v.getEmail() == null || v.getEmail().trim().isEmpty()) ps.setNull(5, Types.VARCHAR);
            else ps.setString(5, v.getEmail().trim());

            if (v.getToken() == null || v.getToken().trim().isEmpty()) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, v.getToken().trim());

            if (v.getExpiraEn() == null) ps.setNull(7, Types.TIMESTAMP);
            else if (v.getExpiraEn() instanceof Timestamp) ps.setTimestamp(7, (Timestamp) v.getExpiraEn());
            else ps.setTimestamp(7, new Timestamp(v.getExpiraEn().getTime()));

            ps.setString(8, "emitido");
            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new RuntimeException("Token ya existe para otro visitante.", dup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Listar con filtros básicos
    @Override
    public List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi) {
        List<Visitante> out = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT id, nombre, dpi, motivo, destino_numero_casa, " +
            "       email, token, " +
            "       qr_fin AS expira_en, " +
            "       estado, " +
            "       COALESCE(creado_en, NOW()) AS creado_en, " +
            "       COALESCE(used_count, 0) AS used_count " +
            "FROM visitantes WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (desde != null && !desde.trim().isEmpty()) {
            sql.append(" AND COALESCE(creado_en, NOW()) >= ?");
            params.add(Timestamp.valueOf(desde.trim() + " 00:00:00"));
        }
        if (hasta != null && !hasta.trim().isEmpty()) {
            sql.append(" AND COALESCE(creado_en, NOW()) <= ?");
            params.add(Timestamp.valueOf(hasta.trim() + " 23:59:59"));
        }
        if (destinoNumeroCasa != null && !destinoNumeroCasa.trim().isEmpty()) {
            sql.append(" AND destino_numero_casa = ?");
            params.add(destinoNumeroCasa.trim());
        }
        if (dpi != null && !dpi.trim().isEmpty()) {
            sql.append(" AND dpi = ?");
            params.add(dpi.trim());
        }

        sql.append(" ORDER BY COALESCE(creado_en, NOW()) DESC");

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    // Obtener por id
    @Override
    public Visitante obtener(int id) {
        String sql =
            "SELECT id, nombre, dpi, motivo, destino_numero_casa, " +
            "       email, token, " +
            "       qr_fin AS expira_en, " +
            "       estado, " +
            "       COALESCE(creado_en, NOW()) AS creado_en, " +
            "       COALESCE(used_count, 0) AS used_count " +
            "FROM visitantes WHERE id=? LIMIT 1";
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

    // Pase vigente por token
    @Override
    public Visitante obtenerPaseVigentePorToken(String token) {
        String sql =
            "SELECT id, nombre, dpi, motivo, destino_numero_casa, " +
            "       email, token, " +
            "       qr_fin AS expira_en, " +
            "       estado, " +
            "       COALESCE(creado_en, NOW()) AS creado_en, " +
            "       COALESCE(used_count, 0) AS used_count " +
            "FROM visitantes " +
            "WHERE token=? " +
            "  AND estado IN ('emitido','activo') " +
            "  AND COALESCE(used_count,0) < COALESCE(usos_max,2) " +
            "  AND ( visit_type='por_intentos' " +
            "     OR (visit_type='visita' AND (qr_fin IS NULL OR qr_fin >= NOW())) ) " +
            "LIMIT 1";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Consumir un uso y marcar 'consumido' si llega al tope
    @Override
    public boolean marcarConsumidoPorToken(String token) {
        String sql =
            "UPDATE visitantes " +
            "SET first_use_at = COALESCE(first_use_at, NOW()), " +
            "    last_use_at  = NOW(), " +
            "    used_count   = COALESCE(used_count,0) + 1, " +
            "    estado = CASE WHEN (COALESCE(used_count,0) + 1) >= COALESCE(usos_max,2) " +
            "                  THEN 'consumido' ELSE estado END " +
            "WHERE token=? AND estado IN ('emitido','activo')";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            int n = ps.executeUpdate();
            return n >= 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Aprobar (cambia a activo) — sin modificado_por
    @Override
    public boolean aprobar(int idVisitante, Integer modificadoPor) {
        final String sql = "UPDATE visitantes SET estado='activo' WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVisitante);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Rechazar (cambia a cancelado) — sin modificado_por
    @Override
    public boolean rechazar(int idVisitante, Integer modificadoPor) {
        final String sql = "UPDATE visitantes SET estado='cancelado' WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVisitante);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
