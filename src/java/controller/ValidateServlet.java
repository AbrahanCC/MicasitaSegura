package controller;

import util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import dao.AccesoLogDAO;
import dao.AccesoLogDAOImpl;
import model.AccesoLog;
import service.GateService;



@WebServlet("/api/validate")
public class ValidateServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String token = req.getParameter("token");
    boolean ok = false;
    String reason = "invalid";

    if (token != null && !token.trim().isEmpty()) {
      String select = "SELECT id FROM visitantes WHERE token=? AND estado='emitido' AND expira_en > NOW()";
      String update = "UPDATE visitantes SET estado='consumido' WHERE id=?";

      try (Connection c = DBConnection.getConnectionStatic();
           PreparedStatement ps = c.prepareStatement(select)) {

        ps.setString(1, token);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            int id = rs.getInt(1);
            try (PreparedStatement up = c.prepareStatement(update)) {
              up.setInt(1, id);
              up.executeUpdate();
              ok = true;
              reason = "ok";
            }
          } else {
            reason = "expired_or_used_or_not_found";
          }
        }
      } catch (Exception e) {
        reason = "server_error";
      }
    }
    
    // 1) Registrar en bitácora
try {
  AccesoLogDAO logDao = new AccesoLogDAOImpl();
  AccesoLog log = new AccesoLog();
  log.setTipo("VISITA");
  log.setToken(token);
  log.setResultado(ok ? "OK" : "DENEGADO");
  log.setMotivo(ok ? null : reason);
  log.setOrigin("qr");
  logDao.insertar(log);
} catch (Exception e) {
  // si falla la bitácora no rompemos la validación
}

// 2) Si es válido, abrir talanquera
if (ok) {
  boolean gateOk = new GateService().abrir();
  if (!gateOk) {
    // opcional: podrías actualizar la bitácora con motivo "fallo_esp"
    // o añadir un segundo registro con resultado=ERROR
  }
}

    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write("{\"valid\":" + ok + ",\"reason\":\"" + reason + "\"}");
    
    // === Abrir talanquera si el visitante es válido ===
if (ok) {
  try {
    boolean gateOk = new service.GateService().abrir();
    if (!gateOk) {
      System.err.println("No se pudo abrir la talanquera (ESP no respondió 200).");
    }
  } catch (Exception ex) {
    System.err.println("Error abriendo talanquera: " + ex.getMessage());
  }
}


  }
}
