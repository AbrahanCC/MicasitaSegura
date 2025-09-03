package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns = {"/view/guardia/*"})
public class GuardiaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain c) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;

        HttpSession s = req.getSession(false);
        Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");

        if (rol == null || rol != 3) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso solo para guardias");
            return;
        }

        c.doFilter(rq, rs);
    }

    @Override
    public void destroy() { }
}
