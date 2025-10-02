package controller;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Usuario;
import util.PasswordUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    private static String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null) {
                v = v.trim();
                if (!v.isEmpty()) return v;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("uid") != null) {
            Integer rol = (Integer) s.getAttribute("rol");
            String ctx = req.getContextPath();
            if (rol != null) {
                switch (rol) {
                    case 1: resp.sendRedirect(ctx + "/view/admin/dashboard.jsp");  return;
                    case 2: resp.sendRedirect(ctx + "/view/residente/qr.jsp");     return;
                    case 3: resp.sendRedirect(ctx + "/visitantes?op=new");          return;
                }
            }
            resp.sendRedirect(ctx + "/index.jsp");
            return;
        }
        req.getRequestDispatcher("/view/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ident = firstNonEmpty(
                req.getParameter("user"), req.getParameter("correo"),
                req.getParameter("email"), req.getParameter("username")
        );
        String password = firstNonEmpty(
                req.getParameter("pass"), req.getParameter("password"),
                req.getParameter("clave"), req.getParameter("contrasena")
        );

        // --- DEBUG: qué llega del form ---
        System.out.println("DEBUG LOGIN ----");
        System.out.println("ident=" + ident);
        System.out.println("password=" + password);

        Usuario u = (ident == null) ? null : usuarioDAO.buscarPorIdentificador(ident);
        System.out.println("u!=null=" + (u != null));
        if (u != null) {
            System.out.println("user=" + u.getUsername() + " activo=" + u.isActivo());
            System.out.println("hash=" + u.getPassHash());
            System.out.println("verify(input)=" + PasswordUtil.verify(password, u.getPassHash()));
            // Prueba dura: comprobar hash contra "123" directamente
            System.out.println("verify('123')=" + PasswordUtil.verify("123", u.getPassHash()));
        }
        System.out.println("---------------");

        // ----- MODO PRUEBA (TEMPORAL) -----
        // Para aislar el problema del autocompletado: si el usuario es "admin",
        // ignora lo que llegó y valida directamente contra "123".
        boolean ok;
        if (u != null && "admin".equalsIgnoreCase(ident)) {
            ok = (u.isActivo() && u.getPassHash() != null && PasswordUtil.verify("123", u.getPassHash()));
        } else {
            ok = (u != null && u.isActivo() && u.getPassHash() != null && PasswordUtil.verify(password, u.getPassHash()));
        }
        // ----- FIN MODO PRUEBA -----

        if (!ok) {
            req.setAttribute("error", "Credenciales inválidas o usuario inactivo.");
            req.setAttribute("correo", ident);
            req.getRequestDispatcher("/view/login.jsp").forward(req, resp);
            return;
        }

        // Rehash si viniera SHA-256 (raro ya)
        if (u.getPassHash().matches("^[0-9a-fA-F]{64}$")) {
            String newHash = PasswordUtil.hash(password);
            usuarioDAO.actualizarPassword(u.getId(), newHash);
            u.setPassHash(newHash);
        }

        HttpSession s = req.getSession(true);
        s.setAttribute("uid", u.getId());
        s.setAttribute("uname", u.getNombre());
        s.setAttribute("rol", u.getRolId());

        String ctx = req.getContextPath();
        switch (u.getRolId()) {
            case 1: resp.sendRedirect(ctx + "/view/admin/dashboard.jsp");  return;
            case 2: resp.sendRedirect(ctx + "/view/residente/qr.jsp");     return;
            case 3: resp.sendRedirect(ctx + "/visitantes?op=new");          return;
            default: resp.sendRedirect(ctx + "/index.jsp");                return;
        }
    }
}
