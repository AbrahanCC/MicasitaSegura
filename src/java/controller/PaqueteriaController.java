package controller;

import model.Usuario;
import model.Paquete;
import service.PaqueteriaService;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/paqueteria")
public class PaqueteriaController extends HttpServlet {

    private final PaqueteriaService service = new PaqueteriaService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String op = def(req.getParameter("op"), "new");

        try {
            validarGuardia(req);

            switch (op) {
                case "list": {
                    String filtro = trim(req.getParameter("q"));
                    List<Paquete> data = service.listarPendientes(filtro);
                    req.setAttribute("data", data);
                    req.setAttribute("filtro", filtro);
                    req.getRequestDispatcher("/view/guardia/paqueteria-lista.jsp").forward(req, resp);
                    return;
                }
                case "new":
                default: {
                    // Combo de residentes activos para el formulario
                    // (Minimalista: solo necesitamos el id + nombre para el combo en JSP;
                    //  como aún no tenemos ese JSP, solo preparo el forward.)
                    req.setAttribute("residentes", usuarioDAO.buscarDirectorio(null, null, null, null));
                    req.getRequestDispatcher("/view/guardia/paqueteria-form.jsp").forward(req, resp);
                    return;
                }
            }

        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/view/guardia/paqueteria-form.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String op = def(req.getParameter("op"), "");

        try {
            Usuario guardia = validarGuardia(req);

            if ("save".equalsIgnoreCase(op)) {
                String numeroGuia = trim(req.getParameter("numero_guia"));
                int destinatarioId = Integer.parseInt(def(req.getParameter("destinatario_id"), "0"));
                String observaciones = trim(req.getParameter("observaciones"));

                service.registrarRecepcion(numeroGuia, destinatarioId, guardia.getId(), observaciones);
                req.getSession().setAttribute("flash_ok", "Información guardada con éxito.");
                resp.sendRedirect(req.getContextPath() + "/paqueteria?op=list");
                return;
            }

            if ("deliver".equalsIgnoreCase(op)) {
                long id = Long.parseLong(def(req.getParameter("id"), "0"));
                service.entregarPaquete(id, guardia.getId());
                req.getSession().setAttribute("flash_ok", "Entrega registrada.");
                resp.sendRedirect(req.getContextPath() + "/paqueteria?op=list");
                return;
            }

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada.");

        } catch (Exception e) {
            req.getSession().setAttribute("flash_err", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/paqueteria?op=new");
        }
    }

    // --- Utilidades ---

    /** RN1: solo guardias (o admin). */
    private Usuario validarGuardia(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) throw new IllegalStateException("Sesión no válida.");
        Integer rol = (Integer) s.getAttribute("rol");
        Usuario u = (Usuario) s.getAttribute("usuario");
        if (rol == null || (rol != 2 && rol != 1) || u == null)
            throw new SecurityException("Acceso solo para guardias.");
        return u;
    }

    private static String def(String v, String d) { return (v == null || v.isEmpty()) ? d : v; }
    private static String trim(String s) { return s == null ? null : s.trim(); }
}
