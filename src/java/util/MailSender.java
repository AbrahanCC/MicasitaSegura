package util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailSender {
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    public static void enviarAsync(String to, String subject, String body) {
        EXEC.submit(() -> {
            try { enviar(to, subject, body); }
            catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private static void enviar(String to, String subject, String body) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", MailConfig.HOST);
        props.put("mail.smtp.port", MailConfig.PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConfig.USER, MailConfig.PASS);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(MailConfig.FROM));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject);
        msg.setText(body);

        Transport.send(msg);
    }
}
