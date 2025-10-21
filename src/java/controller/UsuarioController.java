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

// CRUD de usuarios (lista/nuevo/editar/eliminar)
@WebServlet("/usuarios")
public class UsuarioController extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final RolDAO rolDAO = new RolDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String op = req.getParameter("op");
        if (op == null) op = "list";

        switch (op) {
            //FA1 formulario de creación con catálogos RN1 y roles RN4
            case "new":
                req.setAttribute("roles", rolDAO.listar());
                req.setAttribute("u", new Usuario());
                req.setAttribute("casas", usuarioDAO.catalogoCasas());
                req.setAttribute("lotes", usuarioDAO.catalogoLotes());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                break;

            //FA1 edición muestra formulario con datos existentes
            case "edit":
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("u", usuarioDAO.obtener(id));
                req.setAttribute("roles", rolDAO.listar());
                req.setAttribute("casas", usuarioDAO.catalogoCasas());
                req.setAttribute("lotes", usuarioDAO.catalogoLotes());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                break;
                
            //FA2 eliminar y redirigir a listado
            case "del":
                usuarioDAO.eliminar(Integer.parseInt(req.getParameter("id")));
                resp.sendRedirect(req.getContextPath() + "/usuarios");
                break;

            default:
                // Flujo básico: mostrar lista
                req.setAttribute("data", usuarioDAO.listar());
                req.getRequestDispatcher("/view/usuario-lista.jsp").forward(req, resp);
        }
    }

    // Guardar usuario (crear/actualizar)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Params
        String idStr      = req.getParameter("id");
        String dpi        = trim(req.getParameter("dpi"));
        String nombre     = trim(req.getParameter("nombre"));
        String apellidos  = trim(req.getParameter("apellidos"));
        String correo     = trim(req.getParameter("correo"));
        // ACEPTA AMBOS NOMBRES: numeroCasa o casa
        String numeroCasa = firstNonEmpty(trim(req.getParameter("numeroCasa")), trim(req.getParameter("casa")));
        String lote       = trim(req.getParameter("lote"));
        String username   = trim(req.getParameter("username"));
        String rolId      = trim(req.getParameter("rolId"));
        String activo     = req.getParameter("activo");
        String pass       = req.getParameter("pass");

        int rolInt = Validador.noVacio(rolId) ? Integer.parseInt(rolId) : 0;
        boolean esGuardia = (rolInt == 2); // 1=ADMIN, 2=GUARDIA, 3=RESIDENTE

        // RN1: si es GUARDIA, lote/numeroCasa no aplican
        if (esGuardia) {
            numeroCasa = null;
            lote = null;
        }

        // RN2: requeridos mínimos si NO es guardia, exige lote + número de casa
        boolean requeridosOk =
                Validador.noVacio(dpi) &&
                Validador.noVacio(nombre) &&
                Validador.noVacio(apellidos) &&
                Validador.noVacio(correo) &&
                Validador.noVacio(username) &&
                rolInt > 0 &&
                (esGuardia || (Validador.noVacio(numeroCasa) && Validador.noVacio(lote)));

        // FA1 paso 5 si falta algo, retorna al formulario con mensaje.
        if (!requeridosOk) {
            req.setAttribute("error", esGuardia
                    ? "Campos requeridos: DPI, Nombre, Apellidos, Correo, Usuario y Rol."
                    : "Campos requeridos: DPI, Nombre, Apellidos, Correo, Usuario, Rol, Lote y Número de casa.");

            // Reenvía modelo parcial para no perder datos ingresados.
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
            req.setAttribute("casas", usuarioDAO.catalogoCasas());
            req.setAttribute("lotes", usuarioDAO.catalogoLotes());
            req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
            return;
        }

        // Construcción modelo
        Usuario u = new Usuario();
        if (Validador.noVacio(idStr)) u.setId(Integer.parseInt(idStr));
        u.setDpi(dpi);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setNumeroCasa(numeroCasa); // puede ser null (GUARDIA)
        u.setLote(lote);             // puede ser null (GUARDIA)
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

            //Mensaje al actualizar
            HttpSession session = req.getSession();
            session.setAttribute("flashOk", "Usuario actualizado correctamente.");
                } else { // crear
                // RN2: contraseña obligatoria al crear
                if (!Validador.noVacio(pass)) {
                    req.setAttribute("error", "La contraseña es obligatoria para crear un usuario.");
                    req.setAttribute("u", u);
                    req.setAttribute("roles", rolDAO.listar());
                    req.setAttribute("casas", usuarioDAO.catalogoCasas());
                    req.setAttribute("lotes", usuarioDAO.catalogoLotes());
                    req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
                    return;
                }
                u.setPassHash(PasswordUtil.hash(pass));
                usuarioDAO.crear(u);//RN3 QR, envio ocurre en DAO tras insertar si es residente
                        //Mensaje de exito al crear
                        HttpSession session = req.getSession();
                        session.setAttribute("flashOk", "Usuario creado correctamente.");
            }
            //Retorno al flujo basico o la lista
            resp.sendRedirect(req.getContextPath() + "/usuarios");

        } catch (Exception ex) {
            // FA3: manejo de duplicados (DPI/correo/username).
            String dup = mensajeDuplicado(ex);
            if (dup != null) {
                req.setAttribute("error", dup);
                req.setAttribute("u", u);
                req.setAttribute("roles", rolDAO.listar());
                req.setAttribute("casas", usuarioDAO.catalogoCasas());
                req.setAttribute("lotes", usuarioDAO.catalogoLotes());
                req.getRequestDispatcher("/view/usuario-form.jsp").forward(req, resp);
            } else {
                throw new ServletException(ex);
            }
        }
    }

    //Helpers
    private static String trim(String s){ return s==null?null:s.trim(); }
    private static String firstNonEmpty(String... vs){
        if (vs!=null) for (String v:vs){ if (v!=null && !v.isEmpty()) return v; }
        return null;
    }

    // excepciones SQL de unicidad, mensajes de duplicados FA3
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
    
    //Traduce constraint a texto de FA3 (DPI/correo/username). */
    private String mapearDuplicado(String msg) {
        String s = msg.toLowerCase();
        if (s.contains("correo")   || s.contains("uq_usuarios_correo"))    return "El correo ya está registrado.";
        if (s.contains("username") || s.contains("uq_usuarios_username"))  return "El nombre de usuario ya existe.";
        if (s.contains("dpi")      || s.contains("uq_usuarios_dpi"))       return "El DPI ya está registrado.";
        return "Ya existe un registro con esos datos.";
    }
}
