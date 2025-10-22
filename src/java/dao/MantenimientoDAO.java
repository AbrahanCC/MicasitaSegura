package dao;

import model.Mantenimiento;
import java.util.List;

// Interfaz DAO para los reportes de mantenimiento
public interface MantenimientoDAO {

    // Crear un nuevo reporte de mantenimiento
    boolean crear(Mantenimiento m);

    // Obtener un reporte por su ID
    Mantenimiento obtener(int id);

    // Listar todos los reportes activos (para administradores)
    List<Mantenimiento> listar();

    // Listar solo los reportes hechos por un residente específico
    List<Mantenimiento> listarPorResidente(int idResidente);

    // Eliminar un reporte de mantenimiento
    boolean eliminar(int id);
}
