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
@WebServlet("/visitantes")
public class VisitanteController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final VisitanteDAO visitanteDAO = new VisitanteDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String op = val(req.getParameter("op"), "list").toLowerCase();

        switch (op) {
            case "new":
                req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);
                return;

            case "scan":
                req.getRequestDispatcher("/view/guardia/scan.jsp").forward(req, resp);
                return;

            default: // list
                String desde   = nz(req.getParameter("desde"));
                String hasta   = nz(req.getParameter("hasta"));
                String destino = nz(req.getParameter("destinoNumeroCasa"));
                String dpi     = nz(req.getParameter("dpi"));

                List<Visitante> data = visitanteDAO.listar(desde, hasta, destino, dpi);
                req.setAttribute("data", data);
                req.getRequestDispatcher("/view/guardia/visitante-lista.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Usa POST " + req.getContextPath() + "/api/emit para emitir el pase de visita.");
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }
    private static String val(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }
}
