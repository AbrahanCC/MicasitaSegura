package controller;

import dao.IncidenteDAO;
import dao.IncidenteDAOImpl;
import dao.UsuarioDAOImpl;
import model.Incidente;
import service.MailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/incidente")
public class IncidenteServlet extends HttpServlet {
  // // DAOs/servicios
  private final IncidenteDAO incDAO = new IncidenteDAOImpl();
  private final UsuarioDAOImpl userDAO = new UsuarioDAOImpl();
  private final MailService mail = new MailService();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // // muestra el formulario de reporte
    req.getRequestDispatcher("/CU6/incidente_nuevo.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");

    // // datos de sesión (RN1: solo residente autenticado)
    HttpSession s = req.getSession(false);
    Integer idResidente = (s==null)?null:(Integer)s.getAttribute("uid");
    String nombreResidente = (s==null)?null:(String)s.getAttribute("uname");
    String numeroCasa = (s==null)?null:(String)s.getAttribute("casa");
    String lote = (s==null)?null:(String)s.getAttribute("lote");
    if (idResidente == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }

    // // inputs
    String tipo = req.getParameter("tipo");                  // // RN3: tipo de incidente
    String fechaHoraStr = req.getParameter("fechaHora");     // // "yyyy-MM-ddTHH:mm"
    String descripcion = req.getParameter("descripcion");    // // máx 200 chars (RN7)

    try {
      // // RN7: validación simple de campos obligatorios
      if (tipo==null || tipo.isEmpty() || fechaHoraStr==null || fechaHoraStr.isEmpty()
          || descripcion==null || descripcion.trim().isEmpty() || descripcion.trim().length()>200) {
        req.setAttribute("error","Completa todos los campos correctamente (máx 200 caracteres).");
        doGet(req, resp);
        return;
      }

      // // parse de fecha/hora
      Timestamp fh = Timestamp.valueOf(fechaHoraStr.replace('T',' ') + ":00");

      // // persistir incidente
      Incidente inc = new Incidente();
      inc.setIdResidente(idResidente);
      inc.setTipo(tipo);
      inc.setFechaHora(fh);
      inc.setDescripcion(descripcion.trim());
      incDAO.create(inc);

      // // RN4: correo a TODOS los guardias activos (best-effort, no bloquea flujo)
      try {
        List<String> guardias = userDAO.listarCorreosGuardiasActivos();
        mail.sendNotificacionIncidenteToMany(
            guardias,
            nombreResidente,
            numeroCasa,
            lote,
            tipo,
            fh,
            descripcion.trim()
        );
      } catch (Exception ignore) {
        // // no bloquear si falla correo
      }

      // // mensaje de éxito y volver a comunicación
      s.setAttribute("msgExito","Se ha creado el incidente con éxito.");
      resp.sendRedirect(req.getContextPath() + "/comunicacion");
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
