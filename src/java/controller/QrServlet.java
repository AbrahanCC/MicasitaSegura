package controller;

import util.QRUtil;
import util.TokenUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Genera imágenes PNG de los códigos QR de visitas o residentes.
@WebServlet(urlPatterns = {"/qr", "/api/qr"})
public class QrServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession s = req.getSession(false);
    if (s == null || s.getAttribute("uid") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String tokenParam = trim(req.getParameter("token"));
    String op         = trim(req.getParameter("op"));
    boolean download  = "1".equals(req.getParameter("download"));

    String token;
    if (tokenParam != null && !tokenParam.isEmpty()) {
      token = tokenParam;
    } else if ("me".equalsIgnoreCase(op)) {
      Object uidObj = s.getAttribute("uid");
      if (!(uidObj instanceof Integer)) {
        resp.sendError(400, "uid inválido");
        return;
      }
      int uid = (Integer) uidObj;
      token = TokenUtil.generateResidentToken(uid);
    } else {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parámetros inválidos");
      return;
    }

    // URL completa para validación
    String base = req.getScheme() + "://" + req.getServerName()
        + ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : (":" + req.getServerPort()))
        + req.getContextPath();
    String url = base + "/api/validate?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name());

    try {
      byte[] png = QRUtil.makeQRPng(url, 400);

      if (download) {
        String fn = (tokenParam != null && !tokenParam.isEmpty()) ? "qr-visita.png" : "mi-qr.png";
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fn + "\"");
      }

      resp.setContentType("image/png");
      resp.setContentLength(png.length);
      resp.getOutputStream().write(png);
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo generar el QR");
    }
  }

  private static String trim(String s) {
    return s == null ? null : s.trim();
  }
}