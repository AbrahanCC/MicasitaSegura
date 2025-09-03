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
    session = Session.getInstance(p, new Authenticator() {
      @Override protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pass);
      }
    });
  }

  public void sendHtml(String to, String subject, String html) throws Exception {
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(from, "Accesos"));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    msg.setSubject(subject, "UTF-8");
    msg.setContent(html, "text/html; charset=UTF-8");
    Transport.send(msg);
  }

  public void sendWithInlinePng(String to, String subject, String htmlBody, byte[] png) throws Exception {
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
    msg.setFrom(new InternetAddress(from, "Accesos"));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    msg.setSubject(subject, "UTF-8");
    msg.setContent(mp);
    Transport.send(msg);
  }
}
