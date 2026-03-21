package project.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


public class PasswordUtil {

    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int PBKDF2_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {}


    public static String hashSecure(String plainText) {
        if (plainText == null || plainText.isEmpty())
            throw new IllegalArgumentException("Password cannot be empty.");

        try {

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);


            PBEKeySpec spec = new PBEKeySpec(
                plainText.toCharArray(),
                salt,
                PBKDF2_ITERATIONS,
                PBKDF2_KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();


            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);


            return String.format("pbkdf2:%d:%s:%s", PBKDF2_ITERATIONS, saltB64, hashB64);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 algorithm not available", e);
        }
    }

    public static boolean verifySecure(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;
        if (!storedHash.startsWith("pbkdf2:")) return false;

        try {

            String[] parts = storedHash.split(":");
            if (parts.length != 4) return false;

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);


            PBEKeySpec spec = new PBEKeySpec(plainText.toCharArray(), salt, iterations, PBKDF2_KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = factory.generateSecret(spec).getEncoded();


            return constantTimeEquals(expectedHash, actualHash);

        } catch (Exception e) {
            return false;
        }
    }

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


    @Deprecated
    public static boolean verify(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;
        return hash(plainText).equals(storedHash);
    }


    public static boolean verifyAny(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;


        if (storedHash.startsWith("pbkdf2:")) {
            return verifySecure(plainText, storedHash);
        } else {

            return verify(plainText, storedHash);
        }
    }


    public static boolean needsUpgrade(String storedHash) {
        return storedHash != null && !storedHash.startsWith("pbkdf2:");
    }


    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
