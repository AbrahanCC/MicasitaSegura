package service;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.Properties;
import javax.mail.util.ByteArrayDataSource;

public class MailService {
  private final Session session;
  private final String from;

  public MailService() {
    // Configuración SMTP: aquí se prepara la conexión con el servidor de correo
    String host = System.getProperty("SMTP_HOST", "smtp.gmail.com");
    String port = System.getProperty("SMTP_PORT", "587");
    String user = System.getProperty("SMTP_USER");
    String pass = System.getProperty("SMTP_PASS");
    from = user;

    Properties p = new Properties();
    p.put("mail.smtp.host", host);
    p.put("mail.smtp.port", port);
    p.put("mail.smtp.auth", "true");
    p.put("mail.smtp.starttls.enable", "true");

    // Autenticación con usuario/contraseña del SMTP
    session = Session.getInstance(p, new Authenticator() {
      @Override protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pass);
      }
    });
  }

  // Método genérico para enviar correos con contenido HTML simple
  public void sendHtml(String to, String subject, String html) throws Exception {
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(from, "Accesos"));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    msg.setSubject(subject, "UTF-8");
    msg.setContent(html, "text/html; charset=UTF-8");
    Transport.send(msg);
  }

   //Método para enviar correos con un QR incrustado
   //El sistema ENVÍA una notificación con el QR adjunto.
   //Cuerpo del mensaje el texto requerido.

  public void sendWithInlinePng(String to, String subject, String htmlBody, byte[] png) throws Exception {
    // Parte del cuerpo en HTML + inserción del QR inline
    MimeBodyPart html = new MimeBodyPart();
    html.setContent(htmlBody + "<br><img src=\"cid:qr\">", "text/html; charset=UTF-8");

    // Parte de la imagen QR como adjunto 
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
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

    // RN3(2): Asunto fijo "Notificación de accesos creados"
    msg.setSubject(subject, "UTF-8");

    msg.setContent(mp);

    // Envío del correo
    Transport.send(msg);
  }
}
