package controller;

import dao.IncidenteDAO;
import dao.IncidenteDAOImpl;
import dao.UsuarioDAOImpl;
import model.Incidente;
import model.Usuario;
import service.MailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/incidente")
public class IncidenteServlet extends HttpServlet {
  private final IncidenteDAO incDAO = new IncidenteDAOImpl();
  private final UsuarioDAOImpl userDAO = new UsuarioDAOImpl();
  private final MailService mail = new MailService();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    HttpSession s = req.getSession(false);
    Integer rol = (s==null)?null:(Integer)s.getAttribute("rol");
    if (rol == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
    if (rol != 3) { resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo residentes pueden reportar incidentes"); return; }
    req.getRequestDispatcher("/CU6/incidente_nuevo.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");

    HttpSession s = req.getSession(false);
    Integer idResidente = (s==null)?null:(Integer)s.getAttribute("uid");
    Integer rol = (s==null)?null:(Integer)s.getAttribute("rol");
    if (idResidente == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
    if (rol == null || rol != 3) { resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo residentes"); return; }

    // ⚠️ SIEMPRE LEER DOMICILIO DESDE BD PARA EVITAR NULOS EN SESIÓN
    Usuario u = userDAO.obtener(idResidente);
    String nombreResidente = (u != null && u.getNombre()!=null) ? u.getNombre() : (String)s.getAttribute("uname");
    String numeroCasa = (u != null) ? trimOrNull(u.getNumeroCasa()) : null;
    String lote       = (u != null) ? trimOrNull(u.getLote())       : null;

    String tipo = req.getParameter("tipo");
    String fechaHoraStr = req.getParameter("fechaHora"); // "yyyy-MM-ddTHH:mm"
    String descripcion = req.getParameter("descripcion");

    try {
      if (tipo==null || tipo.isEmpty() || fechaHoraStr==null || fechaHoraStr.isEmpty()
          || descripcion==null || descripcion.trim().isEmpty() || descripcion.trim().length()>200) {
        req.setAttribute("error","Completa todos los campos correctamente (máx 200 caracteres).");
        doGet(req, resp);
        return;
      }

      Timestamp fh = Timestamp.valueOf(fechaHoraStr.replace('T',' ') + ":00");

      Incidente inc = new Incidente();
      inc.setIdResidente(idResidente);
      inc.setTipo(tipo);
      inc.setFechaHora(fh);
      inc.setDescripcion(descripcion.trim());
      incDAO.create(inc);

      // RN4: correo a guardias activos
      try {
        List<String> guardias = userDAO.listarCorreosGuardiasActivos();
        if (!guardias.isEmpty()) {
          for (String to : guardias) {
            mail.sendNotificacionIncidente(
                to,
                nombreResidente,
                numeroCasa,   // viene de BD; no se pierde
                lote,         // viene de BD; no se pierde
                tipo,
                fh,
                descripcion.trim()
            );
          }
        }
      } catch (Exception ignore) { }

      s.setAttribute("msgExito","Se ha creado el incidente con éxito.");
      resp.sendRedirect(req.getContextPath() + "/comunicacion");
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  private static String trimOrNull(String v) {
    if (v == null) return null;
    v = v.trim();
    return v.isEmpty() ? null : v;
  }
}
