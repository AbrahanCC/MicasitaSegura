package controller;

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

//Servlet que recibe el formulario y emite el pase de visita-Registrar visitantes
@WebServlet("/api/emit")
public class EmitirVisitaServlet extends HttpServlet {

  //Servicio que guarda el pase en BD y devuelve el token del QR. *
  private final EmisionVisitanteService emision = new EmisionVisitanteService();
  //Servicio de correo para notificar a residente y visitante.
  private final MailService mail = new MailService();
  //DAO para ubicar al residente dueño del pase según lote y casa
  private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

  //Maneja el envío del formulario para crear el pase y enviar notificaciones.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");

    // Datos del formulario
    String nombre   = nz(req.getParameter("nombre"));
    String dpi      = nz(req.getParameter("dpi"));
    String email    = nz(req.getParameter("email"));
    String motivo   = nz(req.getParameter("motivo"));
    String destino  = nz(req.getParameter("destino"));       
    String tipo     = nz(req.getParameter("visit_type"));     // "por_intentos" | "visita"
    String usosStr  = nz(req.getParameter("usos_max"));       // solo para por_intentos
    String fvisita  = nz(req.getParameter("fecha_visita"));   // solo para visita (datetime-local)

    // Usuario en sesión quien emite
    Integer guardId = null;
    HttpSession s = req.getSession(false);
    if (s != null) {
      Object uid = s.getAttribute("uid");
      if (uid instanceof Integer) guardId = (Integer) uid;
    }

    try {
      // Validaciones mínimas de requeridos
      if (nombre.isEmpty() || motivo.isEmpty() || destino.isEmpty() || tipo.isEmpty()) {
        throw new IllegalArgumentException("Faltan campos obligatorios.");
      }
      if (!("por_intentos".equals(tipo) || "visita".equals(tipo))) {
        throw new IllegalArgumentException("Tipo de visita inválido.");
      }

      // RN4: intentos > 1 cuando es por_intentos
      Integer usosMax = null;
      if ("por_intentos".equals(tipo)) {
        usosMax = parseEntero(usosStr);
        if (usosMax == null || usosMax <= 1) throw new IllegalArgumentException("Los usos deben ser mayores a 1.");
      }

      // RN5: fecha no pasada cuando es visita
      Timestamp qrInicio = null, qrFin = null;
      if ("visita".equals(tipo)) {
        LocalDateTime ldt = parseFechaVisita(fvisita);
        if (ldt == null) throw new IllegalArgumentException("Fecha de visita requerida.");
        if (ldt.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("La fecha no puede ser pasada.");
        // Ventana del día de la visita (inicio de día a fin de día)
        LocalDate d = ldt.toLocalDate();
        qrInicio = Timestamp.valueOf(d.atStartOfDay());
        qrFin    = Timestamp.valueOf(d.atTime(LocalTime.MAX).withNano(0));
        usosMax  = 2; // dos usos (entrada/salida) dentro de la fecha
      }

      // Resolver residente dueño del pase
      String[] lc = partirDestino(destino);
      Integer residenteId = (lc == null) ? null : usuarioDAO.findResidenteId(lc[1], lc[0]); // numeroCasa, lote
      if (residenteId == null) throw new IllegalStateException("No existe residente activo para la casa " + destino);

      // Guardar y obtener token
      String token = emision.emitirVisitante(
          residenteId, nombre, dpi, email, motivo, destino,
          tipo, usosMax, qrInicio, qrFin, guardId
      );

      // URL absoluta para validar
      String base = req.getScheme() + "://" + req.getServerName()
          + ((req.getServerPort()==80 || req.getServerPort()==443) ? "" : ":" + req.getServerPort())
          + req.getContextPath();
      String url = base + "/api/validate?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name());

      // Enviar correos (RN6 residente, RN7 visitante)
      try {
        byte[] png = QRUtil.makeQRPng(url, 300);

        // Visitante (RN7)
        if (!email.isEmpty()) {
          // usar plantilla de MailService para visitante
          mail.sendAccesoVisitante(email, nombre, tipo, usosMax, png); // 
        }

        // Residente (RN6) — si se pudo identificar el correo
        Usuario residente = usuarioDAO.obtener(residenteId); // 
        String correoResidente = residente != null ? residente.getCorreo() : null;
        if (correoResidente != null && !correoResidente.trim().isEmpty()) {
          String nombreResidente = (residente.getNombre() == null ? "" : residente.getNombre()) + " " +
                                   (residente.getApellidos() == null ? "" : residente.getApellidos()); // 
          mail.sendAccesoResidente(correoResidente, nombreResidente.trim(), png); // 
        }
      } catch (Exception ignore) { }

      // Feedback en la vista y mostrar botón de descarga (FA05)
      req.setAttribute("ok", true);
      req.setAttribute("token", token);
      req.setAttribute("nombreMostrado", nombre.isEmpty() ? "Visitante" : nombre);
      req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);

    } catch (Exception ex) {
      req.setAttribute("error", ex.getMessage());
      req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);
    }
  }

  //Convierte una cadena a entero o null si no es válido
  private Integer parseEntero(String n) {
    try { return (n == null || n.trim().isEmpty()) ? null : Integer.parseInt(n.trim()); }
    catch (Exception e) { return null; }
  }

  //Pasa la fecha/hora enviada desde un input datetime-local
  private LocalDateTime parseFechaVisita(String s) {
    try {
      if (s == null || s.trim().isEmpty()) return null;
      return LocalDateTime.parse(s.trim());
    } catch (Exception e) { return null; }
  }

  //Separa lote y numeroCasa]. */
  private String[] partirDestino(String d) {
    if (d == null) return null;
    String x = d.trim().toUpperCase();
    int p = x.indexOf('-');
    if (p < 0) return null;
    String lote = x.substring(0, p);
    String casa = x.substring(p + 1);
    if (lote.isEmpty() || casa.isEmpty()) return null;
    return new String[]{lote, casa};
  }

  // Devuelve una cadena no nula.
  private static String nz(String s) { return s == null ? "" : s.trim(); }

  // Escapa caracteres básicos para HTML.
  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
  }

  // Fecha actual en texto corto. 
  private static String fechaHoyTexto() {
    LocalDate d = LocalDate.now();
    return String.format("%02d/%02d/%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
  }

  // Hora actual en texto corto.
  private static String horaAhoraTexto() {
    LocalTime t = LocalTime.now().withSecond(0).withNano(0);
    return String.format("%02d:%02d", t.getHour(), t.getMinute());
  }
}
