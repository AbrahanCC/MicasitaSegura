package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns = {
        "/view/admin/*",
        "/view/residente/*",
        "/view/guardia/*",
        "/usuarios/*",
        "/qr"
})
public class AuthFilter implements Filter {

    @Override public void init(FilterConfig filterConfig) { }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain c) throws IOException, ServletException {
        HttpServletRequest req  = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        // Rutas libres (login, logout y recursos estáticos)
        if (uri.equals(ctx + "/") ||
            uri.endsWith("/view/login.jsp") || uri.endsWith("/login") || uri.endsWith("/logout") ||
            uri.contains("/assets/") || uri.contains("/img/") || uri.contains("/css/") || uri.contains("/js/") ||
            uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg") ||
            uri.endsWith(".gif") || uri.endsWith(".svg") || uri.endsWith(".webp") ||
            uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf")) {
            c.doFilter(rq, rs);
            return;
        }

        // Precondición sesión activa obligatoria
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("uid") == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }

        Integer rol = (Integer) s.getAttribute("rol"); // 1=ADMIN, 2=RESIDENTE, 3=GUARDIA

        // Precondición: acceso permitido solo a Residente o Guardia
        if (uri.startsWith(ctx + "/view/residente/") || uri.equals(ctx + "/directorio")) {
            if (rol == null || (rol != 2 && rol != 3 && rol != 1)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // Otras rutas protegidas (Admin, Guardia, etc.)
        if (uri.startsWith(ctx + "/view/admin/")) {
            if (rol == null || rol != 1) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        } else if (uri.startsWith(ctx + "/view/guardia/")) {
            if (rol == null || (rol != 3 && rol != 1)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        } else if (uri.startsWith(ctx + "/usuarios/")) {
            if (rol == null || rol != 1) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        } else if (uri.equals(ctx + "/qr")) { 
            if (rol == null || (rol != 1 && rol != 2)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        }

        c.doFilter(rq, rs);
    }

    @Override public void destroy() { }
}
