package controller;

import service.ConversacionService;
import service.MensajeService;
import model.Conversacion;
import model.Mensaje;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    private final ConversacionService convSrv = new ConversacionService();
    private final MensajeService msgSrv = new MensajeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Debe haber sesión
        HttpSession s = req.getSession(false);
        Integer uid = (s == null) ? null : (Integer) s.getAttribute("uid"); // <-- AQUÍ ESTÁ EL ARREGLO DEL NOMBRE DE ATRIBUTO
        if (uid == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        // id de conversación
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) { resp.sendError(400, "Falta id"); return; }
        int id = Integer.parseInt(idParam);

        // Cargar conversación
        Conversacion c = convSrv.obtener(id);
        if (c == null) { resp.sendError(404, "Conversación no existe"); return; }

        // Seguridad: solo participante puede ver (residente o guardia)
        if (!uid.equals(c.getIdResidente()) && !uid.equals(c.getIdGuardia())) {
            resp.sendError(403); return;
        }

        // Cargar mensajes
        List<Mensaje> mensajes = msgSrv.listarMensajes(id, 200, 0);

        // Enviar a la JSP
        req.setAttribute("conv", c);
        req.setAttribute("mensajes", mensajes);
        req.getRequestDispatcher("/CU6/chat.jsp").forward(req, resp);
    }
}
