package service;

import dao.ConversacionDAOImpl;
import dao.MensajeDAO;
import dao.MensajeDAOImpl;
import model.Conversacion;
import model.Mensaje;

import java.util.List;

public class MensajeService {
    private final MensajeDAO mensajeDAO = new MensajeDAOImpl();
    private final ConversacionDAOImpl conversacionDAO = new ConversacionDAOImpl();
    private final MailService mail = new MailService();

    public Mensaje enviarMensaje(int idConversacion, int idEmisor, String contenido, String nombreEmisor, String correoDestinatario) {
        if (contenido == null || contenido.trim().isEmpty() || contenido.length() > 200) {
            throw new RuntimeException("El mensaje debe tener entre 1 y 200 caracteres");
        }
        Mensaje m = new Mensaje(idConversacion, idEmisor, contenido.trim());
        m = mensajeDAO.create(m);

        // Actualiza fecha_ultimo_mensaje
        conversacionDAO.updateFechaUltimoMensaje(idConversacion);

        // RN6: correo al destinatario (si hay correo)
        if (correoDestinatario != null && !correoDestinatario.isEmpty()) {
            String subject = "Notificación de mensaje";
            String body = "El usuario <b>" + nombreEmisor + "</b> le ha enviado un mensaje.<br>"
                        + "Favor ingresar al apartado de <b>Consulta General</b> para revisar su conversación.";
            mail.sendHtml(correoDestinatario, subject, body);
        }
        return m;
    }

    public List<Mensaje> listarMensajes(int idConversacion, int limit, int offset) {
        return mensajeDAO.findByConversacion(idConversacion, limit, offset);
    }

    public int obtenerDestinatario(Conversacion c, int idEmisor) {
        return (idEmisor == c.getIdResidente()) ? c.getIdGuardia() : c.getIdResidente();
    }
}
