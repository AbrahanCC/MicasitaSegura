package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import model.Aviso;
import util.DBConnection;

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

    public int crearYEnviar(Aviso a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
