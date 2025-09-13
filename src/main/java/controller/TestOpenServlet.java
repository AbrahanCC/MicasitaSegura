package controller;

import service.GateService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

public class TestOpenServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    boolean ok = new GateService().abrir();
    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write("{\"open\":" + ok + "}");
  }
}
