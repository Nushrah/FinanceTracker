package financeTracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for FinanceService
 * Focuses on improving branch coverage
 */
public class FinanceServiceTest {
    
    private FinanceService financeService;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    
    @BeforeEach
    public void setUp() throws SQLException {
        accountRepository = new AccountRepository();
        transactionRepository = new TransactionRepository();
        financeService = new FinanceService(accountRepository, transactionRepository);
        
        // Clean database before each test
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM transactions");
            stmt.executeUpdate("DELETE FROM accounts");
        }
    }
    
    @Test
    public void testAddTransactionIncomeUpdatesBalance() {
        // Create account
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        // Create income transaction
        Transaction income = new Transaction(1, account.getId(), "Salary", 
                                           new BigDecimal("500"), 
                                           Transaction.TransactionType.INCOME,
                                           "Salary", LocalDate.now());
        
        financeService.addTransaction(income);
        
        // Verify balance increased
        Account updated = accountRepository.getAccountById(account.getId());
        assertEquals(new BigDecimal("1500"), updated.getBalance());
    }
    
    @Test
    public void testAddTransactionExpenseUpdatesBalance() {
        // Create account
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        // Create expense transaction
        Transaction expense = new Transaction(1, account.getId(), "Groceries", 
                                            new BigDecimal("200"), 
                                            Transaction.TransactionType.EXPENSE,
                                            "Food & Dining", LocalDate.now());
        
        financeService.addTransaction(expense);
        
        // Verify balance decreased
        Account updated = accountRepository.getAccountById(account.getId());
        assertEquals(new BigDecimal("800"), updated.getBalance());
    }
    
    @Test
    public void testAddTransactionWithNonExistentAccountThrowsException() {
        // Try to add transaction for non-existent account
        Transaction transaction = new Transaction(1, 999, "Test", 
                                                 new BigDecimal("100"), 
                                                 Transaction.TransactionType.INCOME,
                                                 "Salary", LocalDate.now());
        
        assertThrows(IllegalArgumentException.class, () -> {
            financeService.addTransaction(transaction);
        });
    }
    
    @Test
    public void testCalculateMonthlyMetricsWithIncomeAndExpenses() {
        // Setup account and transactions
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        // Add income
        Transaction income = new Transaction(1, account.getId(), "Salary", 
                                           new BigDecimal("5000"), 
                                           Transaction.TransactionType.INCOME,
                                           "Salary", date);
        transactionRepository.addTransaction(income);
        
        // Add expenses
        Transaction expense1 = new Transaction(1, account.getId(), "Rent", 
                                             new BigDecimal("1500"), 
                                             Transaction.TransactionType.EXPENSE,
                                             "Housing", date);
        transactionRepository.addTransaction(expense1);
        
        Transaction expense2 = new Transaction(1, account.getId(), "Food", 
                                             new BigDecimal("500"), 
                                             Transaction.TransactionType.EXPENSE,
                                             "Food & Dining", date);
        transactionRepository.addTransaction(expense2);
        
        // Calculate metrics
        FinancialMetrics metrics = financeService.calculateMonthlyMetrics(2025, 1);
        
        assertEquals(new BigDecimal("5000"), metrics.getTotalIncome());
        assertEquals(new BigDecimal("2000"), metrics.getTotalExpenses());
        assertEquals(new BigDecimal("3000"), metrics.getNetCashFlow());
        assertTrue(metrics.getSavingsRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(metrics.getExpenseToIncomeRatio().compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    public void testCalculateMonthlyMetricsWithNoIncome() {
        // Setup account with only expenses (no income)
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        // Add only expense
        Transaction expense = new Transaction(1, account.getId(), "Food", 
                                            new BigDecimal("200"), 
                                            Transaction.TransactionType.EXPENSE,
                                            "Food & Dining", date);
        transactionRepository.addTransaction(expense);
        
        // Calculate metrics
        FinancialMetrics metrics = financeService.calculateMonthlyMetrics(2025, 1);
        
        assertEquals(BigDecimal.ZERO, metrics.getTotalIncome());
        assertEquals(new BigDecimal("200"), metrics.getTotalExpenses());
        assertEquals(new BigDecimal("-200"), metrics.getNetCashFlow());
        // Savings rate and expense ratio should remain at default (ZERO) when income is zero
        assertEquals(BigDecimal.ZERO, metrics.getSavingsRate());
        assertEquals(BigDecimal.ZERO, metrics.getExpenseToIncomeRatio());
    }
    
    @Test
    public void testCalculateMonthlyMetricsWithNoTransactions() {
        // Calculate metrics for a month with no transactions
        FinancialMetrics metrics = financeService.calculateMonthlyMetrics(2025, 1);
        
        assertEquals(BigDecimal.ZERO, metrics.getTotalIncome());
        assertEquals(BigDecimal.ZERO, metrics.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, metrics.getNetCashFlow());
        assertEquals(BigDecimal.ZERO, metrics.getSavingsRate());
        assertEquals(BigDecimal.ZERO, metrics.getExpenseToIncomeRatio());
    }
    
    @Test
    public void testCalculateExpenseCategoryPercentagesAllAccounts() {
        // Setup account and categorized expenses
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        // Add expenses in different categories
        Transaction food = new Transaction(1, account.getId(), "Groceries", 
                                         new BigDecimal("300"), 
                                         Transaction.TransactionType.EXPENSE,
                                         "Food & Dining", date);
        transactionRepository.addTransaction(food);
        
        Transaction transport = new Transaction(1, account.getId(), "Gas", 
                                              new BigDecimal("100"), 
                                              Transaction.TransactionType.EXPENSE,
                                              "Transportation", date);
        transactionRepository.addTransaction(transport);
        
        Transaction entertainment = new Transaction(1, account.getId(), "Movie", 
                                                   new BigDecimal("100"), 
                                                   Transaction.TransactionType.EXPENSE,
                                                   "Entertainment", date);
        transactionRepository.addTransaction(entertainment);
        
        // Calculate breakdown
        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(2025, 1);
        
        assertEquals(new BigDecimal("500"), breakdown.getTotalExpenses());
        Map<String, BigDecimal> percentages = breakdown.getCategoryPercentages();
        
        assertTrue(percentages.containsKey("Food & Dining"));
        assertTrue(percentages.containsKey("Transportation"));
        assertTrue(percentages.containsKey("Entertainment"));
        
        // Food should be 60% (300/500)
        assertEquals(0, new BigDecimal("60.0000").compareTo(percentages.get("Food & Dining")));
    }
    
    @Test
    public void testCalculateExpenseCategoryPercentagesWithUncategorized() {
        // Setup account
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        // Add expense with "Other" category (instead of null to avoid NOT NULL constraint)
        Transaction uncategorized = new Transaction(1, account.getId(), "Misc", 
                                                  new BigDecimal("100"), 
                                                  Transaction.TransactionType.EXPENSE,
                                                  "Other", date);
        transactionRepository.addTransaction(uncategorized);
        
        // Calculate breakdown
        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(2025, 1);
        
        Map<String, BigDecimal> percentages = breakdown.getCategoryPercentages();
        assertTrue(percentages.containsKey("Other"));
        assertEquals(0, new BigDecimal("100.0000").compareTo(percentages.get("Other")));
    }
    
    @Test
    public void testCalculateExpenseCategoryPercentagesNoExpenses() {
        // Calculate breakdown with no expenses
        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(2025, 1);
        
        assertEquals(BigDecimal.ZERO, breakdown.getTotalExpenses());
        assertTrue(breakdown.getCategoryPercentages().isEmpty());
    }
    
    @Test
    public void testCalculateExpenseCategoryPercentagesByAccountId() {
        // Setup multiple accounts
        Account account1 = new Account(1, "Account 1", Account.AccountType.CHECKING, 
                                      new BigDecimal("1000"), Currency.getInstance("USD"));
        Account account2 = new Account(2, "Account 2", Account.AccountType.SAVINGS, 
                                      new BigDecimal("2000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account1);
        accountRepository.addAccount(account2);
        
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        // Add expenses to account 1
        Transaction food1 = new Transaction(1, account1.getId(), "Groceries", 
                                          new BigDecimal("200"), 
                                          Transaction.TransactionType.EXPENSE,
                                          "Food & Dining", date);
        transactionRepository.addTransaction(food1);
        
        // Add expenses to account 2
        Transaction food2 = new Transaction(2, account2.getId(), "Restaurant", 
                                          new BigDecimal("300"), 
                                          Transaction.TransactionType.EXPENSE,
                                          "Food & Dining", date);
        transactionRepository.addTransaction(food2);
        
        // Calculate breakdown for account 1 only
        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(2025, 1, account1.getId());
        
        // Should only include account 1 expenses
        assertEquals(new BigDecimal("200"), breakdown.getTotalExpenses());
    }
    
    @Test
    public void testCalculateExpenseCategoryPercentagesByAccountIdNoExpenses() {
        // Setup account
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        // Calculate breakdown for account with no expenses
        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(2025, 1, account.getId());
        
        assertEquals(BigDecimal.ZERO, breakdown.getTotalExpenses());
        assertTrue(breakdown.getCategoryPercentages().isEmpty());
    }
    
    @Test
    public void testGetTotalNetWorthMultipleCurrencies() {
        // Setup accounts in different currencies
        Account usdAccount = new Account(1, "USD Account", Account.AccountType.CHECKING, 
                                        new BigDecimal("1000"), Currency.getInstance("USD"));
        Account hkdAccount = new Account(2, "HKD Account", Account.AccountType.SAVINGS, 
                                        new BigDecimal("7800"), Currency.getInstance("HKD"));
        
        accountRepository.addAccount(usdAccount);
        accountRepository.addAccount(hkdAccount);
        
        // Calculate net worth in USD
        BigDecimal netWorth = financeService.getTotalNetWorth(Currency.getInstance("USD"));
        
        // Should convert HKD to USD and add to USD account
        // 7800 HKD / 7.8 = 1000 USD, plus 1000 USD = 2000 USD
        assertTrue(netWorth.compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    public void testGetTotalNetWorthSingleCurrency() {
        // Setup account
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1500"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        // Calculate net worth in same currency
        BigDecimal netWorth = financeService.getTotalNetWorth(Currency.getInstance("USD"));
        
        assertEquals(new BigDecimal("1500"), netWorth);
    }
    
    @Test
    public void testGetTotalNetWorthNoAccounts() {
        // Calculate net worth with no accounts
        BigDecimal netWorth = financeService.getTotalNetWorth(Currency.getInstance("USD"));
        
        assertEquals(BigDecimal.ZERO, netWorth);
    }
    
    @Test
    public void testGetFinancialRecommendation() {
        // Setup account and transactions for recommendation
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        Transaction income = new Transaction(1, account.getId(), "Salary", 
                                           new BigDecimal("5000"), 
                                           Transaction.TransactionType.INCOME,
                                           "Salary", date);
        transactionRepository.addTransaction(income);
        
        Transaction expense = new Transaction(1, account.getId(), "Rent", 
                                            new BigDecimal("2000"), 
                                            Transaction.TransactionType.EXPENSE,
                                            "Housing", date);
        transactionRepository.addTransaction(expense);
        
        // Get recommendation
        String recommendation = financeService.getFinancialRecommendation(2025, 1);
        
        assertNotNull(recommendation);
        assertFalse(recommendation.isEmpty());
    }
    
    @Test
    public void testGetAccountsByUserId() {
        // Setup accounts for different users
        Account user1Account1 = new Account(1, "User 1 - Account 1", Account.AccountType.CHECKING, 
                                          new BigDecimal("1000"), Currency.getInstance("USD"));
        user1Account1.setId(1);
        
        Account user1Account2 = new Account(1, "User 1 - Account 2", Account.AccountType.SAVINGS, 
                                          new BigDecimal("2000"), Currency.getInstance("USD"));
        user1Account2.setId(2);
        
        Account user2Account = new Account(2, "User 2 - Account 1", Account.AccountType.CHECKING, 
                                          new BigDecimal("500"), Currency.getInstance("USD"));
        user2Account.setId(3);
        
        accountRepository.addAccount(user1Account1);
        accountRepository.addAccount(user1Account2);
        accountRepository.addAccount(user2Account);
        
        // Get accounts for user 1
        List<Account> user1Accounts = financeService.getAccountsByUserId(1);
        
        assertEquals(2, user1Accounts.size());
        assertTrue(user1Accounts.stream().allMatch(a -> a.getUserId() == 1));
    }
    
    @Test
    public void testGetAccountsByUserIdNoAccounts() {
        // Get accounts for user with no accounts
        List<Account> accounts = financeService.getAccountsByUserId(999);
        
        assertTrue(accounts.isEmpty());
    }
    
    @Test
    public void testGetAccountById() {
        // Setup account
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        // Get account by ID
        Account retrieved = financeService.getAccountById(account.getId());
        
        assertNotNull(retrieved);
        assertEquals(account.getId(), retrieved.getId());
        assertEquals(account.getName(), retrieved.getName());
    }
    
    @Test
    public void testGetAccountByIdNotFound() {
        // Try to get non-existent account
        Account account = financeService.getAccountById(999);
        
        assertNull(account);
    }
    
    @Test
    public void testAddAccount() {
        // Create and add account
        Account account = new Account(1, "New Account", Account.AccountType.SAVINGS, 
                                     new BigDecimal("500"), Currency.getInstance("USD"));
        
        financeService.addAccount(account);
        
        // Verify account was added
        List<Account> accounts = financeService.getAllAccounts();
        assertEquals(1, accounts.size());
        assertEquals("New Account", accounts.get(0).getName());
    }
    
    @Test
    public void testGetAllAccounts() {
        // Add multiple accounts
        Account account1 = new Account(1, "Account 1", Account.AccountType.CHECKING, 
                                      new BigDecimal("1000"), Currency.getInstance("USD"));
        Account account2 = new Account(2, "Account 2", Account.AccountType.SAVINGS, 
                                      new BigDecimal("2000"), Currency.getInstance("USD"));
        
        financeService.addAccount(account1);
        financeService.addAccount(account2);
        
        // Get all accounts
        List<Account> accounts = financeService.getAllAccounts();
        
        assertEquals(2, accounts.size());
    }
    
    @Test
    public void testGetAccountTransactions() {
        // Setup account and transactions
        Account account = new Account(1, "Test Account", Account.AccountType.CHECKING, 
                                     new BigDecimal("1000"), Currency.getInstance("USD"));
        accountRepository.addAccount(account);
        
        Transaction tx1 = new Transaction(1, account.getId(), "Transaction 1", 
                                        new BigDecimal("100"), 
                                        Transaction.TransactionType.INCOME,
                                        "Salary", LocalDate.now());
        Transaction tx2 = new Transaction(1, account.getId(), "Transaction 2", 
                                        new BigDecimal("50"), 
                                        Transaction.TransactionType.EXPENSE,
                                        "Food & Dining", LocalDate.now());
        
        transactionRepository.addTransaction(tx1);
        transactionRepository.addTransaction(tx2);
        
        // Get transactions for account
        List<Transaction> transactions = financeService.getAccountTransactions(account.getId());
        
        assertEquals(2, transactions.size());
    }
    
    @Test
    public void testGetCurrencyService() {
        // Verify currency service is accessible
        CurrencyExchange currencyService = financeService.getCurrencyService();
        
        assertNotNull(currencyService);
    }
}
