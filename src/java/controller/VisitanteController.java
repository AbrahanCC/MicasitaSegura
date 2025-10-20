package controller;

import dao.VisitanteDAO;
import dao.VisitanteDAOImpl;
import model.Visitante;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

//Controlador principal de visitantes (listar, registrar, escanear y cancelar visitas).
 
@WebServlet("/visitantes")
public class VisitanteController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final VisitanteDAO visitanteDAO = new VisitanteDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String op = porDefecto(req.getParameter("op"), "list").toLowerCase();

        switch (op) {
            case "new":
                // Agregar nombre del residente logueado a la vista
                HttpSession s = req.getSession(false);
                String residenteNombre = (s != null)
                        ? (String) s.getAttribute("nombreUsuario")
                        : "Residente activo";
                req.setAttribute("nombreResidente", residenteNombre);

                // Redirigir al formulario
                req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);
                return;

            case "scan":
                req.getRequestDispatcher("/view/guardia/scan.jsp").forward(req, resp);
                return;

            default:
                String desde   = texto(req.getParameter("desde"));
                String hasta   = texto(req.getParameter("hasta"));
                String destino = texto(req.getParameter("destinoNumeroCasa"));
                String dpi     = texto(req.getParameter("dpi"));

                List<Visitante> data = visitanteDAO.listar(desde, hasta, destino, dpi);
                req.setAttribute("data", data);
                req.getRequestDispatcher("/view/guardia/visitante-lista.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String op = porDefecto(req.getParameter("op"), "");

        if ("cancel".equalsIgnoreCase(op)) {
            try {
                int id = Integer.parseInt(porDefecto(req.getParameter("id"), "0"));
                if (id <= 0) throw new IllegalArgumentException("ID inválido.");
                visitanteDAO.rechazar(id, getUserId(req));
                resp.sendRedirect(req.getContextPath() + "/visitantes");
                return;
            } catch (Exception e) {
                req.setAttribute("error", e.getMessage());
                doGet(req, resp);
                return;
            }
        }

        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Usa POST " + req.getContextPath() + "/api/emit para emitir el pase de visita.");
    }

    //Obtener id de usuario en sesión
    private Integer getUserId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object uid = s.getAttribute("uid");
        return (uid instanceof Integer) ? (Integer) uid : null;
    }

    private static String texto(String s) { return s == null ? "" : s.trim(); }

    private static String porDefecto(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }
}
