package controller;

import service.EmisionVisitanteService;
import util.QRUtil;
import service.MailService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/emit")
public class EmitirVisitaServlet extends HttpServlet {
  private final EmisionVisitanteService emision = new EmisionVisitanteService();
  private final MailService mail = new MailService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String nombre  = req.getParameter("nombre");
    String email   = req.getParameter("email");
    String motivo  = req.getParameter("motivo");
    String destino = req.getParameter("destino"); // número de casa
    int ttl = 10; // minutos

    try {
      // 1) Generar y guardar token
      String token = emision.emitir(nombre, email, motivo, destino, ttl);

      // 2) Armar URL absoluta para validar
      String base = req.getScheme() + "://" + req.getServerName() +
                    ((req.getServerPort()==80 || req.getServerPort()==443) ? "" : ":" + req.getServerPort()) +
                    req.getContextPath();
      String url = base + "/api/validate?token=" + token;

      // 3) Generar QR y enviar correo
      byte[] png = QRUtil.makeQRPng(url, 300);
      String body = "<p>Hola " + (nombre==null? "visitante" : nombre) + ".</p>"
          + "<p>Tu pase vence en " + ttl + " minutos.</p>"
          + "<p>Si no puedes escanear, abre esta URL: <a href=\"" + url + "\">" + url + "</a></p>";
      mail.sendWithInlinePng(email, "Pase de visita", body, png);

      // 4) Responder OK
      resp.setContentType("application/json; charset=UTF-8");
      resp.getWriter().write("{\"ok\":true,\"token\":\"" + token + "\"}");
    } catch (Exception ex) {
      resp.setStatus(500);
      resp.setContentType("application/json; charset=UTF-8");
      resp.getWriter().write("{\"ok\":false,\"error\":\"" + ex.getMessage() + "\"}");
    }
  }
}
