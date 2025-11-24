package financeTracker;

import java.math.BigDecimal;
import java.util.Currency;

public class Account {
    private int id;
    private int userId;
    private String name;
    private AccountType type;
    private BigDecimal balance;
    private Currency currency;
    
    public enum AccountType {
        CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT, CASH
    }
    
    public Account() {}
    
    public Account(int userId, String name, AccountType type, BigDecimal balance, Currency currency) {
    	this.userId = userId;
    	this.name = name;
        this.type = type;
        this.balance = balance;
        this.currency = currency;
    }
    
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    
    @Override
    public String toString() {
        return String.format("Account{user=%d, id=%d, name='%s', type=%s, balance=%.2f %s}", 
            userId, id, name, type, balance, currency.getCurrencyCode());
    }
}