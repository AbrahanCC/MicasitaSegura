package controller;

import util.QRUtil;
import util.TokenUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//genera PNG del QR para validar en /api/validate */
@WebServlet("/qr")
public class QrServlet extends HttpServlet {

  //GET: token (visita) o op=me (residente) + download=1 opcional */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // Sesión
    HttpSession s = req.getSession(false);
    if (s == null || s.getAttribute("uid") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Parámetros
    String tokenParam = trim(req.getParameter("token")); // visita
    String op         = trim(req.getParameter("op"));    // "me" residente
    boolean download  = "1".equals(req.getParameter("download"));

    // Determinar token
    String token;
    if (tokenParam != null && !tokenParam.isEmpty()) {
      // QR de visita por token explícito
      token = tokenParam;
    } else if ("me".equalsIgnoreCase(op)) {
      // QR de residente (token estable)
      Object uidObj = s.getAttribute("uid");
      if (!(uidObj instanceof Integer)) { resp.sendError(400, "uid inválido"); return; }
      int uid = (Integer) uidObj;
      token = TokenUtil.generateResidentToken(uid);
    } else {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parámetros inválidos");
      return;
    }

    // URL absoluta de validación
    String base = req.getScheme() + "://" + req.getServerName()
        + ((req.getServerPort()==80 || req.getServerPort()==443) ? "" : (":" + req.getServerPort()))
        + req.getContextPath();
    String url = base + "/api/validate?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name());

    try {
      // Genera PNG
      byte[] png = QRUtil.makeQRPng(url, 400);

      // Descarga opcional
      if (download) {
        String fn = (tokenParam != null && !tokenParam.isEmpty()) ? "qr-visita.png" : "mi-qr.png";
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fn + "\"");
      }

      // Respuesta
      resp.setContentType("image/png");
      resp.setContentLength(png.length);
      resp.getOutputStream().write(png);

    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo generar el QR");
    }
  }

  // Utilidad
  private static String trim(String s) { return s == null ? null : s.trim(); }
}
