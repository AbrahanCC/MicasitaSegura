package dao;

import model.Visitante;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAOImpl implements VisitanteDAO {

    // Mapeador de fila visitantes
    private Visitante map(ResultSet rs) throws Exception {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        try { v.setUsuarioId((Integer) rs.getObject("usuario_id")); } catch (SQLException ignore) {}
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setMotivo(rs.getString("motivo"));
        v.setCorreo(rs.getString("correo"));
        v.setToken(rs.getString("token"));
        v.setEstado(rs.getString("estado"));
        v.setTipoVisita(rs.getString("tipo_visita"));
        v.setLote(rs.getString("lote"));
        v.setCasa(rs.getString("casa"));
        v.setPrimerUsoEn(rs.getTimestamp("primer_uso_en"));
        v.setUltimoUsoEn(rs.getTimestamp("ultimo_uso_en"));
        v.setQrFin(rs.getTimestamp("qr_fin"));
        v.setCreadoEn(rs.getTimestamp("creado_en"));
        try { v.setUsosRealizados((Integer) rs.getObject("usos_realizados")); } catch (SQLException ignore) {}
        try { v.setUsosMax((Integer) rs.getObject("usos_max")); } catch (SQLException ignore) {}
        return v;
    }

    // Crear visitante estado inicial emitido
    @Override
    public boolean crear(Visitante v) {
        final String sql =
            "INSERT INTO visitantes " +
            "(usuario_id, nombre, dpi, motivo, correo, token, estado, tipo_visita, lote, casa, usos_max, usos_realizados, qr_fin) " +
            "VALUES (?,?,?,?,?,?, 'emitido', ?, ?, ?, ?, 0, ?)";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int i = 1;
            if (v.getUsuarioId() == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, v.getUsuarioId());
            ps.setString(i++, v.getNombre());
            if (v.getDpi() == null || v.getDpi().isEmpty()) ps.setNull(i++, Types.VARCHAR); else ps.setString(i++, v.getDpi());
            ps.setString(i++, v.getMotivo());
            if (v.getCorreo() == null || v.getCorreo().isEmpty()) ps.setNull(i++, Types.VARCHAR); else ps.setString(i++, v.getCorreo());
            ps.setString(i++, v.getToken());
            ps.setString(i++, v.getTipoVisita());                 // tipo_visita
            ps.setString(i++, v.getLote());
            ps.setString(i++, v.getCasa());
            if (v.getUsosMax() == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, v.getUsosMax());
            if (v.getQrFin() == null) ps.setNull(i, Types.TIMESTAMP); else ps.setTimestamp(i, v.getQrFin());

            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new RuntimeException("Token ya existe para otro visitante.", dup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Listar visitantes con filtros básicos
    @Override
    public List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi) {
        List<Visitante> out = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT id, usuario_id, nombre, dpi, motivo, correo, token, estado, tipo_visita, " +
            "       lote, casa, primer_uso_en, ultimo_uso_en, qr_fin, creado_en, usos_realizados, usos_max " +
            "FROM visitantes WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // rango por creado_en
        if (desde != null && !desde.trim().isEmpty()) {
            sql.append(" AND creado_en >= ?");
            params.add(Timestamp.valueOf(desde.trim() + " 00:00:00"));
        }
        if (hasta != null && !hasta.trim().isEmpty()) {
            sql.append(" AND creado_en <= ?");
            params.add(Timestamp.valueOf(hasta.trim() + " 23:59:59"));
        }

        // filtro por destino "LOTE-CASA" si viene así
        String[] lc = splitDestino(destinoNumeroCasa);
        if (lc != null) {
            sql.append(" AND lote = ? AND casa = ?");
            params.add(lc[0]);
            params.add(lc[1]);
        }

        if (dpi != null && !dpi.trim().isEmpty()) {
            sql.append(" AND dpi = ?");
            params.add(dpi.trim());
        }

        sql.append(" ORDER BY creado_en DESC");

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

    //Obtener visitante por id 
    @Override
    public Visitante obtener(int id) {
        final String sql =
            "SELECT id, usuario_id, nombre, dpi, motivo, correo, token, estado, tipo_visita, " +
            "       lote, casa, primer_uso_en, ultimo_uso_en, qr_fin, creado_en, usos_realizados, usos_max " +
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
    final String sql =
        "SELECT id, usuario_id, nombre, dpi, motivo, correo, token, estado, tipo_visita, " +
        "       lote, casa, primer_uso_en, ultimo_uso_en, qr_fin, creado_en, usos_realizados, usos_max " +
        "FROM visitantes " +
        "WHERE token=? " +
        "  AND estado IN ('emitido','activo') " +
        "  AND ( " +
        "        (tipo_visita='por_intentos' AND COALESCE(usos_realizados,0) < COALESCE(usos_max,2)) " +
        "     OR (tipo_visita='visita'       AND (qr_fin IS NULL OR qr_fin >= NOW())) " +
        "      ) " +
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

    // onsumir un uso y marcar 'consumido' si llega al tope 
    @Override
    public boolean marcarConsumidoPorToken(String token) {
        final String sql =
            "UPDATE visitantes " +
            "SET primer_uso_en = COALESCE(primer_uso_en, NOW()), " +
            "    ultimo_uso_en = NOW(), " +
            "    usos_realizados = COALESCE(usos_realizados,0) + 1, " +
            "    estado = CASE WHEN (COALESCE(usos_realizados,0) + 1) >= COALESCE(usos_max,2) " +
            "                  THEN 'consumido' ELSE estado END " +
            "WHERE token=? AND estado IN ('emitido','activo')";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeUpdate() >= 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Aprobar cambia a activo
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

    // Rechazar cambia a cancelado
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

    // === Catálogos (VISITA, CASA, LOTE) ===
    @Override
    public List<String> catalogoVisita() { return listarCodigosCatalogo("VISITA"); }

    @Override
    public List<String> catalogoCasas() { return listarCodigosCatalogo("CASA"); }

    @Override
    public List<String> catalogoLotes() { return listarCodigosCatalogo("LOTE"); }

    // --- util local ---
    private String[] splitDestino(String destino) {
        if (destino == null) return null;
        String x = destino.trim();
        if (x.isEmpty()) return null;
        int p = x.indexOf('-');
        if (p < 0) return null;
        String lote = x.substring(0, p).trim();
        String casa = x.substring(p + 1).trim();
        if (lote.isEmpty() || casa.isEmpty()) return null;
        return new String[]{lote, casa};
    }

    // Consulta genérica de catálogo por código
    private List<String> listarCodigosCatalogo(String codigoCatalogo) {
        final String sql =
            "SELECT ec.codigo " +
            "FROM elemento_catalogo ec " +
            "JOIN catalogo c ON c.id_catalogo = ec.id_catalogo " +
            "WHERE c.codigo = ? AND ec.activo = 1 " +
            "ORDER BY ec.orden, ec.nombre";
        List<String> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigoCatalogo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
