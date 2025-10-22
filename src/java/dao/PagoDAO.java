package dao;

import java.time.LocalDate;
import java.util.List;
import model.Pago;

public interface PagoDAO {
    List<Pago> listarPorUsuario(int usuarioId) throws Exception;
    int crear(Pago p) throws Exception;
    LocalDate ultimoMesPagado(int usuarioId, int tipoId) throws Exception;
    Pago obtener(int id) throws Exception;
}
