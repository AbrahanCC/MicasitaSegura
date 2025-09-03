package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns = {
        "/view/admin/*",
        "/view/residente/*",
        "/view/guardia/*",
        "/usuarios/*"
})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain c) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        if (uri.endsWith("/view/login.jsp") || uri.endsWith("/login") || uri.endsWith("/logout")
                || uri.contains("/assets/") || uri.contains("/img/") || uri.contains("/css/")
                || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg") || uri.endsWith(".gif") || uri.endsWith(".svg")
                || uri.endsWith(".webp") || uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf")) {
            c.doFilter(rq, rs);
            return;
        }

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("uid") == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }

        Integer rol = (Integer) s.getAttribute("rol");
        if (uri.startsWith(ctx + "/view/admin/") && (rol == null || rol != 1)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (uri.startsWith(ctx + "/view/residente/") && (rol == null || rol != 2)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (uri.startsWith(ctx + "/view/guardia/") && (rol == null || rol != 3)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        c.doFilter(rq, rs);
    }

    @Override
    public void destroy() { }
}
