package controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
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
        String action = nv(req.getParameter("action"), "form"); // por defecto: formulario
        HttpSession session = req.getSession(false);
        Usuario user = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        try {
            switch (action) {
                case "listar": {
                    validarSesion(user);
                    List<Reserva> reservas = service.listarPorUsuario(user.getId());
                    req.setAttribute("reservas", reservas);
                    req.getRequestDispatcher("/view/residente/reservas.jsp").forward(req, resp);
                    break;
                }
                case "form":
                default: {
                    validarSesion(user);
                    List<AreaComun> areas = service.listarActivas();
                    req.setAttribute("areas", areas);
                    req.getRequestDispatcher("/view/residente/reserva-form.jsp").forward(req, resp);
                    break;
                }
            }
        } catch (Exception ex) {
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/view/residente/reserva-form.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = nv(req.getParameter("action"), "");
        HttpSession session = req.getSession(false);
        Usuario user = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        try {
            switch (action) {
                case "crear": {
                    validarSesion(user);
                    int areaId = Integer.parseInt(req.getParameter("area_id"));
                    LocalDate fecha = LocalDate.parse(req.getParameter("fecha"));
                    LocalTime ini = LocalTime.parse(req.getParameter("hora_inicio"));
                    LocalTime fin = LocalTime.parse(req.getParameter("hora_fin"));

                    service.crearReserva(user, areaId, fecha, ini, fin);
                    session.setAttribute("flash_ok", "Reserva creada con éxito");

                    // Después de crear, volver al FORMULARIO (no a QR ni a lista)
                    resp.sendRedirect(req.getContextPath() + "/residente/reservas");
                    return;
                }
                case "cancelar": {
                    validarSesion(user);
                    int reservaId = Integer.parseInt(req.getParameter("id"));
                    service.cancelarReserva(reservaId, user);
                    session.setAttribute("flash_ok", "Reserva cancelada con éxito");

                    // Luego de cancelar, mostrar la LISTA
                    resp.sendRedirect(req.getContextPath() + "/residente/reservas?action=listar");
                    return;
                }
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
            }
        } catch (IllegalStateException ise) { // horario no disponible (FA05)
            session.setAttribute("flash_err", ise.getMessage());
            resp.sendRedirect(req.getContextPath() + "/residente/reservas"); // vuelve al form
        } catch (Exception ex) {
            session.setAttribute("flash_err", "Error: " + ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/residente/reservas"); // vuelve al form
        }
    }

    private void validarSesion(Usuario u) {
        if (u == null || u.getId() <= 0) {
            throw new IllegalArgumentException("Sesión no válida.");
        }
    }

    private static String nv(String s, String d) { return (s == null || s.isEmpty()) ? d : s; }
}
