package controller;

import util.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/pingdb")
public class PingDbServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain; charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            DBConnection db = new DBConnection();   
            try (Connection cn = db.getConnection()) {  
                if (cn != null && !cn.isClosed()) {
                    out.println("Conexión exitosa a la base de datos ✅");
                } else {
                    out.println("No se pudo abrir conexión ❌");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                e.printStackTrace(out);
            }
        }
    }
}
