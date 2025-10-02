package controller;

import service.EmisionVisitanteService;
import util.QRUtil;
import service.MailService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/api/emit")
public class EmitirVisitaServlet extends HttpServlet {
  private final EmisionVisitanteService emision = new EmisionVisitanteService();
  private final MailService mail = new MailService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String nombre  = req.getParameter("nombre");
    String email   = req.getParameter("email");
    String motivo  = req.getParameter("motivo");
    String destino = req.getParameter("destino"); // p.ej. "M-5"
    int ttlMin = 30; // ajusta tu regla

    // id del guardia en sesión (si lo tienes como "uid" o "userId")
    HttpSession s = req.getSession(false);
    Integer guardId = null;
    if (s != null) {
      Object uid = s.getAttribute("uid");
      if (uid instanceof Integer) guardId = (Integer) uid;
      if (guardId == null) {
        Object userId = s.getAttribute("userId");
        if (userId instanceof Integer) guardId = (Integer) userId;
      }
    }

    try {
      // 1) Crear pase (obliga a que exista residente de la casa)
      String token = emision.emitir(nombre, email, motivo, destino, ttlMin, guardId);

      // 2) URL absoluta para validar
      String base = req.getScheme() + "://" + req.getServerName()
          + ((req.getServerPort()==80 || req.getServerPort()==443) ? "" : ":" + req.getServerPort())
          + req.getContextPath();
      String url = base + "/api/validate?token=" + token;

      // 3) Enviar QR por correo (opcional)
      if (email != null && !email.trim().isEmpty()) {
        byte[] png = QRUtil.makeQRPng(url, 300);
        String body = "<p>¡Hola " + (nombre == null || nombre.isEmpty() ? "visitante" : nombre) + "!</p>"
            + "<p>Se generó tu <b>código QR</b> de acceso.</p>"
            + "<p>Vigencia: " + ttlMin + " minutos.</p>";
        mail.sendWithInlinePng(email, "QR de acceso", body, png);
      }

      // 4) Volver al formulario con feedback
      req.setAttribute("ok", true);
      req.setAttribute("token", token);
      req.setAttribute("nombreMostrado", (nombre == null || nombre.isEmpty()) ? "Visitante" : nombre);
      req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);

    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}
