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

    // Lista guardias activos usando rol_id = 2 (según tu tabla 'roles')
    public List<UsuarioMin> listarGuardiasActivos() {
        String sql = "SELECT u.id, u.nombre, u.correo " +
                     "FROM usuarios u " +
                     "WHERE u.activo = 1 AND u.rol_id = 2 " +
                     "ORDER BY u.nombre ASC";
        List<UsuarioMin> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new UsuarioMin(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("correo")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public void cerrarConversacion(int idConversacion, int solicitanteId) {
        Conversacion c = obtener(idConversacion);
        if (c == null) throw new RuntimeException("La conversación no existe.");
        if (solicitanteId != c.getIdResidente() && solicitanteId != c.getIdGuardia())
            throw new RuntimeException("No tienes permisos para cerrar esta conversación.");
        cerrar(idConversacion);
    }

    // DTO mínimo para el combo de guardias
    public static class UsuarioMin {
        public final int id;
        public final String nombre;
        public final String correo;
        public UsuarioMin(int id, String nombre, String correo) {
            this.id = id; this.nombre = nombre; this.correo = correo;
        }
    }
}
