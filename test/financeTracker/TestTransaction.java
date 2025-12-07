package financeTracker;

import financeTracker.Transaction;
import financeTracker.Transaction.Category;
import financeTracker.Transaction.TransactionType;
import financeTracker.AccountRepository;
import financeTracker.DatabaseConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTransaction {

    @Test
    @DisplayName("Default constructor + setters/getters")
    void testDefaultConstructorAndSetters() {
        Transaction tx = new Transaction();

        int id = 10;
        int userId = 1;
        int accountId = 2;
        String description = "Coffee";
        BigDecimal amount = new BigDecimal("15.75");
        TransactionType type = TransactionType.EXPENSE;
        String category = "Food & Dining";
        LocalDate date = LocalDate.of(2024, 1, 2);
        String notes = "Morning coffee";

        tx.setId(id);
        tx.setUserId(userId);
        tx.setAccountId(accountId);
        tx.setDescription(description);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategory(category);
        tx.setDate(date);
        tx.setNotes(notes);

        assertEquals(id, tx.getId());
        assertEquals(userId, tx.getUserId());
        assertEquals(accountId, tx.getAccountId());
        assertEquals(description, tx.getDescription());
        assertEquals(amount, tx.getAmount());
        assertEquals(type, tx.getType());
        assertEquals(category, tx.getCategory());
        assertEquals(date, tx.getDate());
        assertEquals(notes, tx.getNotes());
    }

    @Test
    @DisplayName("Parameterized constructor sets all fields correctly (except id and notes)")
    void testParameterizedConstructor() {
        int userId = 5;
        int accountId = 7;
        String description = "Scholarship payment";
        BigDecimal amount = new BigDecimal("1000.00");
        TransactionType type = TransactionType.INCOME;
        String category = "Scholarship";
        LocalDate date = LocalDate.of(2023, 9, 1);

        Transaction tx = new Transaction(userId, accountId, description, amount, type, category, date);

        assertEquals(0, tx.getId()); // default id
        assertEquals(userId, tx.getUserId());
        assertEquals(accountId, tx.getAccountId());
        assertEquals(description, tx.getDescription());
        assertEquals(amount, tx.getAmount());
        assertEquals(type, tx.getType());
        assertEquals(category, tx.getCategory());
        assertEquals(date, tx.getDate());

        String notesIsNull = (tx.getNotes() == null) ? "null" : "not-null";
        assertEquals("null", notesIsNull);
    }

    @Test
    @DisplayName("TransactionType enum values")
    void testTransactionTypeEnum() {
        String incomeName = TransactionType.INCOME.name();
        String expenseName = TransactionType.EXPENSE.name();

        assertEquals("INCOME", incomeName);
        assertEquals("EXPENSE", expenseName);
    }

    @Test
    @DisplayName("Category enum display names")
    void testCategoryDisplayNames() {
        assertEquals("Salary", Category.SALARY.getDisplayName());
        assertEquals("Scholarship", Category.SCHOLARSHIP.getDisplayName());
        assertEquals("Gift", Category.GIFT.getDisplayName());
        assertEquals("Refund", Category.REFUND.getDisplayName());
        assertEquals("Food & Dining", Category.FOOD_DINING.getDisplayName());
        assertEquals("Shopping & Groceries", Category.SHOPPING_GROCERIES.getDisplayName());
        assertEquals("Transportation", Category.TRANSPORTATION.getDisplayName());
        assertEquals("Entertainment", Category.ENTERTAINMENT.getDisplayName());
        assertEquals("Healthcare", Category.HEALTHCARE.getDisplayName());
        assertEquals("Utilities", Category.UTILITIES.getDisplayName());
        assertEquals("Other", Category.OTHER.getDisplayName());
    }

    @Test
    @DisplayName("getIncomeCategories returns correct list and order")
    void testGetIncomeCategories() {
        Category[] income = Category.getIncomeCategories();

        assertEquals(5, income.length);
        assertEquals(Category.SALARY, income[0]);
        assertEquals(Category.SCHOLARSHIP, income[1]);
        assertEquals(Category.GIFT, income[2]);
        assertEquals(Category.REFUND, income[3]);
        assertEquals(Category.OTHER, income[4]);
    }

    @Test
    @DisplayName("getExpenseCategories returns correct list and order")
    void testGetExpenseCategories() {
        Category[] expense = Category.getExpenseCategories();

        assertEquals(7, expense.length);
        assertEquals(Category.FOOD_DINING, expense[0]);
        assertEquals(Category.SHOPPING_GROCERIES, expense[1]);
        assertEquals(Category.TRANSPORTATION, expense[2]);
        assertEquals(Category.ENTERTAINMENT, expense[3]);
        assertEquals(Category.HEALTHCARE, expense[4]);
        assertEquals(Category.UTILITIES, expense[5]);
        assertEquals(Category.OTHER, expense[6]);
    }

    @Test
    @DisplayName("toString produces expected formatted string")
    void testToString() {
        int userId = 2;
        int accountId = 3;
        String description = "Dinner";
        BigDecimal amount = new BigDecimal("123.4"); // will format as 123.40
        TransactionType type = TransactionType.EXPENSE;
        String category = "Food & Dining";
        LocalDate date = LocalDate.of(2024, 5, 15);

        Transaction tx = new Transaction(userId, accountId, description, amount, type, category, date);
        tx.setId(42);

        String result = tx.toString();

        String expected = "Transaction{id=42, userId=2, accountId=3, desc='Dinner', " +
                "amount=123,40, type=EXPENSE, category='Food & Dining', date=2024-05-15}";

        assertEquals(expected, result);
    }
}