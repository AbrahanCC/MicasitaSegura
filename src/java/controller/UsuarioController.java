package controller;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import dao.RolDAO;
import dao.RolDAOImpl;
import model.Usuario;
import util.PasswordUtil;
import service.Validador;

import javax.servlet.annotation.WebServlet;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

// CRUD de usuarios (lista/nuevo/editar/eliminar)
@WebServlet("/usuarios")
public class UsuarioController extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final RolDAO rolDAO = new RolDAOImpl();

    // Catálogo: casas 1..50
    private List<String> catalogoCasas() {
        List<String> casas = new ArrayList<>();
        for (int i = 1; i <= 50; i++) casas.add(String.valueOf(i));
        return casas;
    }

    // Catálogo: lotes A..Z
    private List<String> catalogoLotes() {
        List<String> lotes = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) lotes.add(String.valueOf(c));
        return lotes;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String op = req.getParameter("op");
        if (op == null) op = "list";

        switch (op) {
            case "new":
                req.setAttribute("roles", rolDAO.listar());
                req.setAttribute("u", new Usuario());
                req.setAttribute("casas", catalogoCasas());
                req.setAttribute("lotes", catalogoLotes());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                break;

            case "edit":
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("u", usuarioDAO.obtener(id));
                req.setAttribute("roles", rolDAO.listar());
                req.setAttribute("casas", catalogoCasas());
                req.setAttribute("lotes", catalogoLotes());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                break;

            case "del":
                usuarioDAO.eliminar(Integer.parseInt(req.getParameter("id")));
                resp.sendRedirect(req.getContextPath() + "/usuarios");
                break;

            default:
                req.setAttribute("data", usuarioDAO.listar());
                req.getRequestDispatcher("/view/usuario-lista.jsp").forward(req, resp);
        }
    }
    
    //Opcion guardar usuario cuando esten llenos los campos 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Params
        String idStr      = req.getParameter("id");
        String dpi        = req.getParameter("dpi");
        String nombre     = req.getParameter("nombre");
        String apellidos  = req.getParameter("apellidos");
        String correo     = req.getParameter("correo");
        String numeroCasa = req.getParameter("numeroCasa");
        String lote       = req.getParameter("lote");
        String username   = req.getParameter("username");
        String rolId      = req.getParameter("rolId");
        String activo     = req.getParameter("activo");
        String pass       = req.getParameter("pass");

        // Trim básicos
        if (dpi != null) dpi = dpi.trim();
        if (correo != null) correo = correo.trim();
        if (username != null) username = username.trim();

        int rolInt = Validador.noVacio(rolId) ? Integer.parseInt(rolId) : 0;
        boolean esGuardia = (rolInt == 3); // 1=ADMIN, 2=RESIDENTE, 3=GUARDIA

        // RN1: si es GUARDIA, forzar NULL en lote/numeroCasa
        if (esGuardia) {
            numeroCasa = null;
            lote = null;
        }

        // RN2: requeridos mínimos + (lote/numeroCasa) SOLO si no es GUARDIA
        boolean requeridosOk =
                Validador.noVacio(dpi) &&
                Validador.noVacio(nombre) &&
                Validador.noVacio(apellidos) &&
                Validador.noVacio(correo) &&
                Validador.noVacio(username) &&
                rolInt > 0 &&
                (esGuardia || (Validador.noVacio(numeroCasa) && Validador.noVacio(lote)));

        if (!requeridosOk) {
            req.setAttribute("error", esGuardia
                    ? "Campos requeridos: DPI, Nombre, Apellidos, Correo, Usuario y Rol."
                    : "Campos requeridos: DPI, Nombre, Apellidos, Correo, Usuario, Rol, Lote y Número de casa.");
            Usuario tmp = new Usuario();
            if (Validador.noVacio(idStr)) tmp.setId(Integer.parseInt(idStr));
            tmp.setDpi(dpi);
            tmp.setNombre(nombre);
            tmp.setApellidos(apellidos);
            tmp.setCorreo(correo);
            tmp.setNumeroCasa(numeroCasa);
            tmp.setLote(lote);
            tmp.setUsername(username);
            tmp.setRolId(rolInt);
            tmp.setActivo("on".equalsIgnoreCase(activo) || "1".equals(activo));
            req.setAttribute("u", tmp);
            req.setAttribute("roles", rolDAO.listar());
            req.setAttribute("casas", catalogoCasas());
            req.setAttribute("lotes", catalogoLotes());
            req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
            return;
        }

        // Construcción
        Usuario u = new Usuario();
        if (Validador.noVacio(idStr)) u.setId(Integer.parseInt(idStr));
        u.setDpi(dpi);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setNumeroCasa(numeroCasa); // puede ir null (GUARDIA)
        u.setLote(lote);             // puede ir null (GUARDIA)
        u.setUsername(username);
        u.setRolId(rolInt);
        u.setActivo("on".equalsIgnoreCase(activo) || "1".equals(activo));

        try {
            if (u.getId() > 0) { // actualizar
                if (Validador.noVacio(pass)) {
                    u.setPassHash(PasswordUtil.hash(pass));
                } else {
                    u.setPassHash(null);
                }
                usuarioDAO.actualizar(u);
            } else { // crear campos de casa y loes completos 
                if (!Validador.noVacio(pass)) {
                    req.setAttribute("error", "La contraseña es obligatoria para crear un usuario.");
                    req.setAttribute("u", u);
                    req.setAttribute("roles", rolDAO.listar());
                    req.setAttribute("casas", catalogoCasas());
                    req.setAttribute("lotes", catalogoLotes());
                    req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                    return;
                }
                u.setPassHash(PasswordUtil.hash(pass));
                usuarioDAO.crear(u); // RN3 se aplica dentro del DAO (solo RESIDENTE)
            }
            resp.sendRedirect(req.getContextPath() + "/usuarios");

        } catch (Exception ex) {
            String dup = mensajeDuplicado(ex);
            if (dup != null) {
                req.setAttribute("error", dup);
                req.setAttribute("u", u);
                req.setAttribute("roles", rolDAO.listar());
                req.setAttribute("casas", catalogoCasas());
                req.setAttribute("lotes", catalogoLotes());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
            } else {
                throw new ServletException(ex);
            }
        }
    }

    // Duplicados3
    private String mensajeDuplicado(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof java.sql.SQLIntegrityConstraintViolationException) {
                String msg = t.getMessage();
                if (msg != null) return mapearDuplicado(msg);
            }
            String msg = t.getMessage();
            if (msg != null && msg.contains("Duplicate entry")) {
                return mapearDuplicado(msg);
            }
            t = t.getCause();
        }
        return null;
    }

    private String mapearDuplicado(String msg) {
        String s = msg.toLowerCase();
        if (s.contains("correo")   || s.contains("uq_usuarios_correo"))    return "El correo ya está registrado.";
        if (s.contains("username") || s.contains("uq_usuarios_username"))  return "El nombre de usuario ya existe.";
        if (s.contains("dpi")      || s.contains("uq_usuarios_dpi"))       return "El DPI ya está registrado.";
        return "Ya existe un registro con esos datos.";
    }
}
