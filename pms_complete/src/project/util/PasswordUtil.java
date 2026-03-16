package project.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password hashing utility with PBKDF2 (secure) and SHA-256 (legacy support).

 * SECURITY UPGRADE:
 * - hashSecure() uses PBKDF2 with salt (resistant to rainbow tables)
 * - hash() kept for backward compatibility with Existing Database

 * MIGRATION PLAN:
 * 1. Use hashSecure() for all NEW passwords
 * 2. Use verifyAny() to support both old (SHA-256) and new (PBKDF2) hashes
 * 3. Gradually migrate users by rehashing on next Login

 * Usage:
 *   String hashed = PasswordUtil.hashSecure("my password"); // NEW passwords
 *   boolean ok = PasswordUtil.verifyAny("my password", hashed); // Supports both
 */
public class PasswordUtil {

    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int PBKDF2_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {} // utility class — no instances

    // ═══════════════════════════════════════════════════════════════════════════
    // SECURE METHOD (PBKDF2 with salt) — Use for NEW passwords
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Hashes password using PBKDF2 with random salt (SECURE).
     * Format: pbkdf2:iterations:salt:hash
     *
     * @param plainText raw password
     * @return secure hash string
     */
    public static String hashSecure(String plainText) {
        if (plainText == null || plainText.isEmpty())
            throw new IllegalArgumentException("Password cannot be empty.");

        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash password with PBKDF2
            PBEKeySpec spec = new PBEKeySpec(
                plainText.toCharArray(),
                salt,
                PBKDF2_ITERATIONS,
                PBKDF2_KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Encode to Base64 for storage
            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);

            // Format: algorithm:iterations:salt:hash
            return String.format("pbkdf2:%d:%s:%s", PBKDF2_ITERATIONS, saltB64, hashB64);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 algorithm not available", e);
        }
    }

    /**
     * Verifies password against PBKDF2 hash.
     */
    public static boolean verifySecure(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;
        if (!storedHash.startsWith("pbkdf2:")) return false;

        try {
            // Parse stored hash: pbkdf2:iterations:salt:hash
            String[] parts = storedHash.split(":");
            if (parts.length != 4) return false;

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            // Hash input password with same salt
            PBEKeySpec spec = new PBEKeySpec(plainText.toCharArray(), salt, iterations, PBKDF2_KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            // Constant-time comparison
            return constantTimeEquals(expectedHash, actualHash);

        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LEGACY METHOD (SHA-256 without salt) — For backward compatibility only
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * LEGACY: Hashes using SHA-256 without salt (INSECURE).
     * Only use for backward compatibility with Existing Database.
     *
     * @deprecated Use hashSecure() for new passwords
     */
    @Deprecated
    public static String hash(String plainText) {
        if (plainText == null || plainText.isEmpty())
            throw new IllegalArgumentException("Password cannot be empty.");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }

    /**
     * LEGACY: Verifies against SHA-256 hash.
     *
     * @deprecated Use verifyAny() for migration support
     */
    @Deprecated
    public static boolean verify(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;
        return hash(plainText).equals(storedHash);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MIGRATION HELPER — Supports both old and new hashes
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Universal verification: works with both SHA-256 (old) and PBKDF2 (new).
     * Use this during Migration Period.
     */
    public static boolean verifyAny(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;

        // Detect Hash Type
        if (storedHash.startsWith("pbkdf2:")) {
            return verifySecure(plainText, storedHash);
        } else {
            // Assume legacy SHA-256 (64 hex characters)
            return verify(plainText, storedHash);
        }
    }

    /**
     * Checks if hash needs upgrade from SHA-256 to PBKDF2.
     */
    public static boolean needsUpgrade(String storedHash) {
        return storedHash != null && !storedHash.startsWith("pbkdf2:");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
