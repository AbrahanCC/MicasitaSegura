package dao;

import model.Visitante;
import java.util.List;

public interface VisitanteDAO {

    boolean crear(Visitante v);
    List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi);
    Visitante obtener(int id);


    Visitante obtenerPaseVigentePorToken(String token); 
    boolean marcarConsumidoPorToken(String token);      

   
    boolean aprobar(int idVisitante, Integer modificadoPor);
    boolean rechazar(int idVisitante, Integer modificadoPor);
}
