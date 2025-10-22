package controller;

import model.Pago;
import model.Usuario;
import service.PagoService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name="PagoController", urlPatterns={"/residente/pagos"})
public class PagoController extends HttpServlet {

    private final PagoService service = new PagoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = nv(req.getParameter("action"), "listar");
        HttpSession session = req.getSession(false);
        Usuario user = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        try {
            validarSesion(user);

            switch (action) {
                case "listar": {
                    List<Pago> pagos = service.listarPorUsuario(user.getId());
                    req.setAttribute("pagos", pagos);
                    req.getRequestDispatcher("/view/residente/pagos.jsp").forward(req, resp);
                    break;
                }
                case "recibo": {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Pago p = service.obtener(id);
                    if (p == null || p.getUsuarioId() != user.getId())
                        throw new IllegalArgumentException("Recibo no disponible.");
                    req.setAttribute("pago", p);
                    req.getRequestDispatcher("/view/residente/pago-recibo.jsp").forward(req, resp);
                    break;
                }
                case "form": {
                    req.setAttribute("tipos", service.tiposActivos());
                    req.setAttribute("metodos", service.metodosActivos(user.getId()));
                    req.getRequestDispatcher("/view/residente/pago-form.jsp").forward(req, resp);
                    break;
                }
                default:
                    resp.sendRedirect(req.getContextPath() + "/residente/pagos?action=listar");
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("tipos", service.tiposActivos());
            req.getRequestDispatcher("/view/residente/pago-form.jsp").forward(req, resp);
        }
    }

    @Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String action = nv(req.getParameter("action"), "");
    HttpSession session = req.getSession(false);
    Usuario user = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        try {
            validarSesion(user);

            switch (action) {
                case "consultar": {
                    int tipoId = Integer.parseInt(req.getParameter("tipo_id"));
                    Pago calculo = service.preCalcular(user, tipoId);
                    req.setAttribute("calculo", calculo);
                    req.setAttribute("tipos", service.tiposActivos());
                    req.setAttribute("metodos", service.metodosActivos(user.getId()));
                    req.getRequestDispatcher("/view/residente/pago-form.jsp").forward(req, resp);
                    break;
                }
                case "registrar": {
                    int tipoId = Integer.parseInt(req.getParameter("tipo_id"));
                    String observ = nv(req.getParameter("observaciones"), "");

                    String usarGuardada = nv(req.getParameter("usar_guardada"), "si");
                    Integer metodoId = null;
                    if ("si".equalsIgnoreCase(usarGuardada)) {
                        String v = req.getParameter("metodo_pago_id");
                        if (v != null && !v.isEmpty()) metodoId = Integer.parseInt(v);
                    }

                    boolean guardarNueva = "no".equalsIgnoreCase(usarGuardada)
                            && "si".equalsIgnoreCase(nv(req.getParameter("guardar_nueva"), "no"));

                    String numero = req.getParameter("numero_tarjeta");
                    String marca  = req.getParameter("marca");
                    String titular= req.getParameter("nombre_titular");

                    // Si usas <input type="month" name="fecha_vencimiento"> => "YYYY-MM"
                    String venc = req.getParameter("fecha_vencimiento");
                    int mesExp = 0, anioExp = 0;
                    if (venc != null && !venc.isEmpty()) {
                        String[] p = venc.split("-");
                        if (p.length == 2) {
                            anioExp = Integer.parseInt(p[0]);
                            mesExp  = Integer.parseInt(p[1]);
                        }
                    } else {
                        // compat: si aún llegan campos separados
                        mesExp  = Integer.parseInt(nv(req.getParameter("mes_exp"), "0"));
                        anioExp = Integer.parseInt(nv(req.getParameter("anio_exp"), "0"));
                    }

                    String cvv = req.getParameter("cvv"); // no persistir

                    int pagoId = service.registrarPago(user, tipoId, observ, metodoId, guardarNueva,
                            numero, marca, titular, mesExp, anioExp, cvv);

                    session.setAttribute("flash_ok", "Pago registrado con éxito");
                    resp.sendRedirect(req.getContextPath() + "/residente/pagos?action=recibo&id=" + pagoId);
                    return;
                }
                
                case "recibo": {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Pago p = service.obtener(id);
                    if (p == null || p.getUsuarioId() != user.getId()) {
                        throw new IllegalArgumentException("Recibo no disponible.");
                    }
                    req.setAttribute("pago", p);

                    // traer datos de la tarjeta si existe
                    if (p.getMetodoPagoId() != null) {
                        req.setAttribute("metodoPago", service.obtenerMetodo(p.getMetodoPagoId()));
                    }

                    req.getRequestDispatcher("/view/residente/pago-recibo.jsp").forward(req, resp);
                    break;
                }

                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no soportada");
            }
        } catch (Exception e) {
            session.setAttribute("flash_err", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/residente/pagos?action=listar");
        }
    }

    private void validarSesion(Usuario u) {
        if (u == null || u.getId() <= 0)
            throw new IllegalArgumentException("Sesión no válida.");
    }

    private static String nv(String s, String d) {
        return (s == null || s.isEmpty()) ? d : s;
    }
}
