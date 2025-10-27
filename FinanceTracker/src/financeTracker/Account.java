package financeTracker;

import java.math.BigDecimal;
import java.util.Currency;

public class Account {
    private int id;
    private String name;
    private AccountType type;
    private BigDecimal balance;
    private Currency currency;
    
    public enum AccountType {
        CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT, CASH
    }
    
    public Account() {}
    
    public Account(String name, AccountType type, BigDecimal balance, Currency currency) {
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.currency = currency;
    }
    
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
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
        return String.format("Account{id=%d, name='%s', type=%s, balance=%.2f %s}", 
            id, name, type, balance, currency.getCurrencyCode());
    }
}