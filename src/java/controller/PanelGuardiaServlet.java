package controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;

@WebServlet(name="PanelGuardiaServlet", urlPatterns={"/guardia/panel"})
public class PanelGuardiaServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession s = req.getSession(false);
    Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
    if (rol == null || rol != 3) { // 3 = GUARDIA
      resp.sendRedirect(req.getContextPath() + "/login");
      return;
    }
    req.getRequestDispatcher("/view/guardia/control.jsp").forward(req, resp);
  }
}
