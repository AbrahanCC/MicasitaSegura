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
import java.util.List;

@WebServlet("/usuarios")
public class UsuarioController extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final RolDAO rolDAO = new RolDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String op = req.getParameter("op");
        if (op == null) op = "list";

        switch (op) {
            case "new":
                req.setAttribute("roles", rolDAO.listar());
                req.setAttribute("u", new Usuario());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                break;

            case "edit":
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("u", usuarioDAO.obtener(id));
                req.setAttribute("roles", rolDAO.listar());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                break;

            case "del":
                usuarioDAO.eliminar(Integer.parseInt(req.getParameter("id")));
                resp.sendRedirect(req.getContextPath() + "/usuarios");
                break;

            default:
                List<Usuario> data = usuarioDAO.listar();
                req.setAttribute("data", data);
                req.getRequestDispatcher("/view/usuario-lista.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String idStr      = req.getParameter("id");
        String dpi        = req.getParameter("dpi");
        String nombre     = req.getParameter("nombre");
        String apellidos  = req.getParameter("apellidos");
        String correo     = req.getParameter("correo");
        String numeroCasa = req.getParameter("numeroCasa");
        String username   = req.getParameter("username");
        String rolId      = req.getParameter("rolId");
        String activo     = req.getParameter("activo");
        String pass       = req.getParameter("pass");

        if (!Validador.noVacio(dpi) || !Validador.noVacio(nombre) || !Validador.noVacio(apellidos) ||
            !Validador.noVacio(correo) || !Validador.noVacio(username) || !Validador.noVacio(rolId)) {

            req.setAttribute("error", "Campos requeridos: DPI, Nombre, Apellidos, Correo, Usuario y Rol.");
            Usuario tmp = new Usuario();
            if (Validador.noVacio(idStr)) tmp.setId(Integer.parseInt(idStr));
            tmp.setDpi(dpi);
            tmp.setNombre(nombre);
            tmp.setApellidos(apellidos);
            tmp.setCorreo(correo);
            tmp.setNumeroCasa(numeroCasa);
            tmp.setUsername(username);
            tmp.setRolId(Validador.noVacio(rolId) ? Integer.parseInt(rolId) : 0);
            tmp.setActivo("on".equalsIgnoreCase(activo) || "1".equals(activo));
            req.setAttribute("u", tmp);
            req.setAttribute("roles", rolDAO.listar());
            req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
            return;
        }

        Usuario u = new Usuario();
        if (Validador.noVacio(idStr)) u.setId(Integer.parseInt(idStr));
        u.setDpi(dpi);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setNumeroCasa(numeroCasa);
        u.setUsername(username);
        u.setRolId(Integer.parseInt(rolId));
        u.setActivo("on".equalsIgnoreCase(activo) || "1".equals(activo));

        try {
            if (u.getId() > 0) {
                if (Validador.noVacio(pass)) {
                    u.setPassHash(PasswordUtil.hash(pass));
                } else {
                    u.setPassHash(null);
                }
                usuarioDAO.actualizar(u);
            } else {
                if (!Validador.noVacio(pass)) {
                    req.setAttribute("error", "La contraseña es obligatoria para crear un usuario.");
                    req.setAttribute("u", u);
                    req.setAttribute("roles", rolDAO.listar());
                    req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                    return;
                }
                u.setPassHash(PasswordUtil.hash(pass));
                usuarioDAO.crear(u);
            }
            resp.sendRedirect(req.getContextPath() + "/usuarios");

        } catch (Exception ex) {
            String dup = mensajeDuplicado(ex);
            if (dup != null) {
                req.setAttribute("error", dup);
                req.setAttribute("u", u);
                req.setAttribute("roles", rolDAO.listar());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
            } else {
                throw new ServletException(ex);
            }
        }
    }

  
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
