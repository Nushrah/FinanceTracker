package financeTracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final Connection connection;
    
    public TransactionRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    public void addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (account_id, description, amount, type, category, date, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, transaction.getAccountId());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setBigDecimal(3, transaction.getAmount());
            pstmt.setString(4, transaction.getType().name());
            pstmt.setString(5, transaction.getCategory());
            pstmt.setString(6, transaction.getDate().toString());
            pstmt.setString(7, transaction.getNotes());
            
            pstmt.executeUpdate();
            
            try (var generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add transaction", e);
        }
    }
    
    public List<Transaction> getTransactionsByAccount(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve transactions", e);
        }
        
        return transactions;
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve transactions by date range", e);
        }
        
        return transactions;
    }
    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setAccountId(rs.getInt("account_id"));
        transaction.setDescription(rs.getString("description"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setType(Transaction.TransactionType.valueOf(rs.getString("type")));
        transaction.setCategory(rs.getString("category"));
        transaction.setDate(LocalDate.parse(rs.getString("date")));
        transaction.setNotes(rs.getString("notes"));
        return transaction;
    }
}