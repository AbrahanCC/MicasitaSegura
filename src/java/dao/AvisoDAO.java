package dao;

import model.Aviso;

public interface AvisoDAO {
    int crear(Aviso a); // retorna ID generado
    void registrarEnvio(int avisoId, String email, String estado, String detalle);
}
