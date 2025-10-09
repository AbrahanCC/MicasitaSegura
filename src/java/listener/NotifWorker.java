package listener;

import service.MailService;
import util.DBConnection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class NotifWorker implements ServletContextListener {

    private ScheduledExecutorService exec;

    // Puedes ajustar el intervalo con -DNOTIF_DELAY_SECONDS=5
    private static final int DELAY_SECONDS = Integer.getInteger("NOTIF_DELAY_SECONDS", 10);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleWithFixedDelay(new Runnable() {
            @Override public void run() { tick(); }
        }, 2, DELAY_SECONDS, TimeUnit.SECONDS);

        System.out.println("[NotifWorker] started (interval=" + DELAY_SECONDS + "s)");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (exec != null) exec.shutdownNow();
        System.out.println("[NotifWorker] stopped");
    }

    private void tick() {
        System.out.println("[NotifWorker] tick...");
        try (Connection cn = new DBConnection().getConnection()) {
            cn.setAutoCommit(false);

            // 1) Tomar una tanda bloqueada (FOR UPDATE) de PENDING
            List<Row> batch = new ArrayList<Row>();
            String sel = "SELECT id, recipient, subject, body_html " +
                         "FROM notif_outbox WHERE status='PENDING' " +
                         "ORDER BY id LIMIT 10 FOR UPDATE";
            try (PreparedStatement ps = cn.prepareStatement(sel);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batch.add(new Row(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                }
            }

            System.out.println("[NotifWorker] batch size = " + batch.size());
            if (batch.isEmpty()) { cn.rollback(); return; }

            // 2) Marcar como SENDING
            try (PreparedStatement upd = cn.prepareStatement("UPDATE notif_outbox SET status='SENDING' WHERE id=?")) {
                for (Row r : batch) { upd.setLong(1, r.id); upd.addBatch(); }
                upd.executeBatch();
            }
            cn.commit();

            // 3) Enviar fuera de la transacción
            for (Row r : batch) {
                sendOne(r);
            }

        } catch (Exception e) {
            System.err.println("[NotifWorker] tick error: " + e.getMessage());
        }
    }

    private void sendOne(Row r) {
        System.out.println("[NotifWorker] sending id=" + r.id + " to=" + r.to + " subject=" + r.subject);
        log(r.id, "ATTEMPT", "Sending to " + r.to);
        try {
            new MailService().sendHtml(r.to, r.subject, r.html);

            // Marcar SENT
        try (Connection c2 = DBConnection.getConnection();
                 PreparedStatement ok = c2.prepareStatement(
                         "UPDATE notif_outbox SET status='SENT', sent_at=NOW() WHERE id=?")) {
                ok.setLong(1, r.id);
                ok.executeUpdate();
            }
            log(r.id, "SENT", "OK");
            System.out.println("[NotifWorker] SENT id=" + r.id);

        } catch (Exception ex) {
            System.err.println("[NotifWorker] FAILED id=" + r.id + " -> " + ex.getMessage());
            // Reintentos: si ya tiene >=5, pasa a FAILED; si no, vuelve a PENDING
        try (Connection c2 = DBConnection.getConnection();
                 PreparedStatement fail = c2.prepareStatement(
                         "UPDATE notif_outbox " +
                         "SET status = CASE WHEN retry_count>=5 THEN 'FAILED' ELSE 'PENDING' END, " +
                         "    retry_count = retry_count + 1, last_error=? " +
                         "WHERE id=?")) {
                fail.setString(1, ex.getMessage());
                fail.setLong(2, r.id);
                fail.executeUpdate();
            } catch (Exception ignore) {}
            log(r.id, "FAILED", ex.getMessage());
        }
    }

    private void log(long id, String ev, String detail) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO notif_log (notif_id, event, detail) VALUES (?,?,?)")) {
            ps.setLong(1, id);
            ps.setString(2, ev);
            ps.setString(3, detail);
            ps.executeUpdate();
        } catch (Exception ignore) {}
    }

    // ---- Clase interna (Java 8) ----
    private static class Row {
        final long id;
        final String to;
        final String subject;
        final String html;
        Row(long id, String to, String subject, String html) {
            this.id = id;
            this.to = to;
            this.subject = subject;
            this.html = html;
        }
    }
}
