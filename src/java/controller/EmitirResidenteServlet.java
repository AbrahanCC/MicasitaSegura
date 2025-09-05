package controller;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Usuario;
import service.MailService;
import util.QRUtil;
import util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

// Emite QR PERMANENTE para RESIDENTE
@WebServlet("/api/emit-residente")
public class EmitirResidenteServlet extends HttpServlet {
  private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
  private final MailService mail = new MailService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      // 1) Buscar residente por id o correo
      String idStr = req.getParameter("usuarioId");
      String correoParam = req.getParameter("correo");
      Usuario u = null;
      if (idStr != null && !idStr.trim().isEmpty()) {
        u = usuarioDAO.obtener(Integer.parseInt(idStr));
      } else if (correoParam != null && !correoParam.trim().isEmpty()) {
        u = usuarioDAO.buscarPorCorreo(correoParam.trim());
      }
      if (u == null || u.getCorreo() == null || u.getCorreo().isEmpty()) {
        req.setAttribute("error", "Residente no válido o sin correo.");
        req.setAttribute("u", new Usuario());
        req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
        return;
      }

      // 2) Token permanente (R:<id>:<firma>)
      String secret = System.getProperty("RESIDENT_SECRET", "residentes2025");
      String firma = PasswordUtil.sha256(secret + ":" + u.getId());
      String token = "R:" + u.getId() + ":" + firma.substring(0, 32);

      // 3) URL absoluta p/validar
      String base = req.getScheme() + "://" + req.getServerName()
          + ((req.getServerPort()==80 || req.getServerPort()==443) ? "" : ":" + req.getServerPort())
          + req.getContextPath();
      String url = base + "/api/validate?token=" + token;

      // 4) QR y correo
      byte[] png = QRUtil.makeQRPng(url, 400);
      String body = "<p>¡Hola!</p>"
          + "<p>Se ha generado exitosamente tu <b>código QR de acceso</b> al residencial.</p>"
          + "<p><b>Nombre del Residente:</b> " + u.getNombre() + " " + u.getApellidos() + "</p>"
          + "<p><b>Validez del código QR:</b> ILIMITADO</p>"
          + "<p><b>Instrucciones importantes:</b><br>"
          + "Guarda este correo o el QR adjunto.<br>"
          + "Preséntalo al llegar al residencial para que el personal de seguridad lo valide.</p>";
      mail.sendWithInlinePng(u.getCorreo(), "Notificación de accesos creados", body, png);

      // 5) Volver al formulario con botones de escaneo
      req.setAttribute("ok", true);
      req.setAttribute("token", token);
      req.setAttribute("u", u);
      req.setAttribute("nombreMostrado", u.getNombre() + " " + u.getApellidos());
      req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);

    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
}
