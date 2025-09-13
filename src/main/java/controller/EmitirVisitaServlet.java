package controller;

import service.EmisionVisitanteService;
import util.QRUtil;
import service.MailService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

// Emite QR de VISITA con 2 usos y reenvía al form con botones de escaneo.
@WebServlet("/api/emit")
public class EmitirVisitaServlet extends HttpServlet {
  private final EmisionVisitanteService emision = new EmisionVisitanteService();
  private final MailService mail = new MailService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String nombre  = req.getParameter("nombre");
    String email   = req.getParameter("email");
    String motivo  = req.getParameter("motivo");
    String destino = req.getParameter("destino"); // "LOTE-NUMERO" viene oculto del form
    int usosMax = 2; // entrada y salida

    try {
      // 1) Token de visita (2 usos)
      String token = emision.emitir(nombre, email, motivo, destino, usosMax);

      // 2) URL absoluta p/validar
      String base = req.getScheme() + "://" + req.getServerName()
          + ((req.getServerPort()==80 || req.getServerPort()==443) ? "" : ":" + req.getServerPort())
          + req.getContextPath();
      String url = base + "/api/validate?token=" + token;

      // 3) QR y correo
      if (email != null && !email.trim().isEmpty()) {
        byte[] png = QRUtil.makeQRPng(url, 300);
        String body = "<p>¡Hola " + (nombre==null? "visitante" : nombre) + "!</p>"
            + "<p>Se ha generado exitosamente tu <b>código QR de acceso</b> al residencial.</p>"
            + "<p><b>Validez del código QR:</b> 2 usos (entrada y salida)</p>"
            + "<p><b>Instrucciones importantes:</b><br>"
            + "Guarda este correo o el QR adjunto.<br>"
            + "Preséntalo al llegar al residencial para que el personal de seguridad lo valide.</p>";
        mail.sendWithInlinePng(email, "Notificación de accesos creados", body, png);
      }

      // 4) Volver al formulario con botones de escaneo
      req.setAttribute("ok", true);
      req.setAttribute("token", token);
      req.setAttribute("nombreMostrado", (nombre == null || nombre.isEmpty()) ? "Visitante" : nombre);
      req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);

    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}
