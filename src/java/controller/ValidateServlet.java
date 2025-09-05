package controller;

import dao.AccesoLogDAO;
import dao.AccesoLogDAOImpl;
import model.AccesoLog;
import service.GateService;
import util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

// Residente: ilimitado. Visita: 2 usos. Registra bitácora. Abre talanquera si OK.
@WebServlet("/api/validate")
public class ValidateServlet extends HttpServlet {

  private final AccesoLogDAO logDao = new AccesoLogDAOImpl();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String token = req.getParameter("token");
    String originParam = req.getParameter("origin"); // in|out|qr|null
    String origin = (originParam == null || originParam.trim().isEmpty()) ? "qr" : originParam.trim();

    boolean ok = false;
    String reason = "invalid";

    if (token != null && !token.trim().isEmpty()) {
      try {
        if (token.startsWith("R:")) {
          ok = validateResident(token);
          reason = ok ? "ok" : "invalid_resident_token";
        } else {
          ok = logDao.consumeOneUseIfAvailable(token, 2);
          reason = ok ? "ok" : "uses_exhausted_or_invalid";
        }
      } catch (Exception e) {
        reason = "server_error";
      }
    } else {
      reason = "token_missing";
    }

    // Bitácora
    try {
      AccesoLog log = new AccesoLog();
      log.setTipo(token != null && token.startsWith("R:") ? "RESIDENTE" : "VISITA");
      log.setToken(token);
      log.setResultado(ok ? "OK" : "DENEGADO");
      log.setMotivo(ok ? null : reason);
      log.setOrigin(origin);
      logDao.insertar(log);
    } catch (Exception ignored) {}

    // Apertura
    if (ok) {
      try { new GateService().abrir(); } catch (Exception ignored) {}
    }

    // Respuesta
    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write("{\"valid\":" + ok + ",\"reason\":\"" + reason + "\"}");
  }

  private boolean validateResident(String token) {
    try {
      String[] parts = token.split(":");
      if (parts.length != 3) return false;
      int id = Integer.parseInt(parts[1]);
      String firma = parts[2];
      String secret = System.getProperty("RESIDENT_SECRET", "residentes2025");
      String expected = PasswordUtil.sha256(secret + ":" + id).substring(0, 32);
      return expected.equals(firma);
    } catch (Exception e) {
      return false;
    }
  }
}