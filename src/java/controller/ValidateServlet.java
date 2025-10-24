package controller;

import dao.AccesoLogDAO;
import dao.AccesoLogDAOImpl;
import dao.VisitanteDAO;
import dao.VisitanteDAOImpl;
import model.Visitante;
import service.GateService;
import util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

// Valida tokens de residente/visita y abre talanquera
@WebServlet("/api/validate")
public class ValidateServlet extends HttpServlet {

  // Bitácora de accesos
  private final AccesoLogDAO logDao = new AccesoLogDAOImpl();
  // Chequeo de vigencia y consumo de uso
  private final VisitanteDAO visitanteDao = new VisitanteDAOImpl();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String token  = trim(req.getParameter("token"));
    String origin = dflt(req.getParameter("origin"), "qr"); // cam|qr|app

    boolean ok = false;
    String reason = "invalid";
    Integer usedCount = null; // devuelve conteo para feedback del escáner

    if (isEmpty(token)) {
      reason = "token_missing";
      writeJson(resp, ok, reason, usedCount);
      log(token, origin, ok, reason, null);
      return;
    }

    try {
      if (token.startsWith("R:")) {
        // Residente: ilimitado
        ok = validateResident(token);
        reason = ok ? "ok" : "invalid_resident_token";
        log(token, origin, ok, ok ? null : reason, true);
      } else {
        // Visita: vigente por intentos/fecha
        Visitante vigente = visitanteDao.obtenerPaseVigentePorToken(token);
        if (vigente != null) {
          ok = visitanteDao.marcarConsumidoPorToken(token);
          reason = ok ? "ok" : "no_se_pudo_consumir";
          // sumar 1 localmente si se consumió (evitamos otro SELECT)
          usedCount = ok ? (safeInt(vigente.getUsosRealizados()) + 1) : safeInt(vigente.getUsosRealizados());
        } else {
          ok = false;
          reason = "no_vigente";
        }
        log(token, origin, ok, ok ? null : reason, false);
      }
    } catch (Exception e) {
      ok = false;
      reason = "server_error";
      log(token, origin, ok, reason, null);
    }

    writeJson(resp, ok, reason, usedCount);

    if (ok) {
      try { new GateService().abrir(); } catch (Exception ignore) {}
    }
  }

  // --- util ---

  // Token residente: R:<id>:<firma32>, firma = sha256(secret+":"+id)[0..31]
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

  private void log(String token, String origin, boolean ok, String motivo, Boolean esResidente) {
    try {
      model.AccesoLog a = new model.AccesoLog();
      a.setTipo(Boolean.TRUE.equals(esResidente) ? "RESIDENTE" : "VISITA");
      a.setToken(token);
      a.setResultado(ok ? "OK" : "DENEGADO");
      a.setMotivo(motivo);
      a.setOrigin(origin);
      logDao.insertar(a);
    } catch (Exception ignore) {}
  }

  private void writeJson(HttpServletResponse resp, boolean ok, String reason, Integer usedCount) throws IOException {
    resp.setContentType("application/json; charset=UTF-8");
    String extra = (usedCount == null) ? "" : ",\"used_count\":" + usedCount;
    resp.getWriter().write("{\"valid\":" + ok + ",\"reason\":\"" + reason + "\"" + extra + "}");
  }

  private static String trim(String s) { return s == null ? null : s.trim(); }
  private static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
  private static String dflt(String s, String d) { s = trim(s); return isEmpty(s) ? d : s; }
  private static int safeInt(Integer n) { return n == null ? 0 : n; }
}