package financeTracker;

import financeTracker.Account;
import financeTracker.AccountRepository;
import financeTracker.DatabaseConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAccountRepository {

    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() throws SQLException {
        // Use the same DB connection as AccountRepository
        accountRepository = new AccountRepository();
        Connection connection = DatabaseConnection.getInstance().getConnection();

        // Clean the table before each test to avoid interference
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM accounts");
        }
    }

    private Account createTestAccount(
            int userId,
            String name,
            String typeName,
            String balance,
            String currencyCode
    ) {
        Account account = new Account();
        account.setUserId(userId);
        account.setName(name);
        account.setType(Account.AccountType.valueOf(typeName));
        account.setBalance(new BigDecimal(balance));
        account.setCurrency(Currency.getInstance(currencyCode));
        return account;
    }

    @Test
    @DisplayName("addAccount: generated id is set and data is persisted")
    void testAddAccountAssignsIdAndPersists() {
        Account account = createTestAccount(
                1,
                "Primary Checking",
                "CHECKING",
                "1234.56",
                "USD"
        );

        // Precondition: depending on your Account implementation, id may be 0 by default
        // Just check it's not already > 0
        boolean idInitiallySet = account.getId() > 0;
        assertEquals(false, idInitiallySet);

        accountRepository.addAccount(account);

        boolean idNowSet = account.getId() > 0;
        assertEquals(true, idNowSet);

        List<Account> accounts = accountRepository.getAccountsByUserId(1);
        // exactly one account should be found
        assertEquals(1, accounts.size());

        Account fetched = accounts.get(0);

        assertEquals(account.getId(), fetched.getId());
        assertEquals(1, fetched.getUserId());
        assertEquals("Primary Checking", fetched.getName());
        assertEquals(Account.AccountType.CHECKING, fetched.getType());
        assertEquals(new BigDecimal("1234.56"), fetched.getBalance());
        assertEquals(Currency.getInstance("USD"), fetched.getCurrency());
    }

    @Test
    @DisplayName("getAccountsByUserId: returns empty list when user has no accounts")
    void testGetAccountsByUserIdNoAccounts() {
        int nonExistingUserId = 9999;

        List<Account> accounts = accountRepository.getAccountsByUserId(nonExistingUserId);

        // list should not be null
        boolean isNull = (accounts == null);
        assertEquals(false, isNull);

        // list should be empty
        assertEquals(0, accounts.size());
    }

    @Test
    @DisplayName("getAccountsByUserId: returns multiple accounts for the same user")
    void testGetAccountsByUserIdMultipleAccounts() {
        int userId = 2;

        Account acc1 = createTestAccount(userId, "Savings One", "SAVINGS", "100.00", "USD");
        Account acc2 = createTestAccount(userId, "Savings Two", "SAVINGS", "200.00", "USD");

        accountRepository.addAccount(acc1);
        accountRepository.addAccount(acc2);

        List<Account> accounts = accountRepository.getAccountsByUserId(userId);

        assertEquals(2, accounts.size());

        for (Account a : accounts) {
            assertEquals(userId, a.getUserId());
            assertEquals(Account.AccountType.SAVINGS, a.getType());
            assertEquals(Currency.getInstance("USD"), a.getCurrency());
        }
    }

    @Test
    @DisplayName("getAllAccounts: returns all accounts in table")
    void testGetAllAccounts() {
        Account acc1 = createTestAccount(10, "User10 Checking", "CHECKING", "50.00", "USD");
        Account acc2 = createTestAccount(20, "User20 Savings", "SAVINGS", "75.00", "EUR");
        Account acc3 = createTestAccount(10, "User10 Savings", "SAVINGS", "150.00", "USD");

        accountRepository.addAccount(acc1);
        accountRepository.addAccount(acc2);
        accountRepository.addAccount(acc3);

        List<Account> accounts = accountRepository.getAllAccounts();

        assertEquals(3, accounts.size());

        // We’ll verify presence by boolean flags and assertEquals
        boolean hasUser10Checking = false;
        boolean hasUser20Savings = false;
        boolean hasUser10Savings = false;

        for (Account a : accounts) {
            if (a.getUserId() == 10 &&
                    "User10 Checking".equals(a.getName()) &&
                    Account.AccountType.CHECKING.equals(a.getType())) {
                hasUser10Checking = true;
            }
            if (a.getUserId() == 20 &&
                    "User20 Savings".equals(a.getName()) &&
                    Account.AccountType.SAVINGS.equals(a.getType()) &&
                    Currency.getInstance("EUR").equals(a.getCurrency())) {
                hasUser20Savings = true;
            }
            if (a.getUserId() == 10 &&
                    "User10 Savings".equals(a.getName()) &&
                    Account.AccountType.SAVINGS.equals(a.getType())) {
                hasUser10Savings = true;
            }
        }

        assertEquals(true, hasUser10Checking);
        assertEquals(true, hasUser20Savings);
        assertEquals(true, hasUser10Savings);
    }

    @Test
    @DisplayName("updateAccountBalance: updates the balance in DB")
    void testUpdateAccountBalance() {
        Account acc = createTestAccount(3, "Balance Test", "CHECKING", "500.00", "USD");
        accountRepository.addAccount(acc);

        int accountId = acc.getId();
        BigDecimal newBalance = new BigDecimal("999.99");

        accountRepository.updateAccountBalance(accountId, newBalance);

        Account updated = accountRepository.getAccountById(accountId);

        // updated should not be null
        boolean isNull = (updated == null);
        assertEquals(false, isNull);

        assertEquals(newBalance, updated.getBalance());
    }

    @Test
    @DisplayName("getAccountById: returns account when it exists")
    void testGetAccountByIdExisting() {
        Account acc = createTestAccount(4, "Find Me", "SAVINGS", "300.00", "USD");
        accountRepository.addAccount(acc);

        int accountId = acc.getId();

        Account fetched = accountRepository.getAccountById(accountId);

        // fetched should not be null
        boolean isNull = (fetched == null);
        assertEquals(false, isNull);

        assertEquals(accountId, fetched.getId());
        assertEquals("Find Me", fetched.getName());
        assertEquals(Account.AccountType.SAVINGS, fetched.getType());
        assertEquals(0, new BigDecimal("300.00").compareTo(fetched.getBalance()));
        assertEquals(Currency.getInstance("USD"), fetched.getCurrency());
    }

    @Test
    @DisplayName("getAccountById: returns null when account does not exist")
    void testGetAccountByIdNonExisting() {
        int nonExistingId = 123456;

        Account fetched = accountRepository.getAccountById(nonExistingId);

        // expected null
        boolean isNull = (fetched == null);
        assertEquals(true, isNull);
    }
    
    @Test
    @DisplayName("getAllAccounts: returns empty list when no accounts exist")
    void testGetAllAccountsEmpty() {
        List<Account> accounts = accountRepository.getAllAccounts();
        
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }
    
    @Test
    @DisplayName("getAccountsByUserId: handles multiple different users")
    void testGetAccountsByUserIdMultipleUsers() {
        // Create accounts for different users
        Account user1acc1 = createTestAccount(1, "User1 Account1", "CHECKING", "100.00", "USD");
        Account user1acc2 = createTestAccount(1, "User1 Account2", "SAVINGS", "200.00", "USD");
        Account user2acc1 = createTestAccount(2, "User2 Account1", "CHECKING", "300.00", "EUR");
        
        accountRepository.addAccount(user1acc1);
        accountRepository.addAccount(user1acc2);
        accountRepository.addAccount(user2acc1);
        
        // Test user 1
        List<Account> user1Accounts = accountRepository.getAccountsByUserId(1);
        assertEquals(2, user1Accounts.size());
        
        // Test user 2
        List<Account> user2Accounts = accountRepository.getAccountsByUserId(2);
        assertEquals(1, user2Accounts.size());
        assertEquals(2, user2Accounts.get(0).getUserId());
    }
    
    @Test
    @DisplayName("updateAccountBalance: handles zero balance")
    void testUpdateAccountBalanceToZero() {
        Account acc = createTestAccount(5, "Zero Balance Test", "SAVINGS", "500.00", "USD");
        accountRepository.addAccount(acc);
        
        int accountId = acc.getId();
        BigDecimal zeroBalance = BigDecimal.ZERO;
        
        accountRepository.updateAccountBalance(accountId, zeroBalance);
        
        Account updated = accountRepository.getAccountById(accountId);
        assertNotNull(updated);
        assertEquals(0, zeroBalance.compareTo(updated.getBalance()));
    }
    
    @Test
    @DisplayName("updateAccountBalance: handles negative balance")
    void testUpdateAccountBalanceNegative() {
        Account acc = createTestAccount(6, "Negative Test", "CHECKING", "100.00", "USD");
        accountRepository.addAccount(acc);
        
        int accountId = acc.getId();
        BigDecimal negativeBalance = new BigDecimal("-50.00");
        
        accountRepository.updateAccountBalance(accountId, negativeBalance);
        
        Account updated = accountRepository.getAccountById(accountId);
        assertNotNull(updated);
        assertEquals(0, negativeBalance.compareTo(updated.getBalance()));
    }
    
    // Tests with stub connection for SQLException branches
    @Test
    @DisplayName("addAccount: handles SQLException")
    void testAddAccountSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        AccountRepository repoWithStub = new AccountRepository(stubConnection);
        
        Account account = createTestAccount(1, "Test", "CHECKING", "100", "USD");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.addAccount(account);
        });
        
        assertEquals("Failed to add account", exception.getMessage());
    }
    
    @Test
    @DisplayName("getAccountsByUserId: handles SQLException")
    void testGetAccountsByUserIdSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        AccountRepository repoWithStub = new AccountRepository(stubConnection);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.getAccountsByUserId(1);
        });
        
        assertEquals("Failed to retrieve accounts for user", exception.getMessage());
    }
    
    @Test
    @DisplayName("getAllAccounts: handles SQLException")
    void testGetAllAccountsSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        AccountRepository repoWithStub = new AccountRepository(stubConnection);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.getAllAccounts();
        });
        
        assertEquals("Failed to retrieve accounts", exception.getMessage());
    }
    
    @Test
    @DisplayName("updateAccountBalance: handles SQLException")
    void testUpdateAccountBalanceSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        AccountRepository repoWithStub = new AccountRepository(stubConnection);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.updateAccountBalance(1, new BigDecimal("100"));
        });
        
        assertEquals("Failed to update account balance", exception.getMessage());
    }
    
    @Test
    @DisplayName("getAccountById: handles SQLException")
    void testGetAccountByIdSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        AccountRepository repoWithStub = new AccountRepository(stubConnection);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.getAccountById(1);
        });
        
        assertEquals("Failed to retrieve account", exception.getMessage());
    }
}
