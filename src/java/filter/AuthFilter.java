package filter;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*") // asegura que el filtro aplique a todo
public class AuthFilter implements Filter {

  @Override public void init(FilterConfig filterConfig) { }

  @Override
  public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain c) throws IOException, ServletException {
    HttpServletRequest req  = (HttpServletRequest) rq;
    HttpServletResponse resp = (HttpServletResponse) rs;

    String ctx = req.getContextPath();
    String uri = req.getRequestURI();

    boolean isStatic = uri.contains("/assets/") || uri.contains("/img/") || uri.contains("/css/") || uri.contains("/js/")
        || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
        || uri.endsWith(".gif") || uri.endsWith(".svg") || uri.endsWith(".webp")
        || uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf");

    if (uri.equals(ctx + "/") ||
        uri.endsWith("/view/login.jsp") || uri.endsWith("/login") || uri.endsWith("/logout") ||
        isStatic) {
      c.doFilter(rq, rs);
      return;
    }

    HttpSession s = req.getSession(false);
    if (s == null || s.getAttribute("uid") == null) {
      resp.sendRedirect(ctx + "/login");
      return;
    }

    Integer rol = (Integer) s.getAttribute("rol");

    if (uri.startsWith(ctx + "/view/admin/")) {
      if (rol == null || rol != 1) { resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso solo para administradores"); return; }
    }
    else if (uri.startsWith(ctx + "/view/guardia/")) {
      if (rol == null || (rol != 2 && rol != 1)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso solo para guardias"); return; }
    }
    else if (uri.startsWith(ctx + "/view/residente/")) {
      if (rol == null || rol != 3) { resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso solo para residentes"); return; }
    }
    else if (uri.startsWith(ctx + "/usuarios")) {
      if (rol == null || rol != 1) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
    }
    else if (uri.equals(ctx + "/qr")) {
      if (rol == null || (rol != 1 && rol != 2 && rol != 3)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
    }
    else if (uri.startsWith(ctx + "/CU6/")) {
      if (rol == null || (rol != 1 && rol != 2 && rol != 3)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
    }

    c.doFilter(rq, rs);
  }

  @Override public void destroy() { }
}
