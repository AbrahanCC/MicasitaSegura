package dao;

import model.Visitante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAOImpl implements VisitanteDAO {

    // -------------------------
    // MAPPER
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
        v.setExpiraEn(rs.getTimestamp("expira_en"));   // alias de qr_fin
        v.setEstado(rs.getString("estado"));           // 'emitido'/'consumido' según qr_activo
        v.setCreadoEn(rs.getTimestamp("creado_en"));   // COALESCE en SELECT
        v.setUsedCount(rs.getInt("used_count"));       // calculado/alias
        return v;
    }

    // -------------------------
    // CREAR
    // -------------------------
    @Override
    public boolean crear(Visitante v) {
        String tipo;
        Integer intentos = null;
        Timestamp fin = null;

        if (v.getExpiraEn() != null) {
            tipo = "FECHA";
            fin = (v.getExpiraEn() instanceof Timestamp)
                    ? (Timestamp) v.getExpiraEn()
                    : new Timestamp(v.getExpiraEn().getTime());
        } else {
            tipo = "INTENTOS";
            int usados = Math.max(0, v.getUsedCount());
            intentos = Math.max(0, 2 - usados); // por defecto 2 usos totales
        }

        StringBuilder cols = new StringBuilder("nombre,dpi,motivo,destino_numero_casa,qr_tipo,qr_activo");
        StringBuilder vals = new StringBuilder("?,?,?,?,?,?");
        List<Object> params = new ArrayList<>();

        params.add(v.getNombre());
        params.add(v.getDpi());
        params.add(v.getMotivo());
        params.add(v.getDestinoNumeroCasa());
        params.add(tipo);
        params.add(1); // activo por defecto al emitir

        if (v.getEmail() != null && !v.getEmail().trim().isEmpty()) {
            cols.append(",email"); vals.append(",?"); params.add(v.getEmail().trim());
        }
        if (v.getToken() != null && !v.getToken().trim().isEmpty()) {
            cols.append(",token"); vals.append(",?"); params.add(v.getToken().trim());
        }
        if ("FECHA".equals(tipo)) {
            cols.append(",qr_fin"); vals.append(",?"); params.add(fin);
        } else { // INTENTOS
            cols.append(",qr_intentos"); vals.append(",?"); params.add(intentos);
        }

        String sql = "INSERT INTO visitantes(" + cols + ") VALUES (" + vals + ")";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int i = 1;
            for (Object p : params) {
                if (p instanceof java.util.Date && !(p instanceof java.sql.Timestamp)) {
                    ps.setTimestamp(i++, new java.sql.Timestamp(((java.util.Date) p).getTime()));
                } else {
                    ps.setObject(i++, p);
                }
            }
            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new RuntimeException("Token ya existe para otro visitante.", dup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi) {
        List<Visitante> out = new ArrayList<>();

        String usedCountExpr =
            "CASE WHEN qr_tipo='INTENTOS' THEN GREATEST(0, 2 - IFNULL(qr_intentos,0)) ELSE 0 END AS used_count";

        StringBuilder sql = new StringBuilder(
            "SELECT id AS id, nombre, dpi, motivo, destino_numero_casa, " +
            "       email, token, " +
            "       qr_fin AS expira_en, " +
            "       CASE WHEN qr_activo=1 THEN 'emitido' ELSE 'consumido' END AS estado, " +
            "       COALESCE(creado_en, NOW()) AS creado_en, " +
            "       " + usedCountExpr + " " +
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

        try (Connection cn = new DBConnection().getConnection();
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

 
    @Override
    public Visitante obtener(int id) {
        String usedCountExpr =
            "CASE WHEN qr_tipo='INTENTOS' THEN GREATEST(0, 2 - IFNULL(qr_intentos,0)) ELSE 0 END AS used_count";

        String sql =
            "SELECT id AS id, nombre, dpi, motivo, destino_numero_casa, " +
            "       email, token, " +
            "       qr_fin AS expira_en, " +
            "       CASE WHEN qr_activo=1 THEN 'emitido' ELSE 'consumido' END AS estado, " +
            "       COALESCE(creado_en, NOW()) AS creado_en, " +
            "       " + usedCountExpr + " " +
            "FROM visitantes WHERE id=? LIMIT 1";

        try (Connection cn = new DBConnection().getConnection();
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
    // PASE POR TOKEN
    // -------------------------
    @Override
    public Visitante obtenerPaseVigentePorToken(String token) {
        String usedCountExpr =
            "CASE WHEN qr_tipo='INTENTOS' THEN GREATEST(0, 2 - IFNULL(qr_intentos,0)) ELSE 0 END AS used_count";

        String sql =
            "SELECT id AS id, nombre, dpi, motivo, destino_numero_casa, " +
            "       email, token, " +
            "       qr_fin AS expira_en, " +
            "       CASE WHEN qr_activo=1 THEN 'emitido' ELSE 'consumido' END AS estado, " +
            "       COALESCE(creado_en, NOW()) AS creado_en, " +
            "       " + usedCountExpr + " " +
            "FROM visitantes " +
            "WHERE token=? AND qr_activo=1 AND (" +
            "    (qr_tipo='PERMANENTE') OR " +
            "    (qr_tipo='INTENTOS' AND IFNULL(qr_intentos,0) > 0) OR " +
            "    (qr_tipo='FECHA' AND (qr_fin IS NULL OR qr_fin >= NOW()))" +
            ") " +
            "LIMIT 1";

        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean marcarConsumidoPorToken(String token) {
        String sql =
            "UPDATE visitantes " +
            "SET qr_intentos = CASE WHEN qr_tipo='INTENTOS' AND qr_intentos IS NOT NULL THEN qr_intentos - 1 ELSE qr_intentos END, " +
            "    qr_activo   = CASE WHEN qr_tipo='INTENTOS' AND (qr_intentos - 1) <= 0 THEN 0 ELSE qr_activo END " +
            "WHERE token=? AND qr_activo=1";

        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            int n = ps.executeUpdate();
            return n >= 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean aprobar(int idVisitante, Integer modificadoPor) {
        final String sql = "UPDATE visitantes SET qr_activo=1, modificado_por=? WHERE id=?";
        try (Connection cn = new DBConnection().getConnection();
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
        final String sql = "UPDATE visitantes SET qr_activo=0, modificado_por=? WHERE id=?";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, modificadoPor);
            ps.setInt(2, idVisitante);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
