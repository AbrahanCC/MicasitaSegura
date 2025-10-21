package dao;

import java.util.List;
import model.Mensaje;

public interface MensajeDAO {
    // Crea un mensaje
    Mensaje create(Mensaje m);

    // Lista mensajes de una conversación (paginado simple)
    List<Mensaje> findByConversacion(int idConversacion, int limit, int offset);

    // Marca como leídos todos los mensajes de la conversación enviados por "el otro" usuario
    void marcarLeidos(int idConversacion, int userId);
}
