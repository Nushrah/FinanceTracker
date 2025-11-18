package financeTracker;

import java.time.LocalDateTime;
import java.util.Currency;

public class User {
    private int id;
    private String username;
    private String email;
    private LocalDateTime createdDate;
    private Currency baseCurrency;
    
    // Note: We don't store password for security
    
    public User() {}
    
    public User(String username, String email, Currency baseCurrency) {
        this.username = username;
        this.email = email;
        this.baseCurrency = baseCurrency;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public Currency getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(Currency baseCurrency) { this.baseCurrency = baseCurrency; }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s', currency=%s}", 
            id, username, email, baseCurrency.getCurrencyCode());
    }
}