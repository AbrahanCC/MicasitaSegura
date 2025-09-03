package WebS;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Properties;

@WebServlet("/test-mail")
public class MailTestServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String host = System.getProperty("SMTP_HOST");
    String port = System.getProperty("SMTP_PORT", "587");
    String user = System.getProperty("SMTP_USER");
    String pass = System.getProperty("SMTP_PASS");

    Properties p = new Properties();
    p.put("mail.smtp.host", host);
    p.put("mail.smtp.port", port);
    p.put("mail.smtp.auth", "true");
    p.put("mail.smtp.starttls.enable", "true");
    p.put("mail.smtp.connectiontimeout", "10000");
    p.put("mail.smtp.timeout", "10000");

    Session session = Session.getInstance(p, new Authenticator() {
      @Override protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pass);
      }
    });

    String to = user;
    try {
      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(user, "Prueba SMTP"));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
      msg.setSubject("Prueba SMTP desde Tomcat");
      msg.setText("Si ves este mensaje, tu SMTP funciona.");
      Transport.send(msg);

      resp.setContentType("text/plain; charset=UTF-8");
      resp.getWriter().println("OK: correo enviado a " + to);
    } catch (Exception e) {
      resp.setStatus(500);
      resp.setContentType("text/plain; charset=UTF-8");
      resp.getWriter().println("ERROR enviando correo: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
