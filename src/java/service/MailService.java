package service;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.Properties;
import javax.mail.util.ByteArrayDataSource;

// Servicio central de correo
public class MailService {
  private final Session session;
  private final String from;          
  private final String envelopeFrom; 

  public MailService() {
    // Config SMTP (ajusta con tus credenciales/servidor)
    String host = System.getProperty("SMTP_HOST", "smtp.gmail.com");
    String port = System.getProperty("SMTP_PORT", "587");
    String user = System.getProperty("SMTP_USER", "noreply@tudominio.com"); // <-- TU USUARIO SMTP
    String pass = System.getProperty("SMTP_PASS", "cambia-esto");            // <-- TU CONTRASEÑA SMTP (App Password en Gmail)
    from         = System.getProperty("SMTP_FROM", user);                    // <-- REMITENTE VISIBLE
    envelopeFrom = System.getProperty("SMTP_ENVELOPE_FROM", user);           // <-- RETURN-PATH (REBOTES)

    Properties p = new Properties();
    p.put("mail.smtp.host", host);
    p.put("mail.smtp.port", port);
    p.put("mail.smtp.auth", "true");
    p.put("mail.smtp.starttls.enable", "true");
    p.put("mail.smtp.from", envelopeFrom);

    // Autenticación con usuario/contraseña del SMTP
    session = Session.getInstance(p, new Authenticator() {
      @Override protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pass);
      }
    });
  }

//Correo para VISITANTE con QR inline
  public void sendAccesoVisitante(String to, String nombreVisitante, String tipo, Integer usosMax, byte[] qrPng) {
    // Cuerpo HTML estandarizado para visitante
    String validez = "visita".equalsIgnoreCase(tipo)
        ? "Válido el día de la visita"
        : (usosMax != null ? (usosMax + " intentos") : "Válido por intentos");
    String cuerpoVisitante =
        "¡Hola!<br>Se ha generado exitosamente tu <b>código QR de acceso</b> al residencial.<br><br>" +
        "<b>Nombre del visitante:</b> " + safe(nombreVisitante) + "<br>" +
        "<b>Validez del código QR:</b> " + validez + "<br><br>" +
        "<b>Instrucciones importantes:</b><br>" +
        "• Guarda este correo o el código QR adjunto.<br>" +
        "• Preséntalo al llegar al residencial para que el personal de seguridad lo escanee y valide tu acceso.<br>" +
        "¡Gracias por coordinar tu visita con anticipación!";

    sendWithInlinePng(to, "Notificación de accesos creados", cuerpoVisitante, qrPng);
  }

  // Correo para RESIDENTE con QR inline (permanente/ilimitado
  public void sendAccesoResidente(String to, String nombreCompletoResidente, byte[] qrPng) {
    String cuerpoResidente =
        "<p>¡Hola!</p>" +
        "<p>Se ha generado exitosamente tu <b>código QR de acceso</b> al residencial.</p>" +
        "<p><b>Nombre del Residente:</b> " + safe(nombreCompletoResidente) + "</p>" +
        "<p><b>Validez del código QR:</b> ILIMITADO</p>" +
        "<p><b>Instrucciones importantes:</b><br>" +
        "Guarda este correo o el QR adjunto.<br>" +
        "Preséntalo al llegar al residencial para que el personal de seguridad lo valide.</p>";

    sendWithInlinePng(to, "Notificación de accesos creados", cuerpoResidente, qrPng);
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
    }
  }

  // Envío con PNG (QR), silencioso
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
      msg.setSubject(subject, "UTF-8");
      msg.setContent(mp);

      // Envío del correo
      Transport.send(msg);
    } catch (Exception e) {
      System.err.println("[MailService] sendWithInlinePng fallo → " + e.getMessage());
      // silencioso: no re-lanzar
    }
  }
  
  private static String safe(String s) {
    if (s == null) return "";
    return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
  }
}
