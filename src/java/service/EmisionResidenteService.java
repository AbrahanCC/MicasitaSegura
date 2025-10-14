package service;

import model.Usuario;
import util.PasswordUtil;

// Servicio para generar token permanente de residentes
public class EmisionResidenteService {

  // Genera token permanente a partir del id del usuario (formato: R:<id>:<firma32>)
  public String generarTokenPermanente(int usuarioId) {
    // Secret configurable por propiedad del sistema
    String secret = System.getProperty("RESIDENT_SECRET", "residentes2025");
    String firma = PasswordUtil.sha256(secret + ":" + usuarioId);
    return "R:" + usuarioId + ":" + firma.substring(0, 32);
  }

  // Overload de conveniencia por si prefieres pasar el modelo Usuario
  public String generarTokenPermanente(Usuario u) {
    if (u == null || u.getId() <= 0) {
      throw new IllegalArgumentException("Usuario inválido para token permanente.");
    }
    return generarTokenPermanente(u.getId());
  }
}
