package dao;

import java.util.List;
import model.MetodoPago;

public interface MetodoPagoDAO {
    List<MetodoPago> listarActivosPorUsuario(int usuarioId);
    int crear(MetodoPago mp);
    MetodoPago obtener(int id);
}
