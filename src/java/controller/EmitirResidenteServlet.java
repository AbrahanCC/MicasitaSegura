package controller;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Usuario;
import service.MailService;
import util.QRUtil;
import util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/emit-residente")
public class EmitirResidenteServlet extends HttpServlet {
  private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
  private final MailService mail = new MailService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      // 1) Identificar al residente (elige UNO: por id o por correo)
      String idStr = req.getParameter("usuarioId");
      String correoParam = req.getParameter("correo");

      Usuario u = null;
      if (idStr != null && !idStr.trim().isEmpty()) {
        u = usuarioDAO.obtener(Integer.parseInt(idStr));
      } else if (correoParam != null && !correoParam.trim().isEmpty()) {
        u = usuarioDAO.buscarPorCorreo(correoParam.trim());
      }
      if (u == null) {
        resp.setStatus(400);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"ok\":false,\"error\":\"residente_no_encontrado\"}");
        return;
      }
      if (u.getCorreo() == null || u.getCorreo().trim().isEmpty()) {
        resp.setStatus(400);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"ok\":false,\"error\":\"residente_sin_correo\"}");
        return;
      }

      // 2) Token permanente
      String secret = System.getProperty("RESIDENT_SECRET", "micasita_secret_demo");
      String firma = PasswordUtil.sha256(secret + ":" + u.getId());
      String token = "R:" + u.getId() + ":" + firma.substring(0, 32); // compacto

      // 3) URL para validar
      String base = req.getScheme() + "://" + req.getServerName()
                  + ( (req.getServerPort()==80 || req.getServerPort()==443) ? "" : (":" + req.getServerPort()) )
                  + req.getContextPath();
      String url = base + "/api/validate?token=" + token;

      // 4) Generar PNG del QR
      byte[] png = QRUtil.makeQRPng(url, 400);

      // 5) Enviar correo
      String body = "<h3>QR de acceso residente</h3>"
                  + "<p>Este QR es permanente para el residente <b>" + u.getNombre() + " " + u.getApellidos() + "</b>.</p>"
                  + "<p>Si no puedes escanear, abre esta URL: <a href=\"" + url + "\">" + url + "</a></p>";
      mail.sendWithInlinePng(u.getCorreo(), "QR de acceso residente", body, png);

      // 6) Respuesta
      resp.setContentType("application/json; charset=UTF-8");
      resp.getWriter().write("{\"ok\":true,\"token\":\"" + token + "\"}");
    } catch (Exception ex) {
      resp.setStatus(500);
      resp.setContentType("application/json; charset=UTF-8");
      resp.getWriter().write("{\"ok\":false,\"error\":\"" + ex.getMessage() + "\"}");
    }
  }
}

