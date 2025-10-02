package controller;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Aviso;
import service.NotificacionService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name="NotificacionServlet", urlPatterns={"/guardia/avisos"})
public class NotificacionServlet extends HttpServlet {

    private final NotificacionService service = new NotificacionService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("correos", usuarioDAO.listarCorreosResidentesActivos());
        req.getRequestDispatcher("/view/guardia/avisos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String destinoTipo = req.getParameter("destinoTipo"); // ALL | UNO
        String email       = req.getParameter("email");        // si UNO
        String asunto      = req.getParameter("asunto");
        String mensaje     = req.getParameter("mensaje");

        Aviso a = new Aviso();
        a.setDestinatarioTipo(destinoTipo);
        a.setDestinatarioEmail("UNO".equalsIgnoreCase(destinoTipo) ? email : null);
        a.setAsunto(asunto);
        a.setMensaje(mensaje);

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        a.setCreadoPor(userId);

        int enviados = service.crearYEnviar(a);

        if (enviados > 0) req.setAttribute("ok", "Aviso enviado a " + enviados + " destinatario(s).");
        else req.setAttribute("error", "No se pudo enviar el aviso.");

        // recargar catálogo
        req.setAttribute("correos", usuarioDAO.listarCorreosResidentesActivos());
        req.getRequestDispatcher("/view/guardia/avisos.jsp").forward(req, resp);
    }
}
