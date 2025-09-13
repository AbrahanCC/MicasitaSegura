package dao;

import model.Visitante;
import java.util.List;

public interface VisitanteDAO {
    boolean crear(Visitante v);
    List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi);

    // Para pases (nuevos):
    Visitante obtenerPaseVigentePorToken(String token); // estado='emitido' y no caducado
    boolean marcarConsumidoPorToken(String token);      // set estado='consumido'
}