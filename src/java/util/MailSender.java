package util;

import service.MailService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailSender {
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final MailService MAIL = new MailService(); // usa únicamente MailService

    public static void enviarAsync(String to, String subject, String body) {
        EXEC.submit(() -> {
            try { enviar(to, subject, body); }
            catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private static void enviar(String to, String subject, String body) {
        // Delegamos el envío a MailService como HTML simple
        String html = "<pre style=\"font-family:inherit;white-space:pre-wrap;margin:0\">" + escape(body) + "</pre>";
        MAIL.sendHtml(to, subject, html);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
