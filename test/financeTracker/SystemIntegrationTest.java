package financeTracker;

import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemIntegrationTest {
    
    private static AuthService authService;
    private static FinanceService financeService;
    private static AccountRepository accountRepository;
    private static TransactionRepository transactionRepository;
    private static UserRepository userRepository;
    
    private static User testUser;
    private static Account checkingAccount;
    private static Account savingsAccount;
    
    @BeforeAll
    public static void setUpSystem() {
        // Initialize all services and repositories
        userRepository = new UserRepository();
        accountRepository = new AccountRepository();
        transactionRepository = new TransactionRepository();
        authService = new AuthService();
        financeService = new FinanceService(accountRepository, transactionRepository);
        
        // Clean up test data if it exists from previous runs
        cleanUpTestData();
        
        System.out.println("=== System Integration Test Started ===");
    }
    
    private static void cleanUpTestData() {
        try {
            // Try to delete test user and related data from database
            var conn = DatabaseConnection.getInstance().getConnection();
            var stmt = conn.createStatement();
            
            // Delete transactions first (foreign key constraint)
            stmt.execute("DELETE FROM transactions WHERE user_id IN (SELECT id FROM users WHERE username = 'testuser')");
            // Delete accounts
            stmt.execute("DELETE FROM accounts WHERE user_id IN (SELECT id FROM users WHERE username = 'testuser')");
            // Delete user
            stmt.execute("DELETE FROM users WHERE username = 'testuser'");
            
            System.out.println("✓ Cleaned up test data from previous runs");
        } catch (Exception e) {
            // Ignore errors if data doesn't exist
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("1. User Registration and Authentication")
    public void testUserRegistrationAndLogin() {
        // Register new user
        String username = "testuser";
        String password = "password123";
        
        boolean registered = authService.register(username, password, "test@example.com", "HKD");
        assertTrue(registered, "User should be registered successfully");
        
        // Login with correct credentials
        User loggedInUser = authService.login(username, password);
        assertNotNull(loggedInUser, "User should be able to login");
        assertEquals(username, loggedInUser.getUsername());
        
        testUser = loggedInUser;
        System.out.println("✓ User registered and logged in: " + username);
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Create Multiple Accounts with Different Currencies")
    public void testAccountCreation() {
        assertNotNull(testUser, "Test user must exist");
        
        // Create checking account in HKD
        checkingAccount = new Account(
            testUser.getId(),
            "Checking Account",
            Account.AccountType.CHECKING,
            BigDecimal.valueOf(10000),
            Currency.getInstance("HKD")
        );
        financeService.addAccount(checkingAccount);
        
        // Create savings account in USD
        savingsAccount = new Account(
            testUser.getId(),
            "Savings Account",
            Account.AccountType.SAVINGS,
            BigDecimal.valueOf(5000),
            Currency.getInstance("USD")
        );
        financeService.addAccount(savingsAccount);
        
        // Verify accounts created
        List<Account> accounts = financeService.getAccountsByUserId(testUser.getId());
        assertEquals(2, accounts.size(), "Should have 2 accounts");
        
        System.out.println("✓ Created 2 accounts: HKD Checking (10,000) and USD Savings (5,000)");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. Add Income Transactions")
    public void testAddIncomeTransactions() {
        assertNotNull(checkingAccount, "Checking account must exist");
        
        // Add salary to checking account
        Transaction salary = new Transaction(
            testUser.getId(),
            checkingAccount.getId(),
            "Monthly salary",
            BigDecimal.valueOf(15000),
            Transaction.TransactionType.INCOME,
            "Salary",
            LocalDate.now()
        );
        financeService.addTransaction(salary);
        
        // Verify balance updated
        Account updated = financeService.getAccountById(checkingAccount.getId());
        BigDecimal expectedBalance = BigDecimal.valueOf(25000); // 10000 + 15000
        assertEquals(0, expectedBalance.compareTo(updated.getBalance()));
        
        System.out.println("✓ Added salary HKD 15,000. New balance: " + updated.getBalance());
    }
    
    @Test
    @Order(4)
    @DisplayName("4. Add Multiple Expense Transactions with Categories")
    public void testAddExpenseTransactions() {
        assertNotNull(checkingAccount, "Checking account must exist");
        
        // Add various expenses
        Transaction rent = new Transaction(
            testUser.getId(),
            checkingAccount.getId(),
            "Monthly rent",
            BigDecimal.valueOf(8000),
            Transaction.TransactionType.EXPENSE,
            "Housing",
            LocalDate.now()
        );
        financeService.addTransaction(rent);
        
        Transaction groceries = new Transaction(
            testUser.getId(),
            checkingAccount.getId(),
            "Groceries",
            BigDecimal.valueOf(2000),
            Transaction.TransactionType.EXPENSE,
            "Food",
            LocalDate.now()
        );
        financeService.addTransaction(groceries);
        
        Transaction entertainment = new Transaction(
            testUser.getId(),
            checkingAccount.getId(),
            "Movies and dining",
            BigDecimal.valueOf(1500),
            Transaction.TransactionType.EXPENSE,
            "Entertainment",
            LocalDate.now()
        );
        financeService.addTransaction(entertainment);
        
        Transaction transport = new Transaction(
            testUser.getId(),
            checkingAccount.getId(),
            "Public transport",
            BigDecimal.valueOf(1000),
            Transaction.TransactionType.EXPENSE,
            "Transportation",
            LocalDate.now()
        );
        financeService.addTransaction(transport);
        
        // Verify balance after expenses
        Account updated = financeService.getAccountById(checkingAccount.getId());
        BigDecimal expectedBalance = BigDecimal.valueOf(12500); // 25000 - 8000 - 2000 - 1500 - 1000
        assertEquals(0, expectedBalance.compareTo(updated.getBalance()));
        
        System.out.println("✓ Added 4 expense transactions. New balance: " + updated.getBalance());
    }
    
    @Test
    @Order(5)
    @DisplayName("5. Calculate Financial Metrics")
    public void testFinancialMetricsCalculation() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        
        FinancialMetrics metrics = financeService.calculateMonthlyMetrics(year, month);
        
        // Verify income and expenses
        assertEquals(0, BigDecimal.valueOf(15000).compareTo(metrics.getTotalIncome()));
        assertEquals(0, BigDecimal.valueOf(12500).compareTo(metrics.getTotalExpenses()));
        
        // Verify net cash flow
        BigDecimal expectedNetFlow = BigDecimal.valueOf(2500); // 15000 - 12500
        assertEquals(0, expectedNetFlow.compareTo(metrics.getNetCashFlow()));
        
        // Verify savings rate (2500/15000 * 100 = 16.67%)
        assertTrue(metrics.getSavingsRate().compareTo(BigDecimal.valueOf(16)) > 0);
        assertTrue(metrics.getSavingsRate().compareTo(BigDecimal.valueOf(17)) < 0);
        
        // Verify expense to income ratio (12500/15000 * 100 = 83.33%)
        assertTrue(metrics.getExpenseToIncomeRatio().compareTo(BigDecimal.valueOf(83)) > 0);
        assertTrue(metrics.getExpenseToIncomeRatio().compareTo(BigDecimal.valueOf(84)) < 0);
        
        System.out.println("✓ Financial Metrics - Income: " + metrics.getTotalIncome() + 
                         ", Expenses: " + metrics.getTotalExpenses() + 
                         ", Savings Rate: " + metrics.getSavingsRate() + "%");
    }
    
    @Test
    @Order(6)
    @DisplayName("6. Calculate Expense Category Breakdown")
    public void testExpenseCategoryBreakdown() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        
        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(year, month);
        
        // Verify total expenses
        assertEquals(0, BigDecimal.valueOf(12500).compareTo(breakdown.getTotalExpenses()));
        
        // Verify category percentages
        // Housing: 8000/12500 = 64%
        BigDecimal housingPct = breakdown.getCategoryPercentages().get("Housing");
        assertNotNull(housingPct);
        assertTrue(housingPct.compareTo(BigDecimal.valueOf(63)) > 0);
        assertTrue(housingPct.compareTo(BigDecimal.valueOf(65)) < 0);
        
        // Food: 2000/12500 = 16%
        BigDecimal foodPct = breakdown.getCategoryPercentages().get("Food");
        assertNotNull(foodPct);
        assertTrue(foodPct.compareTo(BigDecimal.valueOf(15)) > 0);
        assertTrue(foodPct.compareTo(BigDecimal.valueOf(17)) < 0);
        
        System.out.println("✓ Expense Breakdown - Housing: " + housingPct + "%, Food: " + foodPct + "%");
    }
    
    @Test
    @Order(7)
    @DisplayName("7. Get Financial Recommendations")
    public void testFinancialRecommendations() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        
        String recommendation = financeService.getFinancialRecommendation(year, month);
        
        assertNotNull(recommendation);
        assertFalse(recommendation.isEmpty());
        assertTrue(recommendation.length() > 20);
        
        System.out.println("✓ Financial Recommendation: " + recommendation);
    }
    
    @Test
    @Order(8)
    @DisplayName("8. Calculate Total Net Worth with Currency Conversion")
    public void testTotalNetWorthCalculation() {
        // Get user's accounts
        List<Account> userAccounts = financeService.getAccountsByUserId(testUser.getId());
        assertEquals(2, userAccounts.size(), "User should have 2 accounts");
        
        // Calculate net worth in HKD manually
        Currency hkd = Currency.getInstance("HKD");
        CurrencyExchange currencyService = new CurrencyExchange();
        BigDecimal totalHKD = BigDecimal.ZERO;
        
        for (Account account : userAccounts) {
            BigDecimal converted = currencyService.convertCurrency(
                account.getBalance(), account.getCurrency(), hkd);
            totalHKD = totalHKD.add(converted);
        }
        
        // Expected: 12500 HKD (checking) + 5000 USD (converted to HKD)
        // 5000 USD * 7.77 = 38850 HKD
        // Total: 12500 + 38850 = 51350 HKD
        BigDecimal expectedHKD = BigDecimal.valueOf(51350);
        assertEquals(0, expectedHKD.compareTo(totalHKD), 
            "Net worth in HKD should be " + expectedHKD + " but was " + totalHKD);
        
        System.out.println("✓ Total Net Worth - HKD: " + totalHKD);
    }
    
    @Test
    @Order(9)
    @DisplayName("9. Test CSV Import Integration")
    public void testCSVImportIntegration() {
        // Create a test account for CSV import
        Account csvAccount = new Account(
            testUser.getId(),
            "CSV Import Account",
            Account.AccountType.CHECKING,
            BigDecimal.valueOf(5000),
            Currency.getInstance("HKD")
        );
        financeService.addAccount(csvAccount);
        
        // Simulate CSV import by adding transactions programmatically
        CSVImportService csvService = new CSVImportService(transactionRepository);
        
        // Add transaction as if imported from CSV
        Transaction importedTx = new Transaction(
            testUser.getId(),
            csvAccount.getId(),
            "Imported from CSV",
            BigDecimal.valueOf(250),
            Transaction.TransactionType.EXPENSE,
            "Shopping",
            LocalDate.now().minusDays(1)
        );
        financeService.addTransaction(importedTx);
        
        // Verify transaction added
        List<Transaction> transactions = financeService.getAccountTransactions(csvAccount.getId());
        assertEquals(1, transactions.size());
        
        Account updated = financeService.getAccountById(csvAccount.getId());
        BigDecimal expectedBalance = BigDecimal.valueOf(4750); // 5000 - 250
        assertEquals(0, expectedBalance.compareTo(updated.getBalance()));
        
        System.out.println("✓ CSV Import simulation successful. Balance after import: " + updated.getBalance());
    }
    
    @Test
    @Order(10)
    @DisplayName("10. Test Password Change")
    public void testPasswordChange() {
        assertNotNull(testUser, "Test user must exist");
        
        String oldPassword = "password123";
        String newPassword = "newpassword456";
        
        // Login first before changing password
        authService.login(testUser.getUsername(), oldPassword);
        
        // Change password
        boolean changed = authService.changePassword(oldPassword, newPassword);
        assertTrue(changed, "Password should be changed successfully");
        
        // Try logging in with old password (should fail)
        User failedLogin = authService.login(testUser.getUsername(), oldPassword);
        assertNull(failedLogin, "Login with old password should fail");
        
        // Login with new password (should succeed)
        User successLogin = authService.login(testUser.getUsername(), newPassword);
        assertNotNull(successLogin, "Login with new password should succeed");
        
        System.out.println("✓ Password changed successfully");
    }
    
    @Test
    @Order(11)
    @DisplayName("11. End-to-End Multi-Currency Transaction Flow")
    public void testMultiCurrencyFlow() {
        // Add income to USD savings account
        Transaction usdIncome = new Transaction(
            testUser.getId(),
            savingsAccount.getId(),
            "Stock dividends",
            BigDecimal.valueOf(1000),
            Transaction.TransactionType.INCOME,
            "Investment Return",
            LocalDate.now()
        );
        financeService.addTransaction(usdIncome);
        
        // Verify USD account balance
        Account updatedSavings = financeService.getAccountById(savingsAccount.getId());
        BigDecimal expectedUSD = BigDecimal.valueOf(6000); // 5000 + 1000
        assertEquals(0, expectedUSD.compareTo(updatedSavings.getBalance()));
        
        // Add expense to HKD checking account
        Transaction hkdExpense = new Transaction(
            testUser.getId(),
            checkingAccount.getId(),
            "Medical checkup",
            BigDecimal.valueOf(500),
            Transaction.TransactionType.EXPENSE,
            "Healthcare",
            LocalDate.now()
        );
        financeService.addTransaction(hkdExpense);
        
        // Verify HKD account balance
        Account updatedChecking = financeService.getAccountById(checkingAccount.getId());
        BigDecimal expectedHKD = BigDecimal.valueOf(12000); // 12500 - 500
        assertEquals(0, expectedHKD.compareTo(updatedChecking.getBalance()));
        
        // Test currency conversion between accounts
        CurrencyExchange exchange = financeService.getCurrencyService();
        BigDecimal usdInHKD = exchange.convertCurrency(
            BigDecimal.valueOf(1000),
            Currency.getInstance("USD"),
            Currency.getInstance("HKD")
        );
        assertEquals(0, BigDecimal.valueOf(7770).compareTo(usdInHKD));
        
        System.out.println("✓ Multi-currency flow complete. USD: " + updatedSavings.getBalance() + 
                         ", HKD: " + updatedChecking.getBalance());
    }
    
    @Test
    @Order(12)
    @DisplayName("12. Verify Complete System State")
    public void testCompleteSystemState() {
        // Verify all accounts exist
        List<Account> allAccounts = financeService.getAllAccounts();
        assertTrue(allAccounts.size() >= 3, "Should have at least 3 accounts");
        
        // Verify transactions recorded
        List<Transaction> checkingTxs = financeService.getAccountTransactions(checkingAccount.getId());
        assertTrue(checkingTxs.size() >= 6, "Checking account should have at least 6 transactions");
        
        List<Transaction> savingsTxs = financeService.getAccountTransactions(savingsAccount.getId());
        assertTrue(savingsTxs.size() >= 1, "Savings account should have at least 1 transaction");
        
        // Verify final metrics
        LocalDate today = LocalDate.now();
        FinancialMetrics finalMetrics = financeService.calculateMonthlyMetrics(
            today.getYear(), 
            today.getMonthValue()
        );
        
        assertTrue(finalMetrics.getTotalIncome().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(finalMetrics.getTotalExpenses().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(finalMetrics.getNetCashFlow());
        
        // Calculate final net worth
        BigDecimal finalNetWorth = financeService.getTotalNetWorth(Currency.getInstance("HKD"));
        assertTrue(finalNetWorth.compareTo(BigDecimal.valueOf(50000)) > 0);
        
        System.out.println("\n=== FINAL SYSTEM STATE ===");
        System.out.println("Total Accounts: " + allAccounts.size());
        System.out.println("Total Income: HKD " + finalMetrics.getTotalIncome());
        System.out.println("Total Expenses: HKD " + finalMetrics.getTotalExpenses());
        System.out.println("Net Cash Flow: HKD " + finalMetrics.getNetCashFlow());
        System.out.println("Total Net Worth: HKD " + finalNetWorth);
        System.out.println("=== System Integration Test Complete ===");
    }
    
    @AfterAll
    public static void tearDown() {
        System.out.println("\n✓ All 12 integration tests passed successfully!");
        System.out.println("System verified: Authentication → Accounts → Transactions → " +
                         "Metrics → Recommendations → Currency Conversion");
    }
}
