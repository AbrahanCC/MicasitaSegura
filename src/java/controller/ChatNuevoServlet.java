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
    private final ConversacionService service = new ConversacionService(); // // orquestador del CU6

    // // helpers de sesión
    private Integer rol(HttpSession s){ return (s==null)?null:(Integer)s.getAttribute("rol"); }
    private Integer uid(HttpSession s){ return (s==null)?null:(Integer)s.getAttribute("uid"); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        Integer r = rol(ses);
        Integer u = uid(ses);
        if (r == null || u == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }

        // // siempre lista mis conversaciones ACTIVA(S)
        List<Conversacion> convs = service.listarActivasPorUsuario(u);
        req.setAttribute("convs", convs);

        // // RN5: solo Admin(1) y Residente(3) pueden crear
        boolean puedeCrear = (r == 1 || r == 3);
        req.setAttribute("puedeCrear", puedeCrear);

        // // listado de guardias activos para el combo solo si puede crear
        if (puedeCrear) {
            List<UsuarioMin> guardias = service.listarGuardiasActivos();
            req.setAttribute("guardias", guardias);
        }

        // // mensaje opcional (por ejemplo, conv_cerrada)
        String msg = req.getParameter("msg");
        if (msg != null) req.setAttribute("msg", msg);

        req.getRequestDispatcher("/CU6/chat_nuevo.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        Integer r = rol(ses);
        Integer u = uid(ses);
        if (r == null || u == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }

        // // RN5: solo Admin o Residente pueden crear conversación
        if (!(r == 1 || r == 3)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }

        String guardiaIdStr = req.getParameter("guardiaId");
        if (guardiaIdStr == null || guardiaIdStr.trim().isEmpty()) {
            req.setAttribute("error", "Selecciona un guardia para crear la conversación.");
            doGet(req, resp);
            return;
        }

        try {
            int idGuardia = Integer.parseInt(guardiaIdStr.trim());
            Conversacion c = service.crearConversacion(u, idGuardia); // // valida cupo/duplicado
            resp.sendRedirect(req.getContextPath() + "/chat?id=" + c.getId());
        } catch (NumberFormatException nfe) {
            req.setAttribute("error", "Identificador de guardia inválido.");
            doGet(req, resp);
        } catch (RuntimeException ex) {
            // // incluye mensajes de negocio: "ya existe", "tiene 4 activas", etc.
            req.setAttribute("error", ex.getMessage());
            doGet(req, resp);
        }
    }
}
