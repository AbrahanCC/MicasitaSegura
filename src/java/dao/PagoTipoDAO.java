package dao;

import java.util.List;
import model.PagoTipo;

public interface PagoTipoDAO {
    List<PagoTipo> listarActivos();
    PagoTipo obtenerPorCodigo(String codigo);
    PagoTipo obtenerPorId(int id);
}
