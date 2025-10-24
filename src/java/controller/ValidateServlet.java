package controller;

import dao.AccesoLogDAO;
import dao.AccesoLogDAOImpl;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import dao.VisitanteDAO;
import dao.VisitanteDAOImpl;
import model.Visitante;
import service.GateService;
import util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/api/validate")
public class ValidateServlet extends HttpServlet {

  private final AccesoLogDAO logDao = new AccesoLogDAOImpl();
  private final VisitanteDAO visitanteDao = new VisitanteDAOImpl();
  private final UsuarioDAO usuarioDao = new UsuarioDAOImpl();

  private static final ZoneId TZ = ZoneId.of(System.getProperty("APP_TZ", "America/Guatemala"));
  private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter F_HORA  = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter F_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String token  = trim(req.getParameter("token"));
    String origin = dflt(req.getParameter("origin"), "qr");   // cam|qr|app
    String dir    = trim(req.getParameter("dir"));            // in|out
    String originWithDir = (dir == null || dir.isEmpty()) ? origin : (origin + ":" + dir);

    boolean ok = false;
    String reason = "invalid";
    Integer usedCount = null;   // opcional para UI
    String type = "";           // PERMANENTE | POR_INTENTOS | POR_FECHA

    Integer usuarioIdLog = null;
    Integer visitanteIdLog = null;

    if (isEmpty(token)) {
      reason = "token_missing";
      writeJson(resp, ok, reason, type, usedCount);
      log(token, originWithDir, ok, reason, null, null, null);
      return;
    }

    try {
      // === Token de RESIDENTE ===
      if (token.startsWith("R:")) {
        Integer residenteId = parseResidenteId(token);
        ok = validateResident(token);
        type = "PERMANENTE";
        reason = ok ? "ok_permanente" : "invalid_resident_token";
        usuarioIdLog = residenteId;

        log(token, originWithDir, ok, ok ? null : reason, true, usuarioIdLog, null);
        writeJson(resp, ok, reason, type, usedCount);

        if (ok) {
          try { new GateService().abrir(); } catch (Exception ignore) {}
          notificarAccesoExitosoResidente(residenteId);
        }
        return;
      }

      // === Token de VISITA ===
      Visitante vigente = visitanteDao.obtenerPaseVigentePorToken(token);
      if (vigente != null) {
        visitanteIdLog = vigente.getId();
        usuarioIdLog   = vigente.getUsuarioId();

        type = "por_intentos".equalsIgnoreCase(dflt(vigente.getTipoVisita(), "")) ? "POR_INTENTOS" : "POR_FECHA";

        if ("POR_INTENTOS".equals(type)) {
          boolean consumido = visitanteDao.marcarConsumidoPorToken(token);
          ok = consumido;
          reason = ok ? "ok_por_intentos" : "sin_intentos";
          Integer prev = vigente.getUsosRealizados() == null ? 0 : vigente.getUsosRealizados();
          usedCount = ok ? prev + 1 : prev;
        } else {
          ok = true;
          reason = "ok_por_fecha"; // vigencia por fecha ya validada en el DAO
        }
      } else {
        ok = false;
        reason = "fuera_de_vigencia";
        type = "";
      }

      log(token, originWithDir, ok, ok ? null : reason, false, usuarioIdLog, visitanteIdLog);
      writeJson(resp, ok, reason, type, usedCount);

      if (ok) {
        try { new GateService().abrir(); } catch (Exception ignore) {}
        notificarAccesoExitosoVisita(vigente, type);
      }

    } catch (Exception e) {
      ok = false;
      reason = "server_error";
      writeJson(resp, ok, reason, type, usedCount);
      log(token, originWithDir, ok, reason, null, usuarioIdLog, visitanteIdLog);
    }
  }

  // ===== Notificaciones (RN2) =====

  private void notificarAccesoExitosoResidente(Integer residenteId) {
    try {
      if (residenteId == null) return;
      String correo = usuarioDao.obtenerCorreoPorId(residenteId);
      if (correo == null || correo.isEmpty()) return;

      String nombrePersona = obtenerNombreResidente(residenteId);
      ZonedDateTime now = ZonedDateTime.now(TZ);

      String body = "El código QR generado para la persona " + safe(nombrePersona)
          + " fue utilizado exitosamente el día " + now.format(F_FECHA)
          + " a las " + now.format(F_HORA)
          + " para acceder al condominio. Este código tiene una validez de permanente. "
          + "En caso de cualquier irregularidad, por favor contacte al administrador del sistema.";

      service.Notifications.enqueueEmail(correo, "Notificación de acceso", body,
          "acceso-R-" + residenteId + "-" + now.toEpochSecond());
    } catch (Exception ignore) { /* best-effort */ }
  }

  private void notificarAccesoExitosoVisita(Visitante v, String type) {
    try {
      if (v == null || v.getUsuarioId() == null) return;
      String correo = usuarioDao.obtenerCorreoPorId(v.getUsuarioId());
      if (correo == null || correo.isEmpty()) return;

      String nombrePersona = v.getNombre() != null ? v.getNombre() : "Visitante";
      String validezTxt;
      if ("POR_INTENTOS".equals(type)) {
        Integer max = v.getUsosMax();
        validezTxt = (max != null) ? (max + " intentos") : "por intentos";
      } else {
        if (v.getQrFin() != null) {
          ZonedDateTime zfin = v.getQrFin().toInstant().atZone(TZ);
          validezTxt = "hasta " + zfin.format(F_FECHA_HORA);
        } else {
          validezTxt = "hasta su fecha de vencimiento";
        }
      }

      ZonedDateTime now = ZonedDateTime.now(TZ);
      String body = "El código QR generado para la persona " + safe(nombrePersona)
          + " fue utilizado exitosamente el día " + now.format(F_FECHA)
          + " a las " + now.format(F_HORA)
          + " para acceder al condominio. Este código tiene una validez de " + validezTxt + ". "
          + "En caso de cualquier irregularidad, por favor contacte al administrador del sistema.";

      service.Notifications.enqueueEmail(correo, "Notificación de acceso", body,
          "acceso-V-" + v.getId() + "-" + now.toEpochSecond());
    } catch (Exception ignore) { /* best-effort */ }
  }

  // ===== Util =====

  private Integer parseResidenteId(String token) {
    try { return Integer.parseInt(token.split(":")[1]); } catch (Exception e) { return null; }
  }

  // Token residente: R:<id>:<firma32>
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

  private void log(String token, String origin, boolean ok, String motivo,
                   Boolean esResidente, Integer usuarioId, Integer visitanteId) {
    try {
      model.AccesoLog a = new model.AccesoLog();
      a.setTipo(Boolean.TRUE.equals(esResidente) ? "RESIDENTE" : "VISITA");
      a.setToken(token);
      a.setResultado(ok ? "OK" : "DENEGADO");
      a.setMotivo(motivo);
      a.setOrigin(origin); // p.ej. "cam:in"
      a.setUsuarioId(usuarioId);
      a.setVisitanteId(visitanteId);
      logDao.insertar(a);
    } catch (Exception ignore) {}
  }

  private void writeJson(HttpServletResponse resp, boolean ok, String reason, String type, Integer usedCount) throws IOException {
    resp.setContentType("application/json; charset=UTF-8");
    StringBuilder sb = new StringBuilder();
    sb.append("{\"valid\":").append(ok)
      .append(",\"reason\":\"").append(escape(reason)).append("\"");
    if (type != null && !type.isEmpty()) sb.append(",\"type\":\"").append(escape(type)).append("\"");
    if (usedCount != null) sb.append(",\"used_count\":").append(usedCount);
    sb.append("}");
    resp.getWriter().write(sb.toString());
  }

  private static String trim(String s) { return s == null ? null : s.trim(); }
  private static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
  private static String dflt(String s, String d) { s = trim(s); return isEmpty(s) ? d : s; }
  private static String safe(String s) { return s == null ? "" : s; }
  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String obtenerNombreResidente(Integer residenteId) {
    try {
      model.Usuario u = usuarioDao.obtener(residenteId);
      if (u == null) return "Residente";
      String n = u.getNombre() == null ? "" : u.getNombre().trim();
      String a = u.getApellidos() == null ? "" : u.getApellidos().trim();
      String full = (n + " " + a).trim();
      return full.isEmpty() ? "Residente" : full;
    } catch (Exception e) {
      return "Residente";
    }
  }
}
