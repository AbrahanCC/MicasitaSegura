package service;

import dao.ConversacionDAO;
import dao.ConversacionDAOImpl;
import model.Conversacion;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversacionService {
    private final ConversacionDAO conversacionDAO = new ConversacionDAOImpl();

    public List<UsuarioMin> listarGuardiasActivos() {
        String sql = "SELECT u.id, u.nombre, u.correo FROM usuarios u " +
                     "JOIN roles r ON u.rol_id=r.id " +
                     "WHERE u.activo=1 AND LOWER(r.nombre) IN ('guardia','agente')";

        List<UsuarioMin> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnectionStatic();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new UsuarioMin(rs.getInt("id"), rs.getString("nombre"), rs.getString("correo")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    public Conversacion crearConversacion(int idResidente, int idGuardia) {
        int activas = conversacionDAO.countActivasPorGuardia(idGuardia);
        if (activas >= 4) throw new RuntimeException("Este guardia ya tiene 4 conversaciones activas");
        if (conversacionDAO.existsActiva(idResidente, idGuardia))
            throw new RuntimeException("Ya existe una conversación con el usuario seleccionado");

        Conversacion c = new Conversacion(idResidente, idGuardia);
        return conversacionDAO.create(c);
    }

    public List<Conversacion> listarActivasPorUsuario(int idUsuario) {
        return conversacionDAO.findActivasByUsuario(idUsuario);
    }

    public Conversacion obtener(int idConversacion) { return conversacionDAO.findById(idConversacion); }
    public void tocarUltimoMensaje(int idConversacion) { conversacionDAO.updateFechaUltimoMensaje(idConversacion); }
    public void cerrar(int idConversacion) { conversacionDAO.cerrar(idConversacion); }

    public static class UsuarioMin {
        public final int id; public final String nombre; public final String correo;
        public UsuarioMin(int id, String nombre, String correo) { this.id = id; this.nombre = nombre; this.correo = correo; }
    }
}
