package financeTracker;
import financeTracker.*;


import java.math.BigDecimal;
import java.util.Currency;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class TestAccount {

	@Test
    @DisplayName("Default constructor, setters and getters")
    void testDefaultConstructorAndSetters() {
        Account account = new Account();

        int id = 1;
        int userId = 42;
        String name = "My Checking Account";
        Account.AccountType type = Account.AccountType.CHECKING;
        BigDecimal balance = new BigDecimal("1234.56");
        Currency currency = Currency.getInstance("USD");

        account.setId(id);
        account.setUserId(userId);
        account.setName(name);
        account.setType(type);
        account.setBalance(balance);
        account.setCurrency(currency);

        assertEquals(id, account.getId());
        assertEquals(userId, account.getUserId());
        assertEquals(name, account.getName());
        assertEquals(type, account.getType());
        assertEquals(balance, account.getBalance());
        assertEquals(currency, account.getCurrency());
    }

    @Test
    @DisplayName("Parameterized constructor initializes fields correctly")
    void testParameterizedConstructor() {

        int userId = 10;
        String name = "Savings";
        Account.AccountType type = Account.AccountType.SAVINGS;
        BigDecimal balance = new BigDecimal("2500.00");
        Currency currency = Currency.getInstance("EUR");

        Account account = new Account(userId, name, type, balance, currency);

        assertEquals(0, account.getId()); // default int
        assertEquals(userId, account.getUserId());
        assertEquals(name, account.getName());
        assertEquals(type, account.getType());
        assertEquals(balance, account.getBalance());
        assertEquals(currency, account.getCurrency());
    }

    @Test
    @DisplayName("toString formats correctly")
    void testToStringFormatting() {

        Account account = new Account();

        account.setId(7);
        account.setUserId(99);
        account.setName("Investment Account");
        account.setType(Account.AccountType.INVESTMENT);
        account.setBalance(new BigDecimal("1234.567")); // test rounding
        account.setCurrency(Currency.getInstance("JPY"));

        String expected = "Account{user=99, id=7, name='Investment Account', "
                        + "type=INVESTMENT, balance=1234,57 JPY}";

        assertEquals(expected, account.toString());
    }


    @Test
    @DisplayName("toString throws NPE when currency is null (assertEquals version)")
    void testToStringWithNullCurrency() {

        Account account = new Account();

        account.setUserId(1);
        account.setId(2);
        account.setName("Broken Account");
        account.setType(Account.AccountType.CASH);
        account.setBalance(new BigDecimal("10.00"));
        account.setCurrency(null);

        Exception ex = null;

        try {
            account.toString();
        } catch (Exception e) {
            ex = e;
        }

        // Verify that the thrown exception is exactly NullPointerException
        assertEquals(NullPointerException.class, ex.getClass());
    }
}
