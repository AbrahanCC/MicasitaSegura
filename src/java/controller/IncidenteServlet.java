package controller;

import service.IncidenteService;
import util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/incidente")
public class IncidenteServlet extends HttpServlet {
    private final IncidenteService service = new IncidenteService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // <<< aquí está el arreglo: ruta CU6
        req.getRequestDispatcher("/CU6/incidente_nuevo.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        Integer idResidente = (s == null) ? null : (Integer) s.getAttribute("uid");   // <<< usa 'uid'
        String nombreResidente = (s == null) ? null : (String) s.getAttribute("uname"); // <<< usa 'uname'
        if (idResidente == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }

        String tipo = req.getParameter("tipo");
        String fechaHoraStr = req.getParameter("fechaHora"); // yyyy-MM-ddTHH:mm
        String descripcion = req.getParameter("descripcion");

        String numeroCasa = getCasa(idResidente); // <<< aquí está el arreglo: leemos 'casa' desde BD

        try {
            Timestamp fh = Timestamp.valueOf(fechaHoraStr.replace("T", " ") + ":00");
            service.crearYNotificar(idResidente, tipo, fh, descripcion,
                    (nombreResidente==null?"Residente":nombreResidente),
                    (numeroCasa==null?"":numeroCasa));
            req.getSession().setAttribute("msgExito", "Se ha creado el incidente con éxito.");
            resp.sendRedirect(req.getContextPath() + "/comunicacion");
        } catch (RuntimeException ex) {
            req.setAttribute("error", ex.getMessage());
            doGet(req, resp);
        }
    }

    private String getCasa(int idResidente) {
        String sql = "SELECT casa FROM usuarios WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idResidente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (Exception e) { return null; }
    }
}
