package dao;

import model.Visitante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAOImpl implements VisitanteDAO {

    // -------------------------
    // MAPPER (usa alias del SELECT)
    // -------------------------
    private Visitante map(ResultSet rs) throws Exception {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setMotivo(rs.getString("motivo"));
        v.setDestinoNumeroCasa(rs.getString("destino_numero_casa"));
        v.setEmail(rs.getString("email"));
        v.setToken(rs.getString("token"));
        v.setExpiraEn(rs.getTimestamp("expira_en"));     // alias de qr_fin
        v.setEstado(rs.getString("estado"));             // enum: emitido/activo/consumido/cancelado
        v.setCreadoEn(rs.getTimestamp("creado_en"));     // timestamp
        v.setUsedCount(rs.getInt("used_count"));         // usado desde columna/expresión
        return v;
    }

    // -------------------------
    // CREAR (ajustado a tu esquema)
    // -------------------------
    @Override
    public boolean crear(Visitante v) {
        // Insert mínimo con columnas que existen en tu tabla
        // nombre, dpi, motivo, destino_numero_casa, (opc) email, (opc) token, (opc) qr_fin, estado
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
            else ps.setTimestamp(7, (v.getExpiraEn() instanceof Timestamp)
                    ? (Timestamp) v.getExpiraEn()
                    : new Timestamp(v.getExpiraEn().getTime()));

            ps.setString(8, "emitido"); // estado inicial

            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new RuntimeException("Token ya existe para otro visitante.", dup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------
    // LISTAR (ajustado a columnas reales)
    // -------------------------
    @Override
    public List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi) {
        List<Visitante> out = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT id, nombre, dpi, motivo, destino_numero_casa, " +
            "       email, token, " +
            "       qr_fin AS expira_en, " +                 // alias
            "       estado, " +
            "       COALESCE(creado_en, NOW()) AS creado_en, " +
            "       COALESCE(used_count, 0) AS used_count " + // columna existe en tu tabla
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

    // -------------------------
    // OBTENER POR ID
    // -------------------------
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

    // -------------------------
    // VALIDACIÓN POR TOKEN (vigente)
    // -------------------------
    @Override
    public Visitante obtenerPaseVigentePorToken(String token) {
        // vigente si: estado en ('emitido','activo') y
        //   (visit_type='por_intentos' y used_count < usos_max)  OR
        //   (visit_type='visita' y (qr_fin IS NULL o qr_fin>=NOW()))
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
            "  AND ( (visit_type='por_intentos' AND COALESCE(used_count,0) < COALESCE(usos_max,2)) " +
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

    // -------------------------
    // CONSUMIR UN USO POR TOKEN
    // -------------------------
    @Override
    public boolean marcarConsumidoPorToken(String token) {
        // incrementa used_count y, si se alcanzó el tope para por_intentos, marca 'consumido'
        String sql =
            "UPDATE visitantes " +
            "SET last_use_at = NOW(), " +
            "    used_count = COALESCE(used_count,0) + 1, " +
            "    estado = CASE " +
            "       WHEN visit_type='por_intentos' " +
            "         AND (COALESCE(used_count,0) + 1) >= COALESCE(usos_max,2) " +
            "       THEN 'consumido' " +
            "       ELSE estado END " +
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

    // -------------------------
    // APROBAR/RECHAZAR (usa 'estado')
    // -------------------------
    @Override
    public boolean aprobar(int idVisitante, Integer modificadoPor) {
        final String sql = "UPDATE visitantes SET estado='activo', modificado_por=? WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, modificadoPor);
            ps.setInt(2, idVisitante);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean rechazar(int idVisitante, Integer modificadoPor) {
        final String sql = "UPDATE visitantes SET estado='cancelado', modificado_por=? WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, modificadoPor);
            ps.setInt(2, idVisitante);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
