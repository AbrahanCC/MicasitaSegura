package service;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Aviso;

import java.util.List;

public class NotificacionService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final MailService mail = new MailService();

    /** Envía y devuelve CUÁNTOS correos se mandaron realmente. */
    public int crearYEnviar(Aviso a) {
        int enviados = 0;

        // arma HTML sencillo con tu mensaje
        String html = buildHtml(a.getMensaje());

        try {
            if ("UNO".equalsIgnoreCase(a.getDestinatarioTipo())) {
                if (a.getDestinatarioEmail() != null && !a.getDestinatarioEmail().trim().isEmpty()) {
                    mail.sendHtml(a.getDestinatarioEmail().trim(), a.getAsunto(), html);
                    enviados = 1;
                }
            } else { // ALL
                List<String> correos = usuarioDAO.listarCorreosResidentesActivos();
                for (String c : correos) {
                    try {
                        mail.sendHtml(c, a.getAsunto(), html);
                        enviados++;
                    } catch (Exception ex) {
                        System.err.println("Fallo enviando a " + c + ": " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error enviando avisos: " + e.getMessage(), e);
        }

        return enviados;
    }

    private String buildHtml(String mensajePlano) {
        String safe = (mensajePlano == null) ? "" : mensajePlano
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<div style='font-family:Segoe UI,Arial,sans-serif;font-size:14px'>"
             + "<p>Estimado(a) residente,</p>"
             + "<p>" + safe + "</p>"
             + "<hr style='border:none;border-top:1px solid #eee;margin-top:16px;'>"
             + "<p style='color:#888'>MiCasitaSegura</p>"
             + "</div>";
    }
}
