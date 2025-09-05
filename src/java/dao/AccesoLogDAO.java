package dao;

import model.AccesoLog;

public interface AccesoLogDAO {
  void insertar(AccesoLog a);

  //Consume 1 uso para un token de VISITA de forma atómica.
  //@param token   token de la visita
  //@param maxUses máximo permitido (p.ej. 2)
  //@return true si se consumió 1 uso; false si ya no había cupo / no existe / inactivo
  boolean consumeOneUseIfAvailable(String token, int maxUses);
}
