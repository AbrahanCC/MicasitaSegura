package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ComunicacionServlet", urlPatterns = {"/comunicacion"})
public class ComunicacionServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // // muestra el menú de CU6 (Consulta General / Reportar incidente)
    req.getRequestDispatcher("/CU6/comunicacion.jsp").forward(req, resp);
  }
}
