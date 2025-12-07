package financeTracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Base64;
import financeTracker.*;

class PasswordHasherTest {

    // Basic functionality tests
    @Test
    @DisplayName("Hash password should return valid PasswordHash object")
    void testHashPassword_ReturnsValidObject() {
        // Act
        PasswordHasher.PasswordHash result = PasswordHasher.hashPassword("testPassword123");
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getHash());
        assertNotNull(result.getSalt());
        assertFalse(result.getHash().isEmpty());
        assertFalse(result.getSalt().isEmpty());
        
        // Verify both hash and salt are valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(result.getHash()));
        assertDoesNotThrow(() -> Base64.getDecoder().decode(result.getSalt()));
    }

    @Test
    @DisplayName("Same password should produce different hashes due to different salts")
    void testHashPassword_DifferentSalts() {
        // Act
        PasswordHasher.PasswordHash hash1 = PasswordHasher.hashPassword("samePassword");
        PasswordHasher.PasswordHash hash2 = PasswordHasher.hashPassword("samePassword");
        
        // Assert
        assertNotEquals(hash1.getHash(), hash2.getHash(), "Hashes should be different due to different salts");
        assertNotEquals(hash1.getSalt(), hash2.getSalt(), "Salts should be different");
    }

    @Test
    @DisplayName("Verify password should return true for correct password")
    void testVerifyPassword_CorrectPassword() {
        // Arrange
        String password = "correctPassword";
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(password);
        
        // Act
        boolean result = PasswordHasher.verifyPassword(password, hash.getHash(), hash.getSalt());
        
        // Assert
        assertTrue(result, "Verification should succeed for correct password");
    }

    @Test
    @DisplayName("Verify password should return false for incorrect password")
    void testVerifyPassword_IncorrectPassword() {
        // Arrange
        String originalPassword = "originalPassword";
        String wrongPassword = "wrongPassword";
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(originalPassword);
        
        // Act
        boolean result = PasswordHasher.verifyPassword(wrongPassword, hash.getHash(), hash.getSalt());
        
        // Assert
        assertFalse(result, "Verification should fail for incorrect password");
    }

    @Test
    @DisplayName("Verify password should return false for wrong salt")
    void testVerifyPassword_WrongSalt() {
        // Arrange
        String password = "testPassword";
        PasswordHasher.PasswordHash hash1 = PasswordHasher.hashPassword(password);
        PasswordHasher.PasswordHash hash2 = PasswordHasher.hashPassword("differentPassword");
        
        // Use correct hash but wrong salt
        boolean result = PasswordHasher.verifyPassword(password, hash1.getHash(), hash2.getSalt());
        
        // Assert
        assertFalse(result, "Verification should fail with wrong salt");
    }

    @Test
    @DisplayName("Verify password should return false for wrong hash")
    void testVerifyPassword_WrongHash() {
        // Arrange
        String password = "testPassword";
        PasswordHasher.PasswordHash hash1 = PasswordHasher.hashPassword(password);
        PasswordHasher.PasswordHash hash2 = PasswordHasher.hashPassword("differentPassword");
        
        // Use correct salt but wrong hash
        boolean result = PasswordHasher.verifyPassword(password, hash2.getHash(), hash1.getSalt());
        
        // Assert
        assertFalse(result, "Verification should fail with wrong hash");
    }

    // Edge cases for password input
    @Test
    @DisplayName("Empty password should be handled")
    void testHashPassword_EmptyPassword() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword("");
            assertNotNull(hash);
            assertTrue(PasswordHasher.verifyPassword("", hash.getHash(), hash.getSalt()));
        });
    }

    @Test
    @DisplayName("Very long password should be handled")
    void testHashPassword_VeryLongPassword() {
        // Arrange
        String longPassword = "a".repeat(1000);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(longPassword);
            assertTrue(PasswordHasher.verifyPassword(longPassword, hash.getHash(), hash.getSalt()));
        });
    }

    @Test
    @DisplayName("Password with special characters should work")
    void testHashPassword_SpecialCharacters() {
        // Arrange
        String password = "p@ssw0rd!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        // Act
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(password);
        boolean result = PasswordHasher.verifyPassword(password, hash.getHash(), hash.getSalt());
        
        // Assert
        assertTrue(result, "Special characters should be handled correctly");
    }

    @Test
    @DisplayName("Unicode characters in password should work")
    void testHashPassword_UnicodeCharacters() {
        // Arrange
        String password = "密码🔑пароль";
        
        // Act
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(password);
        boolean result = PasswordHasher.verifyPassword(password, hash.getHash(), hash.getSalt());
        
        // Assert
        assertTrue(result, "Unicode characters should be handled correctly");
    }

    // Error handling tests
    @Test
    @DisplayName("Verify password should return false for null password")
    void testVerifyPassword_NullPassword() {
        // Arrange
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword("somePassword");
        
        // Act
        boolean result = PasswordHasher.verifyPassword(null, hash.getHash(), hash.getSalt());
        
        // Assert
        assertFalse(result, "Null password should return false");
    }

    @Test
    @DisplayName("Verify password should return false for null hash")
    void testVerifyPassword_NullHash() {
        // Arrange
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword("somePassword");
        
        // Act
        boolean result = PasswordHasher.verifyPassword("password", null, hash.getSalt());
        
        // Assert
        assertFalse(result, "Null hash should return false");
    }

    @Test
    @DisplayName("Verify password should return false for null salt")
    void testVerifyPassword_NullSalt() {
        // Arrange
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword("somePassword");
        
        // Act
        boolean result = PasswordHasher.verifyPassword("password", hash.getHash(), null);
        
        // Assert
        assertFalse(result, "Null salt should return false");
    }

    @Test
    @DisplayName("Verify password should return false for invalid Base64 hash")
    void testVerifyPassword_InvalidBase64Hash() {
        // Arrange
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword("password");
        
        // Act
        boolean result = PasswordHasher.verifyPassword("password", "invalid-base64!!!", hash.getSalt());
        
        // Assert
        assertFalse(result, "Invalid Base64 hash should return false");
    }

    @Test
    @DisplayName("Verify password should return false for invalid Base64 salt")
    void testVerifyPassword_InvalidBase64Salt() {
        // Arrange
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword("password");
        
        // Act
        boolean result = PasswordHasher.verifyPassword("password", hash.getHash(), "invalid-base64!!!");
        
        // Assert
        assertFalse(result, "Invalid Base64 salt should return false");
    }

    // Test constantTimeEquals method (using reflection since it's private)
    @Test
    @DisplayName("constantTimeEquals should return true for equal arrays")
    void testConstantTimeEquals_EqualArrays() throws Exception {
        // Arrange
        byte[] array1 = {1, 2, 3, 4, 5};
        byte[] array2 = {1, 2, 3, 4, 5};
        
        // Act
        boolean result = invokeConstantTimeEquals(array1, array2);
        
        // Assert
        assertTrue(result, "Equal arrays should return true");
    }

    @Test
    @DisplayName("constantTimeEquals should return false for different arrays")
    void testConstantTimeEquals_DifferentArrays() throws Exception {
        // Arrange
        byte[] array1 = {1, 2, 3, 4, 5};
        byte[] array2 = {1, 2, 3, 4, 6}; // Different last element
        
        // Act
        boolean result = invokeConstantTimeEquals(array1, array2);
        
        // Assert
        assertFalse(result, "Different arrays should return false");
    }

    @Test
    @DisplayName("constantTimeEquals should return false for different length arrays")
    void testConstantTimeEquals_DifferentLengthArrays() throws Exception {
        // Arrange
        byte[] array1 = {1, 2, 3, 4, 5};
        byte[] array2 = {1, 2, 3, 4}; // Shorter array
        
        // Act
        boolean result = invokeConstantTimeEquals(array1, array2);
        
        // Assert
        assertFalse(result, "Different length arrays should return false");
    }

    @Test
    @DisplayName("constantTimeEquals should return true for empty arrays")
    void testConstantTimeEquals_EmptyArrays() throws Exception {
        // Arrange
        byte[] array1 = {};
        byte[] array2 = {};
        
        // Act
        boolean result = invokeConstantTimeEquals(array1, array2);
        
        // Assert
        assertTrue(result, "Empty arrays should return true");
    }

    // Performance test - ensure reasonable performance
    @Test
    @DisplayName("Hashing should complete in reasonable time")
    void testHashPassword_Performance() {
        // Arrange
        String password = "testPassword";
        long startTime, endTime;
        
        // Act
        startTime = System.currentTimeMillis();
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(password);
        endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        
        // Assert - Should complete in under 2 seconds (adjust based on ITERATIONS)
        assertTrue(duration < 2000, "Hashing should complete in reasonable time: " + duration + "ms");
        
        // Verify the hash works
        assertTrue(PasswordHasher.verifyPassword(password, hash.getHash(), hash.getSalt()));
    }

    // Test PasswordHash inner class
    @Test
    @DisplayName("PasswordHash constructor and getters should work correctly")
    void testPasswordHashClass() {
        // Arrange
        String expectedHash = "hashValue";
        String expectedSalt = "saltValue";
        
        // Act
        PasswordHasher.PasswordHash passwordHash = new PasswordHasher.PasswordHash(expectedHash, expectedSalt);
        
        // Assert
        assertEquals(expectedHash, passwordHash.getHash());
        assertEquals(expectedSalt, passwordHash.getSalt());
    }

    // Helper method to invoke private constantTimeEquals using reflection
    private boolean invokeConstantTimeEquals(byte[] a, byte[] b) throws Exception {
        Method method = PasswordHasher.class.getDeclaredMethod("constantTimeEquals", byte[].class, byte[].class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, a, b);
    }

    // Test that the algorithm parameters are reasonable
    @Test
    @DisplayName("Hash should have expected length")
    void testHashLength() {
        // Arrange
        String password = "testPassword";
        
        // Act
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(password);
        byte[] decodedHash = Base64.getDecoder().decode(hash.getHash());
        byte[] decodedSalt = Base64.getDecoder().decode(hash.getSalt());
        
        // Assert - HASH_LENGTH is in bits, so divide by 8 for bytes
        assertEquals(256 / 8, decodedHash.length, "Hash should be 256 bits (32 bytes)");
        assertEquals(32, decodedSalt.length, "Salt should be 32 bytes");
    }

    // Test multiple verifications with same hash
    @Test
    @DisplayName("Multiple verifications with same hash should work consistently")
    void testMultipleVerifications() {
        // Arrange
        String password = "consistentPassword";
        PasswordHasher.PasswordHash hash = PasswordHasher.hashPassword(password);
        
        // Act & Assert - Verify multiple times
        for (int i = 0; i < 10; i++) {
            assertTrue(PasswordHasher.verifyPassword(password, hash.getHash(), hash.getSalt()),
                "Verification should consistently return true for correct password");
        }
        
        for (int i = 0; i < 10; i++) {
            assertFalse(PasswordHasher.verifyPassword("wrongPassword", hash.getHash(), hash.getSalt()),
                "Verification should consistently return false for wrong password");
        }
    }
}