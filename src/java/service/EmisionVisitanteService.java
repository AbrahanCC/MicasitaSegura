package service;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class EmisionVisitanteService {

  /**
   * Crea un pase de visitante con token de un solo uso.
   * @return token generado (para armar la URL del QR)
   */
  public String emitir(String nombre, String email, String motivo, String destinoNumeroCasa, int ttlMin) throws Exception {
    String token = UUID.randomUUID().toString();

    String sql = "INSERT INTO visitantes " +
                 "(nombre, email, motivo, destino_numero_casa, token, expira_en, estado) " +
                 "VALUES (?,?,?,?,?, NOW() + INTERVAL ? MINUTE, 'emitido')";

    try (Connection c = DBConnection.getConnectionStatic();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setString(1, nombre);
      ps.setString(2, email);
      ps.setString(3, motivo);
      ps.setString(4, destinoNumeroCasa);
      ps.setString(5, token);
      ps.setInt(6, ttlMin);

      ps.executeUpdate();
    }

    return token;
  }
}
