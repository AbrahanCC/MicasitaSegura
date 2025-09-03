package dao;

import model.Visitante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAOImpl implements VisitanteDAO {

    private Visitante map(ResultSet rs) throws Exception {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setMotivo(rs.getString("motivo"));
        v.setFechaHora(rs.getTimestamp("fecha_hora"));
        v.setDestinoNumeroCasa(rs.getString("destino_numero_casa"));
        // nuevos
        v.setEmail(rs.getString("email"));
        v.setToken(rs.getString("token"));
        v.setExpiraEn(rs.getTimestamp("expira_en"));
        v.setEstado(rs.getString("estado")); // "emitido", "consumido", "caducado"
        v.setCreadoEn(rs.getTimestamp("creado_en"));
        
        Object guardia = rs.getObject("creado_por_guardia_id");
        v.setCreadoPorGuardiaId(guardia == null ? null : ((Number)guardia).intValue());
        return v;
    }

    @Override
    public boolean crear(Visitante v) {
        // INSERT dinámico: incluimos solo columnas con valor no-nulo para respetar DEFAULTs
        StringBuilder cols = new StringBuilder("nombre,dpi,motivo,destino_numero_casa,creado_por_guardia_id");
        StringBuilder vals = new StringBuilder("?,?,?,?,?");
        List<Object> params = new ArrayList<>();

        params.add(v.getNombre());
        params.add(v.getDpi());
        params.add(v.getMotivo());
        params.add(v.getDestinoNumeroCasa());
        params.add(v.getCreadoPorGuardiaId());

        if (v.getFechaHora() != null) {
            cols.append(",fecha_hora");
            vals.append(",?");
            params.add(v.getFechaHora());
        }
        if (v.getEmail() != null && !v.getEmail().trim().isEmpty()) {
            cols.append(",email");
            vals.append(",?");
            params.add(v.getEmail().trim());
        }
        if (v.getToken() != null && !v.getToken().trim().isEmpty()) {
            cols.append(",token");
            vals.append(",?");
            params.add(v.getToken().trim());
        }
        if (v.getExpiraEn() != null) {
            cols.append(",expira_en");
            vals.append(",?");
            params.add(v.getExpiraEn());
        }
        if (v.getEstado() != null && !v.getEstado().trim().isEmpty()) {
            cols.append(",estado");
            vals.append(",?");
            params.add(v.getEstado().trim()); // emitido|consumido|caducado
        }

        String sql = "INSERT INTO visitantes(" + cols + ") VALUES (" + vals + ")";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int i = 1;
            for (Object p : params) {
                if (p instanceof Timestamp) ps.setTimestamp(i++, (Timestamp) p);
                else if (p == null) ps.setNull(i++, Types.VARCHAR);
                else ps.setObject(i++, p);
            }
            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // token duplicado (uq_token)
            throw new RuntimeException("Token ya existe para otro visitante.", dup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi) {
        List<Visitante> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id,nombre,dpi,motivo,fecha_hora,destino_numero_casa," +
            "email,token,expira_en,estado,creado_en,creado_por_guardia_id " +
            "FROM visitantes WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (desde != null && !desde.trim().isEmpty()) {
            sql.append(" AND fecha_hora >= ?");
            params.add(Timestamp.valueOf(desde.trim() + " 00:00:00"));
        }
        if (hasta != null && !hasta.trim().isEmpty()) {
            sql.append(" AND fecha_hora <= ?");
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

        sql.append(" ORDER BY fecha_hora DESC");

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

    /** Buscar un pase válido por token (no caducado ni consumido) */
    public Visitante obtenerPaseVigentePorToken(String token) {
        String sql = "SELECT id,nombre,dpi,motivo,fecha_hora,destino_numero_casa," +
                     "email,token,expira_en,estado,creado_en,creado_por_guardia_id " +
                     "FROM visitantes " +
                     "WHERE token=? AND estado='emitido' AND (expira_en IS NULL OR expira_en >= NOW()) " +
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

    /** Marcar como consumido un pase */
    public boolean marcarConsumidoPorToken(String token) {
        String sql = "UPDATE visitantes SET estado='consumido' WHERE token=? AND estado='emitido'";
        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
