package financeTracker;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordHasher {
    private static final int SALT_LENGTH = 32;
    private static final int HASH_LENGTH = 256;
    private static final int ITERATIONS = 100000;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    
    /**
     * Hashes a password with a randomly generated salt
     */
    public static PasswordHash hashPassword(String password) {
        try {
            byte[] salt = generateSalt();
            byte[] hash = calculateHash(password, salt);
            
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            return new PasswordHash(hashBase64, saltBase64);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies a password against a stored hash and salt
     */
    public static boolean verifyPassword(String password, String storedHash, String storedSalt) {
        try {
            byte[] salt = Base64.getDecoder().decode(storedSalt);
            byte[] expectedHash = Base64.getDecoder().decode(storedHash);
            byte[] actualHash = calculateHash(password, salt);
            
            // Constant-time comparison to prevent timing attacks
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
    
    private static byte[] calculateHash(String password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), 
            salt, 
            ITERATIONS, 
            HASH_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
    
    /**
     * Constant-time comparison to prevent timing attacks
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    
    public static class PasswordHash {
        private final String hash;
        private final String salt;
        
        public PasswordHash(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }
        
        public String getHash() { return hash; }
        public String getSalt() { return salt; }
    }
}