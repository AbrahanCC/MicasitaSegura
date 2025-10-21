package service;

import dao.ConversacionDAO;
import dao.ConversacionDAOImpl;
import dao.MensajeDAO;
import dao.MensajeDAOImpl;
import model.Mensaje;

import java.util.List;

public class MensajeService {
    // // DAOs y servicio de correo centralizado
    private final MensajeDAO mensajeDAO = new MensajeDAOImpl();
    private final ConversacionDAO conversacionDAO = new ConversacionDAOImpl();
    private final MailService mail = new MailService();

    // // Envía y persiste un mensaje; opcionalmente manda correo (RN6)
    public Mensaje enviarMensaje(int idConversacion, int idEmisor, String contenido,
                                 String nombreEmisor, String correoDestinatario, String chatUrl) {
        // // Validación: mensaje 1..200 caracteres
        if (contenido == null || contenido.trim().isEmpty() || contenido.trim().length() > 200) {
            throw new RuntimeException("El mensaje debe tener entre 1 y 200 caracteres");
        }

        // // Persistencia
        Mensaje m = new Mensaje(idConversacion, idEmisor, contenido.trim());
        m = mensajeDAO.create(m);

        // // Actualiza último movimiento de la conversación
        conversacionDAO.updateFechaUltimoMensaje(idConversacion);

        // // Notificación e-mail (best-effort, no bloquea)
        if (correoDestinatario != null && !correoDestinatario.isEmpty()) {
            String nombre = (nombreEmisor == null || nombreEmisor.isEmpty()) ? "Usuario" : nombreEmisor;
            mail.sendNotificacionMensaje(correoDestinatario, nombre, chatUrl);
        }

        return m;
    }

    // // Lista mensajes con paginado simple
    public List<Mensaje> listarMensajes(int idConversacion, int limit, int offset) {
        return mensajeDAO.findByConversacion(idConversacion, limit, offset);
    }

    // // Marca como leídos los mensajes de "el otro" usuario
    public void marcarLeidos(int idConversacion, int userId){
        mensajeDAO.marcarLeidos(idConversacion, userId);
    }
}
