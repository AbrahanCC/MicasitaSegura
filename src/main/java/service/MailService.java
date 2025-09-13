package service;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.Properties;
import javax.mail.util.ByteArrayDataSource;

public class MailService {
  private final Session session;
  private final String from;          // From visible
  private final String envelopeFrom;  // Return-Path (rebotes)

  public MailService() {
    // Config SMTP
    String host = System.getProperty("SMTP_HOST", "smtp.gmail.com");
    String port = System.getProperty("SMTP_PORT", "587");
    String user = System.getProperty("SMTP_USER", "noreply@tudominio.com");
    String pass = System.getProperty("SMTP_PASS", "cambia-esto");
    from         = System.getProperty("SMTP_FROM", user);
    envelopeFrom = System.getProperty("SMTP_ENVELOPE_FROM", user); // casilla que recibirá rebotes

    Properties p = new Properties();
    p.put("mail.smtp.host", host);
    p.put("mail.smtp.port", port);
    p.put("mail.smtp.auth", "true");
    p.put("mail.smtp.starttls.enable", "true");
    // Envelope sender (Return-Path) → rebotes irán aquí
    p.put("mail.smtp.from", envelopeFrom);

    // Autenticación con usuario/contraseña del SMTP
    session = Session.getInstance(p, new Authenticator() {
      @Override protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pass);
      }
    });
  }

  // Envío HTML simple, silencioso (no lanza excepción)
  public void sendHtml(String to, String subject, String html) {
    try {
      if (to == null || to.trim().isEmpty()) return;
      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from, "Accesos"));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
      msg.setSubject(subject, "UTF-8");
      msg.setContent(html, "text/html; charset=UTF-8");
      Transport.send(msg);
    } catch (Exception e) {
      System.err.println("[MailService] sendHtml fallo → " + e.getMessage());
      // silencioso: no re-lanzar
    }
  }

  // Envío con PNG inline (QR), silencioso (no lanza excepción)
  public void sendWithInlinePng(String to, String subject, String htmlBody, byte[] png) {
    try {
      if (to == null || to.trim().isEmpty() || png == null || png.length == 0) return;

      // Parte del cuerpo en HTML + inserción del QR inline
      MimeBodyPart html = new MimeBodyPart();
      html.setContent(htmlBody + "<br><img src=\"cid:qr\">", "text/html; charset=UTF-8");

      // Parte de la imagen QR como adjunto inline
      MimeBodyPart img = new MimeBodyPart();
      img.setDataHandler(new DataHandler(new ByteArrayDataSource(png, "image/png")));
      img.setFileName("qr.png");
      img.setHeader("Content-ID", "<qr>");
      img.setDisposition(MimeBodyPart.INLINE);

      // Empaquetar cuerpo + imagen en un multipart
      MimeMultipart mp = new MimeMultipart("related");
      mp.addBodyPart(html);
      mp.addBodyPart(img);

      // Crear el mensaje final
      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from, "Accesos"));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
      // Asunto
      msg.setSubject(subject, "UTF-8");
      msg.setContent(mp);

      // Envío del correo
      Transport.send(msg);
    } catch (Exception e) {
      System.err.println("[MailService] sendWithInlinePng fallo → " + e.getMessage());
      // silencioso: no re-lanzar
    }
  }
}
