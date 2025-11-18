package financeTracker;

import java.util.Currency;

public class AuthService {
    private final UserRepository userRepository;
    private User currentUser;
    
    public AuthService() {
        this.userRepository = new UserRepository();
        this.currentUser = null;
    }
    
    /**
     * Registers a new user
     */
    public boolean register(String username, String password, String email, String currencyCode) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (userRepository.userExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        try {
            Currency currency = Currency.getInstance(currencyCode);
            return userRepository.createUser(username, password, email, currency);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
        }
    }
    
    /**
     * Logs in a user
     */
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        
        User user = userRepository.authenticateUser(username, password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Changes password for current user
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in");
        }
        
        // Verify current password
        User verifiedUser = userRepository.authenticateUser(
            currentUser.getUsername(), currentPassword);
        
        if (verifiedUser == null) {
            return false;
        }
        
        return userRepository.updatePassword(currentUser.getId(), newPassword);
    }
    
    // Getters
    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
}