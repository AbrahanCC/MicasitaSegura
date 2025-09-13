package dao;

import model.Usuario;
import java.util.List;

public interface UsuarioDAO {
    List<Usuario> listar();
    Usuario obtener(int id);
    boolean crear(Usuario u);
    boolean actualizar(Usuario u);
    boolean eliminar(int id);

    Usuario obtenerPorUsuarioOCorreo(String userOrMail);
    Usuario buscarPorCorreo(String correo);
    Usuario buscarPorIdentificador(String ident);
    void actualizarPassword(int id, String nuevoHash);

    List<Usuario> buscarDirectorio(String nombres, String apellidos, String lote, String numeroCasa);
}
