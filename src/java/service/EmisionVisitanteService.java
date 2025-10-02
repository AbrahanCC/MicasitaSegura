package service;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class EmisionVisitanteService {

  private Integer findResidenteId(Connection c, String destino) throws Exception {
    if (destino == null) return null;
    destino = destino.trim();

    String lote = null, casa = null;
    String d = destino.replace(" ", "");
    int dash = d.indexOf('-');
    if (dash >= 0) {
      lote = d.substring(0, dash).toUpperCase();
      casa = d.substring(dash + 1);
    } else if (d.length() >= 2) {
      lote = d.substring(0, 1).toUpperCase();
      casa = d.substring(1);
    }

    System.out.println("DEBUG: destino=" + destino + " -> lote=" + lote + " casa=" + casa);

    if (lote == null || casa == null) return null;

    String sql = "SELECT id FROM usuarios " +
                 "WHERE rol_id=2 AND activo=1 AND lote=? AND casa=? " +
                 "ORDER BY id DESC LIMIT 1";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, lote);
      ps.setString(2, casa);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getInt(1) : null;
      }
    }
  }

  public String emitir(String nombre,
                       String email,
                       String motivo,
                       String destinoNumeroCasa,
                       int ttlMin,
                       Integer creadoPorGuardiaId) throws Exception {

    String token = UUID.randomUUID().toString();

    try (Connection c = DBConnection.getConnectionStatic()) {
      Integer residenteId = findResidenteId(c, destinoNumeroCasa);
      if (residenteId == null) {
        throw new IllegalStateException("No existe residente activo para la casa " + destinoNumeroCasa);
      }

      String sql = "INSERT INTO visitantes " +
          "(id_usuario, nombre, email, motivo, destino_numero_casa, token, expira_en, estado, creado_por_guardia_id) " +
          "VALUES (?,?,?,?,?,?, TIMESTAMPADD(MINUTE, ?, NOW()), 'emitido', ?)";
      try (PreparedStatement ps = c.prepareStatement(sql)) {
        int i = 1;
        ps.setInt(i++, residenteId);
        ps.setString(i++, nombre);
        ps.setString(i++, email);
        ps.setString(i++, motivo);
        ps.setString(i++, destinoNumeroCasa);
        ps.setString(i++, token);
        ps.setInt(i++, ttlMin);
        if (creadoPorGuardiaId == null) {
          ps.setObject(i++, null);
        } else {
          ps.setInt(i++, creadoPorGuardiaId);
        }
        ps.executeUpdate();
      }
    }

    return token;
  }
}
