package controller;
//vivsitante dos usos y residente uso limitado
import util.QRUtil;
import util.TokenUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;

/** Sirve el QR del usuario autenticado (op=me).
 *  /qr?op=me              -> muestra PNG
 *  /qr?op=me&download=1   -> fuerza descarga */
@WebServlet("/qr")
public class QrServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String op = req.getParameter("op");
        if (!"me".equalsIgnoreCase(op)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "op inválido");
            return;
        }

        // Sesión y rol
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("uid") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer rol = (Integer) s.getAttribute("rol"); // 1=Admin, 2=Residente
        if (rol == null || (rol != 1 && rol != 2)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int uid = (Integer) s.getAttribute("uid");

        // Mismo token que se usa en el correo
        String token = TokenUtil.generateResidentToken(uid);

        // URL que valida el QR
        String base = req.getScheme() + "://" + req.getServerName()
                + (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : (":" + req.getServerPort()))
                + req.getContextPath();
        String url = base + "/api/validate?token=" + URLEncoder.encode(token, "UTF-8");

        try {
            byte[] png = QRUtil.makeQRPng(url, 400);

            // ¿Descarga?
            boolean download = "1".equals(req.getParameter("download"));
            if (download) {
                resp.setHeader("Content-Disposition", "attachment; filename=\"mi-qr.png\"");
            }
            resp.setContentType("image/png");
            resp.setContentLength(png.length);
            resp.getOutputStream().write(png);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo generar el QR");
        }
    }
}
