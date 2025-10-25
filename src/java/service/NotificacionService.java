package service;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Aviso;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Inserta correos en notif_outbox y registra evento en notif_log.
 * Tablas existentes:
 *   notif_outbox(id, recipient, subject, body_html, correlation_id, status, retry_count, last_error, created_at, sent_at)
 *   notif_log(id, notif_id, event, detail, created_at)
 */
public class NotificacionService {

    private static final String STATUS_PENDING = "pending";

    private static final String SQL_ENQUEUE =
        "INSERT INTO notif_outbox (recipient, subject, body_html, correlation_id, status, retry_count, last_error, created_at, sent_at) " +
        "VALUES (?, ?, ?, NULL, ?, 0, NULL, NOW(), NULL)";

    private static final String SQL_LOG =
        "INSERT INTO notif_log (notif_id, event, detail, created_at) VALUES (?, ?, ?, NOW())";

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    public long enqueueEmail(String to, String subject, String htmlBody) {
        if (to == null || to.isEmpty()) return 0L;
        long notifId = 0L;
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_ENQUEUE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, to);
            ps.setString(2, subject);
            ps.setString(3, htmlBody);
            ps.setString(4, STATUS_PENDING);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) notifId = rs.getLong(1);
            }
            try (PreparedStatement log = cn.prepareStatement(SQL_LOG)) {
                log.setLong(1, notifId);
                log.setString(2, "enqueued");
                log.setString(3, "Notification enqueued to outbox");
                log.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("[NotificationService] enqueueEmail error: " + e.getMessage());
        }
        return notifId;
    }

    public long enqueueReservaCreada(String to, String nombreArea,
                                     String fecha, String horaIni, String horaFin) {
        String subject = "Notificación de reserva";
        // Texto RN4)
        String body = "Estimado residente, su reserva para el área común " + nombreArea +
                      " a sido confirmada exitosamente para el día " + fecha +
                      " en el horario de " + horaIni + " a " + horaFin + ". " +
                      "Le recordamos revisar las políticas de uso del espacio, respetar los tiempos asignados y notificar con al menos 24 horas de anticipación en caso de cancelación o modificación." +
                      "¡Gracias por contribuir a un uso ordenado de nuestros recursos comunitarios!";
        return enqueueEmail(to, subject, body);
    }

    /**
     * Procesa el aviso:
     *  - Si destinoTipo = "UNO": envía al correo específico.
     *  - Si destinoTipo = "ALL": obtiene correos de residentes activos y envía a todos.
     * Devuelve la cantidad de notificaciones encoladas exitosamente.
     */
    public int crearYEnviar(Aviso a) {
        if (a == null) return 0;

        String tipo = a.getDestinatarioTipo() == null ? "" : a.getDestinatarioTipo().trim().toUpperCase();
        String subject = a.getAsunto() == null ? "" : a.getAsunto().trim();
        String body    = a.getMensaje() == null ? "" : a.getMensaje().trim();

        if (subject.isEmpty() || body.isEmpty()) {
            // No hay nada que encolar si faltan datos esenciales
            return 0;
        }

        // Armar lista de destinatarios según el tipo
        Set<String> destinatarios = new LinkedHashSet<>();

        if ("UNO".equals(tipo)) {
            String email = a.getDestinatarioEmail() == null ? "" : a.getDestinatarioEmail().trim();
            if (!email.isEmpty()) destinatarios.add(email);
        } else if ("ALL".equals(tipo)) {
            try {
                List<String> correos = usuarioDAO.listarCorreosResidentesActivos();
                if (correos != null) {
                    for (String c : correos) {
                        if (c != null && !c.trim().isEmpty()) destinatarios.add(c.trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("[NotificationService] Error obteniendo correos ALL: " + e.getMessage());
            }
        } else {
            // Tipo desconocido
            return 0;
        }

        if (destinatarios.isEmpty()) return 0;

        int encolados = 0;
        for (String to : destinatarios) {
            long id = enqueueEmail(to, subject, body);
            if (id > 0) encolados++;
        }
        return encolados;
    }
}
