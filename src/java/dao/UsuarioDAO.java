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

    //Búsqueda en directorio residencial (RN1)
    List<Usuario> buscarDirectorio(String nombres, String apellidos, String lote, String numeroCasa);

    // Catálogo de correos (residentes activos)
    List<String> listarCorreosResidentesActivos();

    // Obtener el ID del residente destino por casa y lote
    Integer findResidenteId(String numeroCasa, String lote);
}
