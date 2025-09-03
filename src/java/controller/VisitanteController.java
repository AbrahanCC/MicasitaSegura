package controller;

import dao.VisitanteDAO;
import dao.VisitanteDAOImpl;
import model.Visitante;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import service.MailService;
import util.QRUtil;

public class VisitanteController extends HttpServlet {
    private final VisitanteDAO visitanteDAO = new VisitanteDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String op = val(req.getParameter("op"), "list");

        switch (op) {
            case "new":
                req.getRequestDispatcher("/view/guardia/visitante-form.jsp").forward(req, resp);
                return;

            case "validar": { // GET /visitantes?op=validar&token=...
                String token = trim(req.getParameter("token"));
                if (token.isEmpty()) {
                    req.setAttribute("error", "Token requerido.");
                } else {
                    Visitante v = visitanteDAO.obtenerPaseVigentePorToken(token);
                    if (v == null) {
                        req.setAttribute("error", "Pase inválido o caducado.");
                    } else {
                        req.setAttribute("msg", "Pase vigente para: " + v.getNombre() +
                                (v.getExpiraEn() != null ? " (vence: " + v.getExpiraEn() + ")" : ""));
                        req.setAttribute("visitante", v);
                    }
                }
                break;
            }

            case "consumir": { // GET /visitantes?op=consumir&token=...
                String token = trim(req.getParameter("token"));
                if (token.isEmpty()) {
                    req.setAttribute("error", "Token requerido.");
                } else {
                    boolean ok = visitanteDAO.marcarConsumidoPorToken(token);
                    req.setAttribute(ok ? "msg" : "error",
                            ok ? "Pase consumido." : "No se pudo consumir (ya usado o inválido).");
                }
                break;
            }

            case "scan": { // GET /visitantes?op=scan[&auto=1]
                req.getRequestDispatcher("/view/guardia/scan.jsp").forward(req, resp);
                return;
            }

            default:
                // list
        }

        String desde   = trim(req.getParameter("desde"));
        String hasta   = trim(req.getParameter("hasta"));
        String destino = trim(req.getParameter("destinoNumeroCasa"));
        String dpi     = trim(req.getParameter("dpi"));

        List<Visitante> data = visitanteDAO.listar(desde, hasta, destino, dpi);
        req.setAttribute("data", data);
        req.getRequestDispatcher("/view/guardia/visitante-lista.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Campos mínimos del formulario
        String nombre = trim(req.getParameter("nombre"));
        String motivo = trim(req.getParameter("motivo"));
        String lote   = trim(req.getParameter("lote"));
        String numero = trim(req.getParameter("numeroCasa"));
        String email  = trim(req.getParameter("email")); // opcional

        // Construcción del modelo
        Visitante v = new Visitante();
        v.setNombre(nombre);
        v.setMotivo(motivo);
        v.setFechaHora(new Timestamp(System.currentTimeMillis()));
        v.setDestinoNumeroCasa(lote.isEmpty() || numero.isEmpty() ? "" : (lote + "-" + numero));
        v.setCreadoPorGuardiaId(null); // ya no se usa

        // Pase SIEMPRE de 10 minutos, 1 solo uso
        if (!email.isEmpty()) v.setEmail(email);
        v.setToken(UUID.randomUUID().toString().replace("-", ""));
        v.setExpiraEn(Timestamp.from(Instant.now().plus(10, ChronoUnit.MINUTES)));
        v.setEstado("emitido");

        boolean ok = visitanteDAO.crear(v);
        if (ok) {
            req.setAttribute("msg", "Visitante registrado (pase emitido)");

            // Enviar correo (si hay email) con QR que apunta a /api/validate
            if (v.getEmail() != null && !v.getEmail().trim().isEmpty()) {
                try {
                    String base = req.getScheme() + "://" + req.getServerName()
                            + ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : (":" + req.getServerPort()))
                            + req.getContextPath(); // p.ej. http://localhost:8080/MiCasitaSegura
                    String url = base + "/api/validate?token=" + v.getToken();

                    byte[] png = QRUtil.makeQRPng(url, 400);
                    String asunto = "Pase de acceso temporal";
                    String body = "<h3>MiCasitaSegura</h3>"
                            + "<p>Hola, este es tu pase temporal.</p>"
                            + "<p>Escanéalo en la entrada o abre esta URL si lo necesitas: "
                            + "<a href=\"" + url + "\">" + url + "</a></p>";
                    new MailService().sendWithInlinePng(v.getEmail(), asunto, body, png);
                } catch (Exception mailEx) {
                    System.err.println("WARN email: " + mailEx.getMessage());
                }
            }

            // Ir al escáner (evita 403 pasando por el controlador)
            resp.sendRedirect(req.getContextPath() + "/visitantes?op=scan&auto=1");
            return;

        } else {
            req.setAttribute("error", "No se pudo registrar");
            doGet(req, resp);
        }
    }

    /* ----------------- helpers ----------------- */
    private static String trim(String s) { return s == null ? "" : s.trim(); }
    private static String val(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }
}
