package dao;

import java.util.List;
import model.Paquete;

public interface PaqueteDAO {

    long crear(Paquete p) throws Exception;

    /** Lista solo PENDIENTES. Si filtro es null/"" no se filtra. 
     *  Filtro aplica a numero_guia, nombre/apellidos del destinatario, casa y lote. */
    List<Paquete> listarPendientes(String filtro) throws Exception;

    /** Cambia estado a ENTREGADO, setea fecha_entrega=NOW() y entregado_por. */
    boolean marcarEntregado(long paqueteId, int guardiaId) throws Exception;

    /** Obtiene un paquete por id (útil para revalidar antes de entregar). */
    Paquete obtener(long id) throws Exception;
}
