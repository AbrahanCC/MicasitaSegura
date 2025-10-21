package service;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.List;
import java.util.Properties;
import javax.mail.util.ByteArrayDataSource;

// Servicio central de correo
public class MailService {
  private final Session session;
  private final String from;          // remitente visible
  private final String envelopeFrom;  // return-path

  public MailService() {
    // // Config SMTP (ajusta con tus credenciales/servidor)
    String host = System.getProperty("SMTP_HOST", "smtp.gmail.com");
    String port = System.getProperty("SMTP_PORT", "587");
    String user = System.getProperty("SMTP_USER", "acceso.talanquera@gmail.com"); 
    String pass = System.getProperty("SMTP_PASS", "tsusgaorojfswkum");           
    from         = System.getProperty("SMTP_FROM", "acceso.talanquera@gmail.com");                    
    envelopeFrom = System.getProperty("SMTP_ENVELOPE_FROM", "acceso.talanquera@gmail.com");           

    Properties p = new Properties();
    p.put("mail.smtp.host", host);
    p.put("mail.smtp.port", port);
    p.put("mail.smtp.auth", "true");
    p.put("mail.smtp.starttls.enable", "true");
    p.put("mail.smtp.from", envelopeFrom);

    // // Autenticación con usuario/contraseña del SMTP
    session = Session.getInstance(p, new Authenticator() {
      @Override protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pass);
      }
    });
  }

  // Utilidades base (HTML/PNG)
  public void sendHtml(String to, String subject, String html) {
    try {
      if (to == null || to.trim().isEmpty()) return;
      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from, "MiCasitaSegura"));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
      msg.setSubject(subject, "UTF-8");
      msg.setContent(html, "text/html; charset=UTF-8");
      Transport.send(msg);
    } catch (Exception e) {
      System.err.println("[MailService] sendHtml fallo → " + e.getMessage());
    }
  }

  public void sendHtmlToMany(List<String> tos, String subject, String html) {
    if (tos == null || tos.isEmpty()) return;
    for (String to : tos) sendHtml(to, subject, html); // // best-effort
  }

  public void sendWithInlinePng(String to, String subject, String htmlBody, byte[] png) {
    try {
      if (to == null || to.trim().isEmpty() || png == null || png.length == 0) return;

      MimeBodyPart html = new MimeBodyPart();
      html.setContent(htmlBody + "<br><img src=\"cid:qr\">", "text/html; charset=UTF-8");

      MimeBodyPart img = new MimeBodyPart();
      img.setDataHandler(new DataHandler(new ByteArrayDataSource(png, "image/png")));
      img.setFileName("qr.png");
      img.setHeader("Content-ID", "<qr>");
      img.setDisposition(MimeBodyPart.INLINE);

      MimeMultipart mp = new MimeMultipart("related");
      mp.addBodyPart(html);
      mp.addBodyPart(img);

      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from, "MiCasitaSegura"));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
      msg.setSubject(subject, "UTF-8");
      msg.setContent(mp);

      Transport.send(msg);
    } catch (Exception e) {
      System.err.println("[MailService] sendWithInlinePng fallo → " + e.getMessage());
    }
  }

  // Plantillas específicas (QR, Chat, Incidente)
  // // VISITANTE con QR inline
  public void sendAccesoVisitante(String to, String nombreVisitante, String tipo, Integer usosMax, byte[] qrPng) {
    String validez = "visita".equalsIgnoreCase(tipo)
        ? "Válido el día de la visita"
        : (usosMax != null ? (usosMax + " intentos") : "Válido por intentos");
    String cuerpo =
        "¡Hola!<br>Se ha generado tu <b>código QR de acceso</b>.<br><br>" +
        "<b>Nombre del visitante:</b> " + safe(nombreVisitante) + "<br>" +
        "<b>Validez del código QR:</b> " + safe(validez) + "<br><br>" +
        "<b>Instrucciones:</b><br>" +
        "• Guarda este correo o el QR adjunto.<br>" +
        "• Preséntalo al llegar para escaneo.";
    sendWithInlinePng(to, "Notificación de accesos creados", cuerpo, qrPng);
  }

  // // RESIDENTE con QR inline
  public void sendAccesoResidente(String to, String nombreCompletoResidente, byte[] qrPng) {
    String cuerpo =
        "<p>¡Hola!</p>" +
        "<p>Se ha generado tu <b>código QR de acceso</b>.</p>" +
        "<p><b>Nombre del Residente:</b> " + safe(nombreCompletoResidente) + "</p>" +
        "<p><b>Validez del código QR:</b> ILIMITADO</p>" +
        "<p><b>Instrucciones:</b> Guarda este correo o el QR adjunto y preséntalo al ingresar.</p>";
    sendWithInlinePng(to, "Notificación de accesos creados", cuerpo, qrPng);
  }

  // // Notificación de MENSAJE (RN6)
  public void sendNotificacionMensaje(String to, String nombreEmisor, String chatUrl) {
    if (to == null || to.trim().isEmpty()) return;
    String subject = "Notificación de mensaje";
    String body =
        "El usuario <b>" + safe(nombreEmisor == null ? "Usuario" : nombreEmisor) + "</b> le ha enviado un mensaje.<br>" +
        "Ingrese a <b>Consulta General</b> para revisar su conversación.<br><br>" +
        (chatUrl != null ? "<a href=\"" + safe(chatUrl) + "\">Abrir conversación</a>" : "");
    sendHtml(to, subject, body);
  }

  // // Notificación de INCIDENTE a un guardia (RN4)
  public void sendNotificacionIncidente(String to, String nombreResidente, String numeroCasa, String lote,
                                        String tipo, java.sql.Timestamp fechaHora, String descripcion) {
    if (to == null || to.trim().isEmpty()) return;
    String subject = "Reporte de incidente";

    // // Domicilio solo si hay ambos datos
    String domicilio = null;
    if (numeroCasa != null && !numeroCasa.trim().isEmpty() && lote != null && !lote.trim().isEmpty()) {
      domicilio = " que vive en la casa <b>" + safe(numeroCasa.trim()) + "</b> del lote <b>" + safe(lote.trim()) + "</b>";
    }

    String body =
        "Se le informa que el residente <b>" + safe(nombreResidente == null ? "Residente" : nombreResidente) + "</b>"
        + (domicilio != null ? domicilio : "") + ", ha reportado un incidente.<br><br>"
        + "<b>Tipo:</b> " + safe(tipo) + "<br>"
        + "<b>Fecha y hora:</b> " + safe(String.valueOf(fechaHora)) + "<br>"
        + "<b>Descripción:</b> " + safe(descripcion) + "<br><br>"
        + "Por favor, tomar las acciones correspondientes.";
    sendHtml(to, subject, body);
  }

  // // Versión masiva de la notificación de INCIDENTE (lista de correos)
  public void sendNotificacionIncidenteToMany(List<String> correos, String nombreResidente, String numeroCasa, String lote,
                                              String tipo, java.sql.Timestamp fechaHora, String descripcion) {
    if (correos == null || correos.isEmpty()) return;
    for (String to : correos) {
      sendNotificacionIncidente(to, nombreResidente, numeroCasa, lote, tipo, fechaHora, descripcion);
    }
  }

  // // Escapado mínimo para HTML
  private static String safe(String s) {
    if (s == null) return "";
    return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
  }
}
