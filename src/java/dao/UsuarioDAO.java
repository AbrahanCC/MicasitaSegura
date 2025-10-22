package dao;

import model.Usuario;
import java.util.List;

public interface UsuarioDAO {
    // CRUD
    List<Usuario> listar();
    Usuario obtener(int id);
    boolean crear(Usuario u);
    boolean actualizar(Usuario u);
    boolean eliminar(int id);

    // Login / búsquedas comunes
    Usuario obtenerPorUsuarioOCorreo(String userOrMail);
    Usuario buscarPorCorreo(String correo);
    Usuario buscarPorIdentificador(String ident);
    void actualizarPassword(int id, String nuevoHash);

    // Búsqueda en directorio residencial (RN1)
    List<Usuario> buscarDirectorio(String nombres, String apellidos, String lote, String numeroCasa);

    // Catálogo de correos (residentes activos)
    List<String> listarCorreosResidentesActivos();

    // Correos de guardias activos
    List<String> listarCorreosGuardiasActivos();

    // Obtener ID del residente destino por casa y lote
    Integer findResidenteId(String numeroCasa, String lote);

    // Catálogos
    List<String> catalogoLotes();
    List<String> catalogoCasas();
    List<String> catalogoVisita();

    String obtenerCorreoPorId(int idUsuario);

    List<String> listarCorreosAdminsActivos();
}
