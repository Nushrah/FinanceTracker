package financeTracker;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Currency;

public class UserRepository {
    private final Connection connection;
    
    public UserRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Creates a new user with hashed password
     */
    public boolean createUser(String username, String password, String email, Currency baseCurrency) {
        String sql = "INSERT INTO users (username, password_hash, salt, email, created_date, base_currency) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Hash the password with a random salt
            PasswordHasher.PasswordHash passwordHash = PasswordHasher.hashPassword(password);
            
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash.getHash());
            pstmt.setString(3, passwordHash.getSalt());
            pstmt.setString(4, email);
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.setString(6, baseCurrency.getCurrencyCode());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new RuntimeException("Username already exists");
            }
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    /**
     * Authenticates a user by verifying password hash
     */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT id, username, email, created_date, base_currency, password_hash, salt FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String storedSalt = rs.getString("salt");
                    
                    // Verify the password against the stored hash
                    if (PasswordHasher.verifyPassword(password, storedHash, storedSalt)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setCreatedDate(LocalDateTime.parse(rs.getString("created_date")));
                        user.setBaseCurrency(Currency.getInstance(rs.getString("base_currency")));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to authenticate user", e);
        }
        return null;
    }
    
    /**
     * Checks if username exists
     */
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user existence", e);
        }
    }
    
    /**
     * Updates user password
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            PasswordHasher.PasswordHash passwordHash = PasswordHasher.hashPassword(newPassword);
            
            pstmt.setString(1, passwordHash.getHash());
            pstmt.setString(2, passwordHash.getSalt());
            pstmt.setInt(3, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password", e);
        }
    }
}