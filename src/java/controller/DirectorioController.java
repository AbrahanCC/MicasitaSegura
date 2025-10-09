package controller;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/directorio")
public class DirectorioController extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Acceso a opción Directorio Residencial
        String op = req.getParameter("op");

        //Limpiar formulario
        if ("limpiar".equalsIgnoreCase(op)) {
            req.setAttribute("msg", "Formulario limpio.");
            req.getRequestDispatcher("/view/residente/directorio.jsp").forward(req, resp);
            return;
        }

        //Captura de parámetros opcionales
        String nombres    = p(req, "nombres");
        String apellidos  = p(req, "apellidos");
        String lote       = p(req, "lote");
        String numeroCasa = p(req, "numeroCasa");

        boolean loteVacio = lote.isEmpty();
        boolean numVacio  = numeroCasa.isEmpty();

        //Validación número de casa incompleto
        if (loteVacio ^ numVacio) {
            req.setAttribute("error", "Por favor, debe seleccionar un lote y un número de casa o ninguno de los dos.");
            req.getRequestDispatcher("/view/residente/directorio.jsp").forward(req, resp);
            return;
        }

        //Usuario presiona “Buscar”
        boolean hayFiltros = !nombres.isEmpty() || !apellidos.isEmpty() || (!loteVacio && !numVacio);

        // Búsqueda en DAO (FA2 posible)
        if (hayFiltros) {
            List<Usuario> lista = usuarioDAO.buscarDirectorio(nombres, apellidos, lote, numeroCasa);
            if (lista.isEmpty()) req.setAttribute("msg", "No se encontró ningún usuario con los datos ingresados."); // CU21-FA2
            req.setAttribute("lista", lista);
        }

        //Muestra información del residente
        req.getRequestDispatcher("/view/residente/directorio.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private String p(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }
}
