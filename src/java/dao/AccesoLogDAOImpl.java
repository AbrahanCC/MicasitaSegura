package dao;

import model.AccesoLog;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AccesoLogDAOImpl implements AccesoLogDAO {

  @Override
  public void insertar(AccesoLog a) {
    String sql = "INSERT INTO accesos_log(tipo,visitante_id,usuario_id,token,resultado,motivo,origin) "
               + "VALUES(?,?,?,?,?,?,?)";
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, a.getTipo());
      if (a.getVisitanteId() == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, a.getVisitanteId());
      if (a.getUsuarioId()  == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, a.getUsuarioId());
      ps.setString(4, a.getToken());
      ps.setString(5, a.getResultado());
      ps.setString(6, a.getMotivo());
      ps.setString(7, a.getOrigin() == null ? "qr" : a.getOrigin());
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean consumeOneUseIfAvailable(String token, int maxUses) {
    //Incrementa el contador de usos
    //El token está en estado emitido
    //Todavía no alcanzó el máximo permitido
    //Si al sumar llega al límite, cambia estado a consumido
    final String sql =
        "UPDATE visitantes " +
        "   SET used_count = COALESCE(used_count,0) + 1, " +
        "       estado = CASE WHEN COALESCE(used_count,0) + 1 >= ? THEN 'consumido' ELSE estado END " +
        " WHERE token = ? " +
        "   AND estado = 'emitido' " +
        "   AND COALESCE(used_count,0) < ?";

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setInt(1, maxUses);  
      ps.setString(2, token); 
      ps.setInt(3, maxUses);  
      int updated = ps.executeUpdate();
      return updated > 0; // true si logró consumir un uso
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
