package service;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

// Servicio para emitir pases de visita y devolver el token QR
public class EmisionVisitanteService {

  //Inserta el pase y retorna el token generado
  public String emitirVisitante(
      Integer usuarioId,        // residente destino (FK usuarios.id)
      String nombre,
      String dpi,
      String correo,
      String motivo,
      String lote,
      String casa,
      String tipoVisita,        // "por_intentos" | "visita"
      Integer usosMax,          // >1; en "visita" se usa 2
      Timestamp qrFin,          // fin de validez para tipo "visita" 
      Integer creadoPorGuardiaId // (no se persiste en esta tabla; reservado por si se amplía
  ) throws Exception {

    String token = UUID.randomUUID().toString();

    final String sql =
        "INSERT INTO visitantes " +
        " (usuario_id, nombre, dpi, motivo, correo, token, estado, tipo_visita, lote, casa, usos_max, usos_realizados, qr_fin) " +
        "VALUES (?,?,?,?,?,?,'emitido',?,?,?,?,0,?)";

    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      int i = 1;
      if (usuarioId == null) ps.setNull(i++, java.sql.Types.INTEGER); else ps.setInt(i++, usuarioId);
      ps.setString(i++, nombre);
      if (dpi == null || dpi.isEmpty()) ps.setNull(i++, java.sql.Types.VARCHAR); else ps.setString(i++, dpi);
      ps.setString(i++, motivo);
      if (correo == null || correo.isEmpty()) ps.setNull(i++, java.sql.Types.VARCHAR); else ps.setString(i++, correo);
      ps.setString(i++, token);
      ps.setString(i++, tipoVisita);
      ps.setString(i++, lote);
      ps.setString(i++, casa);
      if (usosMax == null) ps.setNull(i++, java.sql.Types.INTEGER); else ps.setInt(i++, usosMax);
      if (qrFin == null) ps.setNull(i, java.sql.Types.TIMESTAMP); else ps.setTimestamp(i, qrFin);

      ps.executeUpdate();
    }

    return token;
  }
}
