package controller;

//Servlet que recibe el formulario y emite el pase de visita-Registrar visitantes
import dao.VisitanteDAO;
import dao.VisitanteDAOImpl;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Usuario;
import service.EmisionVisitanteService;
import util.QRUtil;
import service.MailService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.*;

@WebServlet("/api/emit")
public class EmitirVisitaServlet extends HttpServlet {

    // Servicios/DAO usados
    private final EmisionVisitanteService emision = new EmisionVisitanteService();
    private final MailService mail = new MailService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final VisitanteDAO visitanteDAO = new VisitanteDAOImpl(); // para catálogos

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // ===== 1) Leer parámetros (nombres alineados a la vista/BD) =====
        final String nombre      = nz(req.getParameter("nombre"));
        final String dpi         = nz(req.getParameter("dpi"));
        final String email       = nz(req.getParameter("correo"));
        final String motivo      = nz(req.getParameter("motivo"));
        final String lote        = nz(req.getParameter("lote"));
        final String numeroCasa  = nz(req.getParameter("numeroCasa"));
        final String tipo        = nz(req.getParameter("tipoVisita"));   // 'por_intentos' | 'visita'
        final String usosStr     = nz(req.getParameter("usosMax"));      // solo para 'por_intentos'
        final String fechaVisita = nz(req.getParameter("fechaVisita"));  // solo para 'visita' (yyyy-MM-dd)

        // Usuario en sesión (opcional, quién emitió)
        Integer guardId = null;
        HttpSession s = req.getSession(false);
        if (s != null) {
            Object uid = s.getAttribute("uid");
            if (uid instanceof Integer) guardId = (Integer) uid;
        }

        try {
            // ===== 2) Validaciones mínimas (RN) =====
            if (nombre.isEmpty() || lote.isEmpty() || numeroCasa.isEmpty() || tipo.isEmpty()) {
                throw new IllegalArgumentException("Faltan campos obligatorios.");
            }
            if (!("por_intentos".equals(tipo) || "visita".equals(tipo))) {
                throw new IllegalArgumentException("Tipo de visita inválido.");
            }

            // RN4: intentos > 1 cuando es por_intentos
            Integer usosMax = null;
            if ("por_intentos".equals(tipo)) {
                usosMax = parseEntero(usosStr);
                if (usosMax == null || usosMax <= 1) {
                    throw new IllegalArgumentException("Los usos deben ser mayores a 1.");
                }
            }

            // RN5: fecha no puede ser pasada cuando es 'visita'
            Timestamp qrFin = null;
            if ("visita".equals(tipo)) {
                LocalDate d = parseFechaVisita(fechaVisita);
                if (d == null) throw new IllegalArgumentException("Fecha de visita requerida.");
                if (d.isBefore(LocalDate.now())) throw new IllegalArgumentException("La fecha no puede ser pasada.");
                // Fin del día seleccionado (permite 2 usos: entrada/salida)
                qrFin = Timestamp.valueOf(d.atTime(LocalTime.MAX).withNano(0));
                usosMax = 2;
            }

            // ===== 3) NO validar residente (se permite cualquier lote/casa) =====
            Integer residenteId = null; // se guarda null en visitantes.usuario_id

            // ===== 4) Guardar pase y obtener token =====
            String token = emision.emitirVisitante(
                residenteId, nombre, dpi, email, motivo,
                lote, numeroCasa,              // guardar por columnas separadas
                tipo, usosMax, qrFin, guardId  // reglas según tipo
            );

            // ===== 5) Construir URL absoluta de validación (para el QR) =====
            String base = req.getScheme() + "://" + req.getServerName()
                    + ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort())
                    + req.getContextPath();
            String url = base + "/api/validate?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name());

            // ===== 6) Notificaciones por correo (solo al visitante si hay correo) =====
            try {
                byte[] png = QRUtil.makeQRPng(url, 300);

                // Visitante — si hay correo
                if (!email.isEmpty()) {
                    mail.sendAccesoVisitante(email, nombre, tipo, usosMax, png);
                }

                // Residente: omitido cuando no se resolvió (residenteId == null)
                if (residenteId != null) {
                    Usuario residente = usuarioDAO.obtener(residenteId);
                    if (residente != null && residente.getCorreo() != null && !residente.getCorreo().trim().isEmpty()) {
                        String nomRes = ((residente.getNombre() == null) ? "" : residente.getNombre()) + " " +
                                        ((residente.getApellidos() == null) ? "" : residente.getApellidos());
                        mail.sendAccesoResidente(residente.getCorreo(), nomRes.trim(), png);
                    }
                }
            } catch (Exception ignore) {
                // Evitar que un fallo de correo rompa el flujo principal
            }

            // ===== 7) Feedback a la vista (FA05/FA06 se gestionan en la UI) =====
            req.setAttribute("ok", true);
            req.setAttribute("token", token);
            req.setAttribute("nombreMostrado", nombre.isEmpty() ? "Visitante" : nombre);

            // Recargar catálogos para que el form vuelva poblado
            req.setAttribute("tiposVisita", visitanteDAO.catalogoVisita());
            req.setAttribute("lotes", visitanteDAO.catalogoLotes());
            req.setAttribute("casas", visitanteDAO.catalogoCasas());

            req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);

        } catch (Exception ex) {
            // Recargar catálogos también en error para no dejar combos vacíos
            req.setAttribute("tiposVisita", visitanteDAO.catalogoVisita());
            req.setAttribute("lotes", visitanteDAO.catalogoLotes());
            req.setAttribute("casas", visitanteDAO.catalogoCasas());

            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);
        }
    }

    // --- Utilidades ---

    private static String nz(String s) { return s == null ? "" : s.trim(); }

    private static Integer parseEntero(String n) {
        try { return (n == null || n.trim().isEmpty()) ? null : Integer.parseInt(n.trim()); }
        catch (Exception e) { return null; }
    }

    // Espera formato yyyy-MM-dd desde <input type="date" name="fechaVisita">
    private static LocalDate parseFechaVisita(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return LocalDate.parse(s.trim());
        } catch (Exception e) { return null; }
    }
}
