package util;

import org.mindrot.jbcrypt.BCrypt;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class PasswordUtil {
    private static final int ROUNDS = 10;

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(ROUNDS));
    }

    public static boolean verify(String plain, String hashed) {
        if (plain == null || hashed == null || hashed.isEmpty()) return false;

        if (hashed.startsWith("$2y$") || hashed.startsWith("$2b$")) {
            hashed = "$2a$" + hashed.substring(4);
        }

        if (hashed.startsWith("$2a$")) {
            try {
                return BCrypt.checkpw(plain, hashed);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        if (hashed.matches("^[0-9a-fA-F]{64}$")) {
            return sha256(plain).equalsIgnoreCase(hashed);
        }

        return false;
    }

    public static String sha256(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
