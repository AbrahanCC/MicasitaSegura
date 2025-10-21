package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import model.AreaComun;
import model.Reserva;
import model.Usuario;
import service.ReservaService;

@WebServlet(name = "ReservaController", urlPatterns = {"/residente/reservas"})
public class ReservaController extends HttpServlet {

    private final ReservaService service = new ReservaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = n(req.getParameter("action"), "listar");
        HttpSession session = req.getSession(false);
        Usuario user = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        try {
            switch (action) {
                case "nuevo": // carga de formulario
                    List<AreaComun> areas = service.listarAreasActivas();
                    req.setAttribute("areas", areas);
                    req.getRequestDispatcher("/view/residente/reserva-form.jsp").forward(req, resp);
                    break;
                default: // listar
                    validarSesion(user);
                    List<Reserva> reservas = service.listarPorUsuario(user.getId());
                    req.setAttribute("reservas", reservas);
                    req.getRequestDispatcher("/view/residente/reservas.jsp").forward(req, resp);
            }
        } catch (Exception ex) {
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/view/residente/reservas.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = n(req.getParameter("action"), "");
        HttpSession session = req.getSession(false);
        Usuario user = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        try {
            switch (action) {
                case "crear":
                    validarSesion(user);
                    int areaId = Integer.parseInt(req.getParameter("area_id"));
                    LocalDate fecha = LocalDate.parse(req.getParameter("fecha"));
                    LocalTime ini = LocalTime.parse(req.getParameter("hora_inicio"));
                    LocalTime fin = LocalTime.parse(req.getParameter("hora_fin"));
                    service.crearReserva(user, areaId, fecha, ini, fin);
                    req.getSession().setAttribute("flash_ok", "Reserva creada con éxito");
                    resp.sendRedirect(req.getContextPath() + "/residente/reservas");
                    return;

                case "cancelar":
                    validarSesion(user);
                    int reservaId = Integer.parseInt(req.getParameter("id"));
                    service.cancelarReserva(reservaId, user);
                    req.getSession().setAttribute("flash_ok", "Reserva cancelada con éxito");
                    resp.sendRedirect(req.getContextPath() + "/residente/reservas");
                    return;

                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
            }
        } catch (IllegalStateException ise) { // mensaje FA05
            req.getSession().setAttribute("flash_err", ise.getMessage());
            resp.sendRedirect(req.getContextPath() + "/residente/reservas?action=nuevo");
        } catch (Exception ex) {
            req.getSession().setAttribute("flash_err", "Error: " + ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/residente/reservas");
        }
    }

    private void validarSesion(Usuario u) {
        if (u == null || u.getId() <= 0) {
            throw new IllegalArgumentException("Sesión no válida.");
        }
    }

    private static String n(String s, String d) { return (s == null || s.isEmpty()) ? d : s; }
}
