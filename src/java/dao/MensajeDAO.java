package dao;

import java.util.List;
import model.Mensaje;

public interface MensajeDAO {
    Mensaje create(Mensaje m);
    List<Mensaje> findByConversacion(int idConversacion, int limit, int offset);
}
