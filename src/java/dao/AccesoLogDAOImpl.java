package dao;

import model.AccesoLog;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AccesoLogDAOImpl implements AccesoLogDAO {
  @Override
  public void insertar(AccesoLog a) {
    String sql = "INSERT INTO accesos_log(tipo,visitante_id,usuario_id,token,resultado,motivo,origin)"
               + " VALUES(?,?,?,?,?,?,?)";
    try (Connection c = DBConnection.getConnectionStatic();
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
}

