package controller;

import service.ConversacionService;
import service.ConversacionService.UsuarioMin;
import model.Conversacion;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/chat/nuevo")
public class ChatNuevoServlet extends HttpServlet {
    private final ConversacionService service = new ConversacionService();

    private boolean allow(HttpSession s) {
        if (s == null) return false;
        Integer rol = (Integer) s.getAttribute("rol");
        // >>> AQUÍ ESTÁ EL ARREGLO: permitir ADMIN(1) y RESIDENTE(2)
        return rol != null && (rol == 1 || rol == 2);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        if (!allow(ses)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        List<UsuarioMin> guardias = service.listarGuardiasActivos();
        req.setAttribute("guardias", guardias);
        req.getRequestDispatcher("/CU6/chat_nuevo.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        if (!allow(ses)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        Integer uid = (Integer) ses.getAttribute("uid"); // id del usuario logueado
        int idGuardia = Integer.parseInt(req.getParameter("guardiaId"));

        try {
            // Usamos el uid como "id_residente" lógico para la conversación
            Conversacion c = service.crearConversacion(uid, idGuardia);
            resp.sendRedirect(req.getContextPath() + "/chat?id=" + c.getId());
        } catch (RuntimeException ex) {
            req.setAttribute("error", ex.getMessage());
            doGet(req, resp);
        }
    }
}
