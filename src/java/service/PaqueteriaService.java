package service;

import dao.PaqueteDAO;
import dao.PaqueteDAOImpl;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Paquete;
import model.Usuario;

import java.util.List;

public class PaqueteriaService {

    private final PaqueteDAO paqueteDAO = new PaqueteDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final NotificacionService notif = new NotificacionService();

    /** Registrar recepción de paquete (RN1 + flujo 3.3.1 a 3.3.6). */
    public long registrarRecepcion(String numeroGuia, int destinatarioId, int guardiaId, String observaciones) throws Exception {
        if (numeroGuia == null || numeroGuia.trim().isEmpty())
            throw new IllegalArgumentException("Número de guía es obligatorio.");
        if (destinatarioId <= 0) throw new IllegalArgumentException("Destinatario inválido.");
        if (guardiaId <= 0) throw new IllegalArgumentException("Sesión de guardia inválida.");

        // snapshot de casa/lote desde usuarios
        Usuario dest = usuarioDAO.obtener(destinatarioId);
        if (dest == null || !dest.isActivo() || dest.getRolId() != 3)
            throw new IllegalStateException("El destinatario no es un residente activo.");

        Paquete p = new Paquete();
        p.setNumeroGuia(numeroGuia.trim());
        p.setDestinatarioId(destinatarioId);
        p.setCasaDestinatario(dest.getNumeroCasa());
        p.setLoteDestinatario(dest.getLote());
        p.setRecibidoPor(guardiaId);
        p.setObservaciones(observaciones);

        return paqueteDAO.crear(p);
    }

    /** Lista de pendientes con filtro (RN03, RN04). */
    public List<Paquete> listarPendientes(String filtro) throws Exception {
        return paqueteDAO.listarPendientes(filtro == null ? "" : filtro.trim());
    }

    /** Entregar paquete: cambia estado y envía notificación (RN2, 3.3.7-3.3.11). */
    public boolean entregarPaquete(long paqueteId, int guardiaId) throws Exception {
        if (paqueteId <= 0) throw new IllegalArgumentException("Paquete inválido.");
        if (guardiaId <= 0) throw new IllegalArgumentException("Sesión de guardia inválida.");

        Paquete p = paqueteDAO.obtener(paqueteId);
        if (p == null) throw new IllegalStateException("Paquete no existe.");
        if (!"PENDIENTE".equals(p.getEstado())) throw new IllegalStateException("El paquete no está pendiente.");

        boolean ok = paqueteDAO.marcarEntregado(paqueteId, guardiaId);
        if (ok) {
            // RN2: notificar al residente
            String correo = usuarioDAO.obtenerCorreoPorId(p.getDestinatarioId());
            if (correo != null) {
                String asunto = "Entrega de Paquetería";
                String cuerpo = "Se le informa que se ha entregado paquete con identificación "
                        + safe(p.getNumeroGuia()) + ", en la fecha <b>" + java.time.LocalDateTime.now()
                        + "</b>.";
                notif.enqueueEmail(correo, asunto, cuerpo);
            }
        }
        return ok;
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}
