package service;

import dao.IncidenteDAO;
import dao.IncidenteDAOImpl;
import model.Incidente;

import java.sql.Timestamp;
import java.util.List;

public class IncidenteService {
    private final IncidenteDAO dao = new IncidenteDAOImpl();
    private final ConversacionService convService = new ConversacionService();
    private final MailService mail = new MailService();

    public Incidente crearYNotificar(int idResidente, String tipo, Timestamp fechaHora, String descripcion,
                                     String nombreResidente, String numeroCasa) {
        if (tipo == null || fechaHora == null || descripcion == null || descripcion.trim().isEmpty() || descripcion.length() > 200) {
            throw new RuntimeException("Complete todos los campos correctamente (descr. <= 200)");
        }
        Incidente inc = new Incidente(idResidente, tipo, fechaHora, descripcion.trim());
        inc = dao.create(inc);

        // RN4: avisar a guardias activos
        List<ConversacionService.UsuarioMin> guardias = convService.listarGuardiasActivos();
        for (ConversacionService.UsuarioMin g : guardias) {
            String subject = "Reporte de incidente";
            String body = "Se le informa que el residente <b>" + nombreResidente + "</b>, que vive en <b>" + numeroCasa + "</b>, ha reportado un incidente:<br><br>"
                        + "<b>" + tipo + "</b><br>"
                        + fechaHora + "<br>"
                        + descripcion + "<br><br>"
                        + "Por favor, tomar las acciones correspondientes.";
            mail.sendHtml(g.correo, subject, body);
        }
        return inc;
    }
}
