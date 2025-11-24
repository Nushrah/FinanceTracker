package financeTracker;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Currency;

public class AccountRepository {
    private final Connection connection;
    
    public AccountRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    public void addAccount(Account account) {
        String sql = "INSERT INTO accounts (user_id, name, type, balance, currency) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        	pstmt.setInt(1, account.getUserId());
            pstmt.setString(2, account.getName());
            pstmt.setString(3, account.getType().name());
            pstmt.setBigDecimal(4, account.getBalance());
            pstmt.setString(5, account.getCurrency().getCurrencyCode());
            
            pstmt.executeUpdate();
            
            try (var generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add account", e);
        }
    }
    
    public List<Account> getAccountsByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account();
                    account.setId(rs.getInt("id"));
                    account.setUserId(rs.getInt("user_id"));
                    account.setName(rs.getString("name"));
                    account.setType(Account.AccountType.valueOf(rs.getString("type")));
                    account.setBalance(rs.getBigDecimal("balance"));
                    account.setCurrency(Currency.getInstance(rs.getString("currency")));
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve accounts for user", e);
        }
        
        return accounts;
    }
    
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getInt("id"));
                account.setUserId(rs.getInt("user_id"));
                account.setName(rs.getString("name"));
                account.setType(Account.AccountType.valueOf(rs.getString("type")));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setCurrency(Currency.getInstance(rs.getString("currency")));
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve accounts", e);
        }
        
        return accounts;
    }
    
    public void updateAccountBalance(int accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account balance", e);
        }
    }
    
    public Account getAccountById(int id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account();
                    account.setId(rs.getInt("id"));
                    account.setName(rs.getString("name"));
                    account.setType(Account.AccountType.valueOf(rs.getString("type")));
                    account.setBalance(rs.getBigDecimal("balance"));
                    account.setCurrency(Currency.getInstance(rs.getString("currency")));
                    return account;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve account", e);
        }
        return null;
    }
}