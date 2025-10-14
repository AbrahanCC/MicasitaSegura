package service;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

//Servicio para emitir pases de visita y devolver el token QR
public class EmisionVisitanteService {

 //Inserta el pase y retorna el token generado
  public String emitirVisitante(
      Integer residenteId,
      String nombre,
      String dpi,
      String email,
      String motivo,
      String destinoNumeroCasa,
      String visitType,          // "por_intentos" | "visita"
      Integer usosMax,           // >1; en "visita" usamos 2
      Timestamp qrInicio,        // opcional (no requerido por validación actual)
      Timestamp qrFin,           // en "visita" = fin del día seleccionado
      Integer creadoPorGuardiaId // opcional, puede ser null
  ) throws Exception {

    String token = UUID.randomUUID().toString();

    // Solo columnas existentes y usadas por la validación actual.
    final String sql =
        "INSERT INTO visitantes " +
        " (nombre, dpi, motivo, destino_numero_casa, email, token, " +
        "  estado, visit_type, usos_max, used_count, qr_fin) " +
        "VALUES (?,?,?,?,?,?, 'emitido', ?, ?, 0, ?)";

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      int i = 1;
      ps.setString(i++, nombre);
      ps.setString(i++, (dpi == null || dpi.isEmpty()) ? null : dpi);
      ps.setString(i++, motivo);
      ps.setString(i++, destinoNumeroCasa);
      ps.setString(i++, (email == null || email.isEmpty()) ? null : email);
      ps.setString(i++, token);
      ps.setString(i++, visitType);
      ps.setInt(i++, usosMax != null ? usosMax : 2);
      if (qrFin == null) ps.setNull(i++, java.sql.Types.TIMESTAMP); else ps.setTimestamp(i++, qrFin);

      ps.executeUpdate();
    }

    return token;
  }
}
