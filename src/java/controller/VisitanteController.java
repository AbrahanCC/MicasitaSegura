package controller;

import dao.VisitanteDAO;
import dao.VisitanteDAOImpl;
import model.Visitante;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/** Controlador de vistas para Visitantes (solo GET). */
@WebServlet("/visitantes") //visitantes -> lista / form / scan
public class VisitanteController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final VisitanteDAO visitanteDAO = new VisitanteDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // op: new | scan | list (default)
        String op = val(req.getParameter("op"), "list");

        switch (op) {
            case "new":
                // Formulario: POST lo maneja /api/emit (EmitirVisitaServlet)
                req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);
                return;

            case "scan":
                // Lector QR en navegador (scan.jsp valida via /api/validate)
                req.getRequestDispatcher("/view/guardia/scan.jsp").forward(req, resp);
                return;

            default: // list
                String desde   = trim(req.getParameter("desde"));
                String hasta   = trim(req.getParameter("hasta"));
                String destino = trim(req.getParameter("destinoNumeroCasa"));
                String dpi     = trim(req.getParameter("dpi"));

                List<Visitante> data = visitanteDAO.listar(desde, hasta, destino, dpi);
                req.setAttribute("data", data);
                req.getRequestDispatcher("/view/guardia/visitante-lista.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // No emitir aquí (evita lógica duplicada). Usar /api/emit.
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Usa POST " + req.getContextPath() + "/api/emit para emitir el pase de visita.");
    }

    /* Helpers */
    private static String trim(String s) { return s == null ? "" : s.trim(); }
    private static String val(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }
}
