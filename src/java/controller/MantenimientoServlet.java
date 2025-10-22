package controller;

import dao.MantenimientoDAO;
import dao.MantenimientoDAOImpl;
import model.Mantenimiento;
import model.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@WebServlet("/residente/mantenimiento")
public class MantenimientoServlet extends HttpServlet {

    private final MantenimientoDAO mantenimientoDAO = new MantenimientoDAOImpl(); 

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Solo muestra el formulario de reporte
        req.getRequestDispatcher("/view/residente/mantenimiento.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Usuario u = (Usuario) s.getAttribute("usuario");

        // --- Capturar los datos del formulario ---
        String tipo = req.getParameter("tipoInconveniente");
        String descripcion = req.getParameter("descripcion");

        // Validaciones básicas
        if (tipo == null || tipo.trim().isEmpty() || descripcion == null || descripcion.trim().isEmpty()) {
            req.setAttribute("error", "Debe completar todos los campos antes de enviar el reporte.");
            req.getRequestDispatcher("/view/residente/mantenimiento.jsp").forward(req, resp);
            return;
        }

        // --- Crear objeto de mantenimiento ---
        Mantenimiento m = new Mantenimiento();
        m.setIdResidente(u.getId());
        m.setTipoInconveniente(tipo);
        m.setDescripcion(descripcion);
        m.setFechaHora(Timestamp.valueOf(LocalDateTime.now()));
        m.setActivo(true);
        m.setNombreResidente(u.getNombre() + " " + u.getApellidos());
        m.setNumeroCasa(u.getNumeroCasa());
        m.setLote(u.getLote());

        // --- Guardar en la base de datos ---
        boolean ok = mantenimientoDAO.crear(m);

        if (ok) {
            req.setAttribute("mensaje", "Reporte enviado correctamente. Se notificará al administrador.");
        } else {
            req.setAttribute("error", "Hubo un problema al registrar el reporte. Intente nuevamente.");
        }

        // Volver al formulario
        req.getRequestDispatcher("/view/residente/mantenimiento.jsp").forward(req, resp);
    }
}
