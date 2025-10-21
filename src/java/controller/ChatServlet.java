package controller;

import service.ConversacionService;
import service.MensajeService;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Conversacion;
import model.Mensaje;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    // // Servicios
    private final ConversacionService convSrv = new ConversacionService();
    private final MensajeService msgSrv = new MensajeService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl(); // // para obtener correo por id

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        Integer uid = (s == null) ? null : (Integer) s.getAttribute("uid");
        if (uid == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) { resp.sendError(400, "Falta id"); return; }

        int id;
        try { id = Integer.parseInt(idParam); }
        catch (NumberFormatException e) { resp.sendError(400, "Id inválido"); return; }

        Conversacion c = convSrv.obtener(id);
        if (c == null) { resp.sendError(404, "Conversación no existe"); return; }

        // // Solo participantes pueden ver
        if (!uid.equals(c.getIdResidente()) && !uid.equals(c.getIdGuardia())) {
            resp.sendError(403); return;
        }

        // // Marca como leídos los mensajes entrantes
        msgSrv.marcarLeidos(id, uid);

        // // Carga mensajes
        List<Mensaje> mensajes = msgSrv.listarMensajes(id, 200, 0);

        req.setAttribute("conv", c);
        req.setAttribute("mensajes", mensajes);
        req.getRequestDispatcher("/CU6/chat.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        Integer uid = (s == null) ? null : (Integer) s.getAttribute("uid");
        String uname = (s == null) ? null : (String) s.getAttribute("uname");
        if (uid == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String op = req.getParameter("op");
        String idParam = req.getParameter("idConversacion");
        if (idParam == null || idParam.trim().isEmpty()) { resp.sendError(400, "Falta idConversacion"); return; }

        int idConversacion;
        try { idConversacion = Integer.parseInt(idParam); }
        catch (NumberFormatException e) { resp.sendError(400, "Id inválido"); return; }

        Conversacion c = convSrv.obtener(idConversacion);
        if (c == null) { resp.sendError(404, "Conversación no existe"); return; }
        if (!uid.equals(c.getIdResidente()) && !uid.equals(c.getIdGuardia())) { resp.sendError(403); return; }

        if ("close".equalsIgnoreCase(op)) {
            // // Cierra la conversación
            convSrv.cerrar(idConversacion);
            resp.sendRedirect(req.getContextPath() + "/chat/nuevo?msg=conv_cerrada");
            return;
        }

        // // Envío de mensaje
        if ("CERRADA".equalsIgnoreCase(c.getEstado())) {
            req.setAttribute("error", "La conversación está cerrada.");
            doGet(req, resp);
            return;
        }

        String contenido = req.getParameter("contenido");
        int destinatarioId = (uid.equals(c.getIdResidente())) ? c.getIdGuardia() : c.getIdResidente();

        // // Correo del destinatario vía DAO (sin SQL aquí)
        String correoDest = usuarioDAO.obtenerCorreoPorId(destinatarioId); // // puede ser null

        // // URL del chat para incluir en el correo
        String chatUrl = req.getScheme() + "://" + req.getServerName()
                + (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort())
                + req.getContextPath() + "/chat?id=" + idConversacion;

        try {
            msgSrv.enviarMensaje(idConversacion, uid, contenido, (uname==null?"Usuario":uname), correoDest, chatUrl);
            resp.sendRedirect(req.getContextPath() + "/chat?id=" + idConversacion);
        } catch (RuntimeException ex) {
            req.setAttribute("error", ex.getMessage());
            doGet(req, resp);
        }
    }
}
