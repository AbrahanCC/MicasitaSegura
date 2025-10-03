package dao;

import java.util.List;
import model.Conversacion;

public interface ConversacionDAO {
    boolean existsActiva(int idResidente, int idGuardia);
    int countActivasPorGuardia(int idGuardia);
    Conversacion create(Conversacion c);        // devuelve con ID
    Conversacion findById(int id);
    List<Conversacion> findActivasByUsuario(int idUsuario);
    void updateFechaUltimoMensaje(int idConversacion);
    void cerrar(int idConversacion);
}
