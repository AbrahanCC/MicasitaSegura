package service;

import util.DBConnection;
import java.sql.*;

/** Encola notificaciones para ser enviadas por el worker (no envía aquí). */
public class Notifications {


     
    public static long enqueueEmail(String to, String subject, String bodyHtml, String correlationId) throws Exception {
        final String sql = "INSERT INTO notif_outbox " +
                "(recipient, subject, body_html, correlation_id) VALUES (?,?,?,?)";

        try (Connection cn = new DBConnection().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, to);
            ps.setString(2, subject);
            ps.setString(3, bodyHtml);
            ps.setString(4, correlationId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("No se obtuvo el ID generado de notif_outbox");
                }
            }
        }
    }
}
