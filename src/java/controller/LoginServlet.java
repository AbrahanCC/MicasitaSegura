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
            if (rol != null) {
                switch (rol) {
                    case 1: resp.sendRedirect(req.getContextPath() + "/view/admin/dashboard.jsp"); return;
                    case 2: resp.sendRedirect(req.getContextPath() + "/view/residente/qr.jsp");   return;
                    case 3: resp.sendRedirect(req.getContextPath() + "/view/guardia/control.jsp"); return;
                }
            }
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }
        req.getRequestDispatcher("/view/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ident = firstNonEmpty(
                req.getParameter("user"),
                req.getParameter("correo"),
                req.getParameter("email"),
                req.getParameter("username")
        );
        String password = firstNonEmpty(
                req.getParameter("pass"),
                req.getParameter("password"),
                req.getParameter("clave")
        );

        Usuario u = (ident == null) ? null : usuarioDAO.buscarPorIdentificador(ident);

        boolean ok = (u != null && u.isActivo() && u.getPassHash() != null &&
                      PasswordUtil.verify(password, u.getPassHash()));

        if (ok) {
            if (u.getPassHash().matches("^[0-9a-fA-F]{64}$")) {
                String newHash = PasswordUtil.hash(password);
                usuarioDAO.actualizarPassword(u.getId(), newHash);
                u.setPassHash(newHash);
            }

            HttpSession s = req.getSession(true);
            s.setAttribute("uid", u.getId());
            s.setAttribute("uname", u.getNombre());
            s.setAttribute("rol", u.getRolId());

            switch (u.getRolId()) {
                case 1: resp.sendRedirect(req.getContextPath() + "/view/admin/dashboard.jsp"); return;
                case 2: resp.sendRedirect(req.getContextPath() + "/view/residente/qr.jsp");   return;
                case 3: resp.sendRedirect(req.getContextPath() + "/view/guardia/control.jsp"); return;
                default: resp.sendRedirect(req.getContextPath() + "/index.jsp"); return;
            }
        } else {
            req.setAttribute("error", "Credenciales inválidas o usuario inactivo.");
            req.setAttribute("correo", ident);
            req.getRequestDispatcher("/view/login.jsp").forward(req, resp);
        }
    }
}
