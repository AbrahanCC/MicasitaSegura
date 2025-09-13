package controller;
//controla la talanquera//
import dao.AccesoLogDAO;
import dao.AccesoLogDAOImpl;
import model.AccesoLog;
import service.GateService;
import util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/validate")
public class ValidateServlet extends HttpServlet {

  private final AccesoLogDAO logDao = new AccesoLogDAOImpl();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String token  = str(req.getParameter("token"));
    String origin = def(req.getParameter("origin"), "qr");

    boolean ok = false;
    String reason = "invalid";

    if (!isEmpty(token)) {
      try {
        if (token.startsWith("R:")) {
          ok = validateResident(token);                 // residentes: ilimitado
          reason = ok ? "ok" : "invalid_resident_token";
        } else {
          ok = logDao.consumeOneUseIfAvailable(token, 2); // visitas: 2 usos
          reason = ok ? "ok" : "uses_exhausted_or_invalid";
        }
      } catch (Exception e) {
        reason = "server_error";
      }
    } else {
      reason = "token_missing";
    }

    // bitácora (sin dir; origin opcional: cam/qr/app)
    try {
      AccesoLog log = new AccesoLog();
      log.setTipo(token != null && token.startsWith("R:") ? "RESIDENTE" : "VISITA");
      log.setToken(token);
      log.setResultado(ok ? "OK" : "DENEGADO");
      log.setMotivo(ok ? null : reason);
      log.setOrigin(origin);
      logDao.insertar(log);
    } catch (Exception ignore) {}

    // abrir talanquera cuando es válido
    if (ok) {
      try { new GateService().abrir(); } catch (Exception ignore) {}
    }

    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write("{\"valid\":" + ok + ",\"reason\":\"" + reason + "\"}");
  }

  // --- helpers ---
  private static String str(String s) { return s == null ? null : s.trim(); }
  private static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
  private static String def(String s, String d) { s = str(s); return isEmpty(s) ? d : s; }

  // token residente: R:<id>:<firma32>, firma= sha256(secret+":"+id)[0..31]
  private boolean validateResident(String token) {
    try {
      String[] p = token.split(":");
      if (p.length != 3) return false;
      int id = Integer.parseInt(p[1]);
      String firma = p[2];
      String secret = System.getProperty("RESIDENT_SECRET", "residentes2025");
      String expected = PasswordUtil.sha256(secret + ":" + id).substring(0, 32);
      return expected.equals(firma);
    } catch (Exception e) { return false; }
  }
}
