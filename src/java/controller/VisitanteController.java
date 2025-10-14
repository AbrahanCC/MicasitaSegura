package controller;

import dao.VisitanteDAO;
import dao.VisitanteDAOImpl;
import model.Visitante;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

// Controla vistas y acciones de visitantes (listar, nuevo, escáner y cancelar)
@WebServlet("/visitantes")
public class VisitanteController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // DAO de visitantes
    private final VisitanteDAO visitanteDAO = new VisitanteDAOImpl();

    // Navegación de vistas (lista, formulario, escáner)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String op = porDefecto(req.getParameter("op"), "list").toLowerCase();

        switch (op) {
            case "new": // Muestra formulario de registro
                req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);
                return;

            case "scan": // Muestra lector de QR
                req.getRequestDispatcher("/view/guardia/scan.jsp").forward(req, resp);
                return;

            default: // Lista con filtros
                String desde   = texto(req.getParameter("desde"));
                String hasta   = texto(req.getParameter("hasta"));
                String destino = texto(req.getParameter("destinoNumeroCasa"));
                String dpi     = texto(req.getParameter("dpi"));

                List<Visitante> data = visitanteDAO.listar(desde, hasta, destino, dpi);
                req.setAttribute("data", data);
                req.getRequestDispatcher("/view/guardia/visitante-lista.jsp").forward(req, resp);
        }
    }

    // Procesa acciones (cancelar visita)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String op = porDefecto(req.getParameter("op"), "");
        if ("cancel".equalsIgnoreCase(op)) {
            try {
                int id = Integer.parseInt(porDefecto(req.getParameter("id"), "0"));
                if (id <= 0) throw new IllegalArgumentException("ID inválido.");
                visitanteDAO.rechazar(id, getUserId(req));
                resp.sendRedirect(req.getContextPath() + "/visitantes"); // Vuelve a la lista
                return;
            } catch (Exception e) {
                req.setAttribute("error", e.getMessage());
                doGet(req, resp); // Repite la lista con el error
                return;
            }
        }

        // Método no permitido para otras operaciones
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Usa POST " + req.getContextPath() + "/api/emit para emitir el pase de visita.");
    }

    // Obtiene el id de usuario en sesión o null
    private Integer getUserId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object uid = s.getAttribute("uid");
        return (uid instanceof Integer) ? (Integer) uid : null;
    }

    // Devuelve texto no nulo y recortado
    private static String texto(String s) { return s == null ? "" : s.trim(); }

    // Devuelve valor con defecto si viene nulo/vacío
    private static String porDefecto(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }
}
