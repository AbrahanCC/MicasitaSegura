package util;

// Genera SIEMPRE el mismo token de residente.
//Centralizado para que email y /qr usen la misma fórmula
public final class TokenUtil {

    private TokenUtil() {}

    // Token estable para residente: R:{id}:{firma(32)}
    public static String generateResidentToken(int userId) {
        String secret = System.getProperty("RESIDENT_SECRET", "residentes2025");
        String firma = PasswordUtil.sha256(secret + ":" + userId);
        return "R:" + userId + ":" + firma.substring(0, 32);
    }
}
