package financeTracker;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private int id;
    private int accountId;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
    
    public enum TransactionType {
        INCOME, EXPENSE
    }
    
    public Transaction() {}
    
    public Transaction(int accountId, String description, BigDecimal amount, 
                      TransactionType type, String category, LocalDate date) {
        this.accountId = accountId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @Override
    public String toString() {
        return String.format("Transaction{id=%d, accountId=%d, desc='%s', amount=%.2f, type=%s, category='%s', date=%s}",
            id, accountId, description, amount, type, category, date);
    }
}