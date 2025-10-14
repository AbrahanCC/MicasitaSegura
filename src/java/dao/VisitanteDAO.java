package dao;

import model.Visitante;
import java.util.List;

public interface VisitanteDAO {

    boolean crear(Visitante v);
    List<Visitante> listar(String desde, String hasta, String destinoNumeroCasa, String dpi);
    Visitante obtener(int id);

    // Obtiene el pase vigente según el token QR
    Visitante obtenerPaseVigentePorToken(String token);

    // Marca el pase como consumido según el token
    boolean marcarConsumidoPorToken(String token);

    // Aprueba el acceso de un visitante
    boolean aprobar(int idVisitante, Integer modificadoPor);

    // Rechaza el acceso de un visitante
    boolean rechazar(int idVisitante, Integer modificadoPor);
}
