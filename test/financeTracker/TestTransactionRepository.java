package financeTracker;

import financeTracker.Transaction;
import financeTracker.TransactionRepository;
import financeTracker.DatabaseConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTransactionRepository {

    private Connection connection;
    private TransactionRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        connection = DatabaseConnection.getInstance().getConnection();

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS transactions (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user_id INTEGER NOT NULL," +
                            "account_id INTEGER NOT NULL," +
                            "description TEXT," +
                            "amount DECIMAL(10,2)," +
                            "type TEXT," +
                            "category TEXT," +
                            "date TEXT," +
                            "notes TEXT" +
                            ")"
            );
            stmt.executeUpdate("DELETE FROM transactions");
        }

        repository = new TransactionRepository();
    }

    // ------------ Helpers ------------

    private Transaction buildSampleTransaction() {
        Transaction tx = new Transaction();
        tx.setUserId(1);
        tx.setAccountId(100);
        tx.setDescription("Coffee");
        tx.setAmount(new BigDecimal("15.50"));
        tx.setType(Transaction.TransactionType.EXPENSE);
        tx.setCategory("Food");
        tx.setDate(LocalDate.of(2024, 1, 1));
        tx.setNotes("Morning");
        return tx;
    }

    private void insertRow(Integer id, int userId, int accountId,
                           String desc, BigDecimal amt, String type,
                           String cat, LocalDate date, String notes) throws Exception {

        String sql = (id == null)
                ? "INSERT INTO transactions (user_id, account_id, description, amount, type, category, date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                : "INSERT INTO transactions (id, user_id, account_id, description, amount, type, category, date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int i = 1;
            if (id != null) pstmt.setInt(i++, id);
            pstmt.setInt(i++, userId);
            pstmt.setInt(i++, accountId);
            pstmt.setString(i++, desc);
            pstmt.setBigDecimal(i++, amt);
            pstmt.setString(i++, type);
            pstmt.setString(i++, cat);
            pstmt.setString(i++, date.toString());
            pstmt.setString(i, notes);
            pstmt.executeUpdate();
        }
    }

    // ------------ addTransaction tests ------------

    @Test
    @DisplayName("addTransaction inserts correctly & sets generated ID")
    void testAddTransactionNormal() throws Exception {
        Transaction tx = buildSampleTransaction();
        repository.addTransaction(tx);

        // check ID set
        int idSet = (tx.getId() > 0) ? 1 : 0;
        assertEquals(1, idSet);

        // verify DB row
        try (PreparedStatement pstmt =
                     connection.prepareStatement("SELECT * FROM transactions WHERE id = ?")) {
            pstmt.setInt(1, tx.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                int rowExists = rs.next() ? 1 : 0;
                assertEquals(1, rowExists);

                assertEquals(tx.getUserId(), rs.getInt("user_id"));
                assertEquals(tx.getAccountId(), rs.getInt("account_id"));
                assertEquals(tx.getDescription(), rs.getString("description"));
                assertEquals(0, tx.getAmount().compareTo(rs.getBigDecimal("amount")));
                assertEquals(tx.getType().name(), rs.getString("type"));
                assertEquals(tx.getCategory(), rs.getString("category"));
                assertEquals(tx.getDate().toString(), rs.getString("date"));
                assertEquals(tx.getNotes(), rs.getString("notes"));
            }
        }
    }

    @Test
    @DisplayName("addTransaction catches SQLException and throws RuntimeException")
    void testAddTransactionErrorBranch() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS transactions");
        }

        Transaction tx = buildSampleTransaction();

        int exceptionThrown = 0;
        try {
            repository.addTransaction(tx);
        } catch (RuntimeException e) {
            exceptionThrown = 1;

            // exception message contains "Failed"
            int contains = e.getMessage().contains("Failed") ? 1 : 0;
            assertEquals(1, contains);
        }

        assertEquals(1, exceptionThrown);
    }

    // ------------ getTransactionsByAccount ------------

    @Test
    @DisplayName("getTransactionsByAccount returns sorted results")
    void testGetByAccountNormal() throws Exception {
        insertRow(1, 1, 50, "Old", new BigDecimal("10.0"), "EXPENSE",
                "A", LocalDate.of(2024,1,1), "x");

        insertRow(2, 1, 50, "New", new BigDecimal("20.0"), "INCOME",
                "B", LocalDate.of(2024,2,1), "y");

        insertRow(3, 1, 999, "Other", new BigDecimal("99.0"), "EXPENSE",
                "C", LocalDate.of(2024,3,1), "z");

        List<Transaction> res = repository.getTransactionsByAccount(50);

        assertEquals(2, res.size());
        assertEquals("New", res.get(0).getDescription());
        assertEquals("Old", res.get(1).getDescription());
        assertEquals("y", res.get(0).getNotes());
        assertEquals("x", res.get(1).getNotes());
    }

    @Test
    @DisplayName("getTransactionsByAccount returns empty list")
    void testGetByAccountEmpty() {
        List<Transaction> res = repository.getTransactionsByAccount(777);
        assertEquals(0, res.size());
    }

    @Test
    @DisplayName("getTransactionsByAccount SQLException branch")
    void testGetByAccountErrorBranch() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS transactions");
        }

        int caught = 0;
        try {
            repository.getTransactionsByAccount(1);
        } catch (RuntimeException e) {
            caught = 1;

            int contains = e.getMessage().contains("Failed") ? 1 : 0;
            assertEquals(1, contains);
        }

        assertEquals(1, caught);
    }

    // ------------ getTransactionsByDateRange ------------

    @Test
    @DisplayName("getTransactionsByDateRange returns correct filtered results")
    void testGetByDateRangeNormal() throws Exception {

        insertRow(1,1,1,"Jan1", new BigDecimal("10"),"EXPENSE","X",
                LocalDate.of(2024,1,1),"N1");

        insertRow(2,1,1,"Jan10", new BigDecimal("11"),"EXPENSE","X",
                LocalDate.of(2024,1,10),"N2");

        insertRow(3,1,1,"Jan20", new BigDecimal("12"),"INCOME","X",
                LocalDate.of(2024,1,20),"N3");

        // Out of range
        insertRow(4,1,1,"Dec", new BigDecimal("5"),"EXPENSE","X",
                LocalDate.of(2023,12,31),"No");

        List<Transaction> res =
                repository.getTransactionsByDateRange(LocalDate.of(2024,1,1),
                                                      LocalDate.of(2024,1,31));

        assertEquals(3, res.size());
        assertEquals("Jan20", res.get(0).getDescription());
        assertEquals("Jan10", res.get(1).getDescription());
        assertEquals("Jan1", res.get(2).getDescription());
    }

    @Test
    @DisplayName("getTransactionsByDateRange empty list")
    void testGetByDateRangeEmpty() {
        List<Transaction> res =
                repository.getTransactionsByDateRange(LocalDate.of(2000,1,1),
                                                      LocalDate.of(2000,1,2));
        assertEquals(0, res.size());
    }

    @Test
    @DisplayName("getTransactionsByDateRange SQLException branch")
    void testGetByDateRangeErrorBranch() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS transactions");
        }

        int caught = 0;
        try {
            repository.getTransactionsByDateRange(LocalDate.of(2024,1,1),
                                                  LocalDate.of(2024,1,10));
        } catch (RuntimeException e) {
            caught = 1;

            int contains = e.getMessage().contains("Failed") ? 1 : 0;
            assertEquals(1, contains);
        }

        assertEquals(1, caught);
    }
    
    @Test
    @DisplayName("getTransactionsByDateRange: handles same start and end date")
    void testGetByDateRangeSameDate() throws Exception {
        LocalDate targetDate = LocalDate.of(2024, 5, 15);
        
        insertRow(1, 1, 1, "SameDay1", new BigDecimal("100"), "INCOME", "Salary",
                targetDate, "Test1");
        insertRow(2, 1, 1, "SameDay2", new BigDecimal("50"), "EXPENSE", "Food",
                targetDate, "Test2");
        insertRow(3, 1, 1, "DifferentDay", new BigDecimal("30"), "EXPENSE", "Transport",
                LocalDate.of(2024, 5, 16), "Test3");
        
        List<Transaction> res = repository.getTransactionsByDateRange(targetDate, targetDate);
        
        assertEquals(2, res.size());
        assertTrue(res.stream().anyMatch(t -> "SameDay1".equals(t.getDescription())));
        assertTrue(res.stream().anyMatch(t -> "SameDay2".equals(t.getDescription())));
    }
    
    @Test
    @DisplayName("getTransactionsByAccount: handles multiple types and categories")
    void testGetByAccountMultipleTypesAndCategories() throws Exception {
        int targetAccountId = 42;
        
        insertRow(1, 1, targetAccountId, "Income1", new BigDecimal("1000"), "INCOME",
                "Salary", LocalDate.of(2024, 1, 1), "Monthly");
        insertRow(2, 1, targetAccountId, "Expense1", new BigDecimal("200"), "EXPENSE",
                "Food", LocalDate.of(2024, 1, 5), "Groceries");
        insertRow(3, 1, targetAccountId, "Expense2", new BigDecimal("100"), "EXPENSE",
                "Transport", LocalDate.of(2024, 1, 10), "Gas");
        insertRow(4, 1, 999, "OtherAccount", new BigDecimal("50"), "EXPENSE",
                "Food", LocalDate.of(2024, 1, 1), "Ignored");
        
        List<Transaction> res = repository.getTransactionsByAccount(targetAccountId);
        
        assertEquals(3, res.size());
        
        // Verify they're sorted by date descending
        assertEquals("Expense2", res.get(0).getDescription());
        assertEquals("Expense1", res.get(1).getDescription());
        assertEquals("Income1", res.get(2).getDescription());
    }
    
    @Test
    @DisplayName("addTransaction: handles large amounts")
    void testAddTransactionLargeAmount() {
        Transaction tx = buildSampleTransaction();
        tx.setAmount(new BigDecimal("999999.99"));
        
        repository.addTransaction(tx);
        
        assertTrue(tx.getId() > 0);
        
        List<Transaction> retrieved = repository.getTransactionsByAccount(tx.getAccountId());
        assertEquals(1, retrieved.size());
        assertEquals(0, new BigDecimal("999999.99").compareTo(retrieved.get(0).getAmount()));
    }
    
    @Test
    @DisplayName("getTransactionsByDateRange: handles boundary dates correctly")
    void testGetByDateRangeBoundaries() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        
        // Exactly on start date
        insertRow(1, 1, 1, "Start", new BigDecimal("100"), "INCOME", "X",
                startDate, "On start");
        
        // Exactly on end date  
        insertRow(2, 1, 1, "End", new BigDecimal("200"), "INCOME", "X",
                endDate, "On end");
        
        // Between
        insertRow(3, 1, 1, "Middle", new BigDecimal("150"), "INCOME", "X",
                LocalDate.of(2024, 3, 15), "In middle");
        
        // Before start
        insertRow(4, 1, 1, "Before", new BigDecimal("50"), "INCOME", "X",
                startDate.minusDays(1), "Before start");
        
        // After end
        insertRow(5, 1, 1, "After", new BigDecimal("50"), "INCOME", "X",
                endDate.plusDays(1), "After end");
        
        List<Transaction> res = repository.getTransactionsByDateRange(startDate, endDate);
        
        assertEquals(3, res.size());
        
        // Verify the boundary dates are included
        boolean hasStart = res.stream().anyMatch(t -> t.getDescription().equals("Start"));
        boolean hasEnd = res.stream().anyMatch(t -> t.getDescription().equals("End"));
        boolean hasMiddle = res.stream().anyMatch(t -> t.getDescription().equals("Middle"));
        
        assertTrue(hasStart);
        assertTrue(hasEnd);
        assertTrue(hasMiddle);
    }
}
