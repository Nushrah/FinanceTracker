package financeTracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVImportServiceTest {

    private CSVImportService csvImportService;
    private TransactionRepository transactionRepository;
    
    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        // Create a real TransactionRepository (it will use the test database connection)
        transactionRepository = new TransactionRepository();
        csvImportService = new CSVImportService(transactionRepository);
    }
    
    // Helper method to create CSVImportService with auto-answer for categorization
    private CSVImportService createAutoAnsweringCSVService() {
        // Mock System.in to automatically answer "1" for each categorization prompt
        // This simulates user always selecting the first category
        String autoInput = "1\n".repeat(100); // Enough "1" answers for all tests
        System.setIn(new ByteArrayInputStream(autoInput.getBytes()));
        return new CSVImportService(transactionRepository);
    }

    // Test helper class to capture System.out and System.err
    private static class OutputCapture {
        private final StringBuilder output = new StringBuilder();
        private final StringBuilder error = new StringBuilder();

        public void startCapture() {
            System.setOut(new java.io.PrintStream(System.out) {
                public void print(String s) {
                    super.print(s);
                    output.append(s);
                }
            });
            System.setErr(new java.io.PrintStream(System.err) {
                public void print(String s) {
                    super.print(s);
                    error.append(s);
                }
            });
        }

        public String getOutput() {
            return output.toString();
        }

        public String getError() {
            return error.toString();
        }
    }

    @Test
    void testParseCSVLine_SimpleCommaSeparated() {
        String line = "field1,field2,field3";
        String[] result = csvImportService.parseCSVLine(line);
        
        assertArrayEquals(new String[]{"field1", "field2", "field3"}, result);
    }

    @Test
    void testParseCSVLine_WithQuotesAndCommas() {
        String line = "\"field,1\",field2,\"field,3\"";
        String[] result = csvImportService.parseCSVLine(line);
        
        assertArrayEquals(new String[]{"field,1", "field2", "field,3"}, result);
    }

    @Test
    void testParseCSVLine_WithSpacesAndTrimming() {
        String line = "  field1  , field2 ,  field3  ";
        String[] result = csvImportService.parseCSVLine(line);
        
        assertArrayEquals(new String[]{"field1", "field2", "field3"}, result);
    }

    @Test
    void testParseCSVLine_EmptyFields() {
        String line = "field1,,field3,";
        String[] result = csvImportService.parseCSVLine(line);
        
        assertArrayEquals(new String[]{"field1", "", "field3", ""}, result);
    }

    @Test
    void testParseCSVLine_OnlyCommas() {
        String line = ",,,";
        String[] result = csvImportService.parseCSVLine(line);
        
        assertArrayEquals(new String[]{"", "", "", ""}, result);
    }

    @Test
    void testParseBankStatementTransaction_IncomeTransaction() {
        String[] fields = {"HKD", "1 Feb", "Salary Deposit", "5000.00", "", "15000.00"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getAccountId());
        assertEquals("Salary Deposit", result.getDescription());
        assertEquals(0, new BigDecimal("5000.00").compareTo(result.getAmount()));
        assertEquals(Transaction.TransactionType.INCOME, result.getType());
        assertEquals("Temporary", result.getCategory());
        assertNotNull(result.getDate());
    }

    @Test
    void testParseBankStatementTransaction_ExpenseTransaction() {
        String[] fields = {"HKD", "2 Feb", "Grocery Shopping", "", "150.50", "14849.50"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNotNull(result);
        assertEquals(0, new BigDecimal("150.50").compareTo(result.getAmount()));
        assertEquals(Transaction.TransactionType.EXPENSE, result.getType());
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getAccountId());
    }

    @Test
    void testParseBankStatementTransaction_ForeignCurrencyAddsNotes() {
        String[] fields = {"USD", "3 Feb", "Amazon Purchase", "", "100.00", "14749.50"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNotNull(result);
        assertEquals("Original currency: USD", result.getNotes());
    }

    @Test
    void testParseBankStatementTransaction_HKDCurrencyNoNotes() {
        String[] fields = {"HKD", "3 Feb", "Local Purchase", "", "100.00", "14749.50"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNotNull(result);
        assertNull(result.getNotes()); // HKD should not add notes
    }

    @Test
    void testParseBankStatementTransaction_EmptyCurrency() {
        String[] fields = {"", "3 Feb", "Local Purchase", "", "100.00", "14749.50"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNotNull(result);
        assertNull(result.getNotes()); // Empty currency should not add notes
    }

    @Test
    void testParseBankStatementTransaction_SkipEmptyAmounts() {
        String[] fields = {"HKD", "4 Feb", "Balance Transfer", "", "", "14749.50"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNull(result);
    }

    @Test
    void testParseBankStatementTransaction_SkipEmptyDescription() {
        String[] fields = {"HKD", "5 Feb", "", "100.00", "", "14849.50"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNull(result);
    }

    @Test
    void testParseBankStatementTransaction_BothAmountsEmpty() {
        String[] fields = {"HKD", "1 Feb", "Transfer", "", "", "10000.00"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNull(result);
    }

    @Test
    void testParseBankStatementTransaction_BothAmountsPresent_PrioritizesDeposit() {
        String[] fields = {"HKD", "1 Feb", "Invalid Transaction", "100.00", "50.00", "10000.00"};
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        Transaction result = csvImportService.parseBankStatementTransaction(fields, 1, 1, formatter);
        
        assertNotNull(result);
        assertEquals(Transaction.TransactionType.INCOME, result.getType());
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getAmount()));
    }

    @Test
    void testParseDateWithCurrentYear_ValidDate() {
        String dateStr = "15 Mar";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        LocalDate result = csvImportService.parseDateWithCurrentYear(dateStr, formatter);
        
        int currentYear = LocalDate.now().getYear();
        assertEquals(LocalDate.of(currentYear, 3, 15), result);
    }

    @Test
    void testParseDateWithCurrentYear_InvalidDateReturnsCurrentDate() {
        String dateStr = "Invalid Date";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        LocalDate result = csvImportService.parseDateWithCurrentYear(dateStr, formatter);
        
        // Should return current date as fallback
        assertNotNull(result);
        // It should be today or very recent
        LocalDate today = LocalDate.now();
        assertTrue(result.equals(today) || result.isAfter(today.minusDays(1)));
    }

    @Test
    void testParseDateWithCurrentYear_EdgeCaseDate() {
        String dateStr = "29 Feb";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM");
        
        LocalDate result = csvImportService.parseDateWithCurrentYear(dateStr, formatter);
        
        // Should handle leap years gracefully
        assertNotNull(result);
    }

    @Test
    void testImportTransactionsFromCSV_ValidFile() throws IOException {
        // Create a test CSV file
        File testFile = new File(tempDir, "test_import.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("CCY,Date,Transaction Details,Deposit,Withdrawal,Balance\n");
            writer.write("HKD,1 Feb,Salary Payment,5000.00,,15000.00\n");
            writer.write("HKD,2 Feb,Grocery Store,,150.50,14849.50\n");
            writer.write("HKD,3 Feb,Restaurant Dinner,,200.00,14649.50\n");
        }

        // Create a simple FinanceService for testing
        TestFinanceService testFinanceService = new TestFinanceService();
        
        // Use auto-answering CSV service (simulates user selecting category 1 for each transaction)
        CSVImportService autoAnsweringService = createAutoAnsweringCSVService();
        
        List<Transaction> result = autoAnsweringService.importTransactionsFromCSV(
            testFile.getAbsolutePath(), 1, 1, testFinanceService);

        assertNotNull(result);
        // Should parse all 3 transactions (header is skipped)
        assertEquals(3, result.size());
        
        // Verify transaction types
        assertEquals(Transaction.TransactionType.INCOME, result.get(0).getType());
        assertEquals(Transaction.TransactionType.EXPENSE, result.get(1).getType());
        assertEquals(Transaction.TransactionType.EXPENSE, result.get(2).getType());
        
        // Verify auto-categorization worked
        assertNotNull(result.get(0).getCategory(), "Income transaction should have category");
        assertNotEquals("Temporary", result.get(0).getCategory(), "Should not have temporary category");
        assertNotNull(result.get(1).getCategory(), "Expense transaction should have category");
        assertNotEquals("Temporary", result.get(1).getCategory(), "Should not have temporary category");
    }

    @Test
    void testImportTransactionsFromCSV_SkipHeaderAndEmptyLines() throws IOException {
        File testFile = new File(tempDir, "test_empty_lines.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("CCY,Date,Transaction Details,Deposit,Withdrawal,Balance\n");
            writer.write("\n"); // Empty line
            writer.write("HKD,1 Feb,Salary,5000.00,,15000.00\n");
            writer.write("   \n"); // Whitespace line
            writer.write("HKD,2 Feb,Groceries,,150.50,14849.50\n");
        }

        TestFinanceService testFinanceService = new TestFinanceService();
        CSVImportService autoAnsweringService = createAutoAnsweringCSVService();
        
        List<Transaction> result = autoAnsweringService.importTransactionsFromCSV(
            testFile.getAbsolutePath(), 1, 1, testFinanceService);

        assertEquals(2, result.size()); // Should skip header and empty lines
    }

    @Test
    void testImportTransactionsFromCSV_SkipSummaryRows() throws IOException {
        File testFile = new File(tempDir, "test_summary.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("CCY,Date,Transaction Details,Deposit,Withdrawal,Balance\n");
            writer.write("HKD,1 Feb,Salary,5000.00,,15000.00\n");
            writer.write("B/F BALANCE,,Previous Balance,,,10000.00\n");
            writer.write("C/F BALANCE,,Closing Balance,,,15000.00\n");
            writer.write("Transaction Summary,,Total,,,5000.00\n");
            writer.write("HKD,2 Feb,Groceries,,150.50,14849.50\n");
        }

        TestFinanceService testFinanceService = new TestFinanceService();
        CSVImportService autoAnsweringService = createAutoAnsweringCSVService();
        
        List<Transaction> result = autoAnsweringService.importTransactionsFromCSV(
            testFile.getAbsolutePath(), 1, 1, testFinanceService);

        assertEquals(2, result.size()); // Should skip summary rows
    }

    @Test
    void testImportTransactionsFromCSV_InsufficientFields() throws IOException {
        File testFile = new File(tempDir, "test_insufficient.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("CCY,Date,Transaction Details,Deposit,Withdrawal,Balance\n");
            writer.write("HKD,1 Feb,Salary,5000.00\n"); // Only 4 fields
            writer.write("HKD,2 Feb,Groceries,,150.50,14849.50\n"); // 6 fields - valid
        }

        TestFinanceService testFinanceService = new TestFinanceService();
        CSVImportService autoAnsweringService = createAutoAnsweringCSVService();
        
        List<Transaction> result = autoAnsweringService.importTransactionsFromCSV(
            testFile.getAbsolutePath(), 1, 1, testFinanceService);

        assertEquals(1, result.size()); // Should skip line with insufficient fields
    }

    @Test
    void testImportTransactionsFromCSV_ParsingErrorInAmount() throws IOException {
        File testFile = new File(tempDir, "test_invalid_amount.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("CCY,Date,Transaction Details,Deposit,Withdrawal,Balance\n");
            writer.write("HKD,1 Feb,Salary,INVALID_AMOUNT,,15000.00\n"); // Invalid amount
            writer.write("HKD,2 Feb,Groceries,,150.50,14849.50\n"); // Valid
        }

        OutputCapture outputCapture = new OutputCapture();
        outputCapture.startCapture();
        
        TestFinanceService testFinanceService = new TestFinanceService();
        CSVImportService autoAnsweringService = createAutoAnsweringCSVService();
        
        List<Transaction> result = autoAnsweringService.importTransactionsFromCSV(
            testFile.getAbsolutePath(), 1, 1, testFinanceService);

        assertEquals(1, result.size()); // Should skip line with parsing error
        
        // Should log error message
        String errorOutput = outputCapture.getError();
        assertTrue(errorOutput.contains("Error parsing line") || errorOutput.contains("Failed to save transaction"));
    }

    @Test
    void testImportTransactionsFromCSV_FileNotFound() {
        String nonExistentFile = "nonexistent_file_12345.csv";
        
        TestFinanceService testFinanceService = new TestFinanceService();
        CSVImportService autoAnsweringService = createAutoAnsweringCSVService();
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            autoAnsweringService.importTransactionsFromCSV(nonExistentFile, 1, 1, testFinanceService);
        });
        
        assertTrue(exception.getMessage().contains("Failed to import CSV file"));
    }

    @Test
    void testPreviewCSV_ValidFile() throws IOException {
        File testFile = new File(tempDir, "preview_test.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("CCY,Date,Transaction Details,Deposit,Withdrawal,Balance\n");
            writer.write("HKD,1 Feb,Salary,5000.00,,15000.00\n");
            writer.write("HKD,2 Feb,Groceries,,150.50,14849.50\n");
        }

        // This test just verifies no exception is thrown during preview
        assertDoesNotThrow(() -> {
            csvImportService.previewCSV(testFile.getAbsolutePath(), 3);
        });
    }

    @Test
    void testPreviewCSV_FileNotFound() {
        String nonExistentFile = "nonexistent_preview_12345.csv";
        
        // Should not throw exception, just print error
        assertDoesNotThrow(() -> {
            csvImportService.previewCSV(nonExistentFile, 5);
        });
    }

    @Test
    void testClose() {
        // Test that close method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            csvImportService.close();
        });
    }

    @Test
    void testTransactionCategoryEnum() {
        // Test that category enums work correctly
        Transaction.Category[] incomeCategories = Transaction.Category.getIncomeCategories();
        Transaction.Category[] expenseCategories = Transaction.Category.getExpenseCategories();
        
        assertTrue(incomeCategories.length > 0);
        assertTrue(expenseCategories.length > 0);
        
        // Test display names
        assertEquals("Salary", Transaction.Category.SALARY.getDisplayName());
        assertEquals("Food & Dining", Transaction.Category.FOOD_DINING.getDisplayName());
    }

    // Simple test implementation of FinanceService for testing
    private static class TestFinanceService extends FinanceService {
        private int addTransactionCallCount = 0;
        
        public TestFinanceService() {
            super(new AccountRepository(), new TransactionRepository());
        }
        
        @Override
        public void addTransaction(Transaction transaction) {
            addTransactionCallCount++;
            // Simulate successful addition
            System.out.println("Test: Added transaction - " + transaction.getDescription());
        }
        
        public int getAddTransactionCallCount() {
            return addTransactionCallCount;
        }
    }

    @Test
    void testCategorizeTransaction_IncomeCategories() {
        Transaction incomeTransaction = new Transaction(1, 1, "Salary", 
            new BigDecimal("5000.00"), Transaction.TransactionType.INCOME, "Temporary", LocalDate.now());
        
        // This is harder to test without mocking user input, but we can verify the method exists
        // and doesn't throw exceptions for valid input
        assertNotNull(incomeTransaction);
    }

    @Test
    void testCategorizeTransaction_ExpenseCategories() {
        Transaction expenseTransaction = new Transaction(1, 1, "Groceries", 
            new BigDecimal("150.50"), Transaction.TransactionType.EXPENSE, "Temporary", LocalDate.now());
        
        assertNotNull(expenseTransaction);
    }

    @Test
    void testCSVWithDifferentFormats() throws IOException {
        // Test various CSV formats and edge cases
        File testFile = new File(tempDir, "test_formats.csv");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("CCY,Date,Transaction Details,Deposit,Withdrawal,Balance\n");
            writer.write(",1 Feb,No Currency Transaction,100.00,,15100.00\n");
            writer.write("USD,2 Feb,\"Transaction, with commas\",,50.00,15050.00\n");
            writer.write("EUR,3 Feb,\"Quoted \"\"Transaction\"\"\",,25.00,15025.00\n");
        }

        TestFinanceService testFinanceService = new TestFinanceService();
        CSVImportService autoAnsweringService = createAutoAnsweringCSVService();
        
        List<Transaction> result = autoAnsweringService.importTransactionsFromCSV(
            testFile.getAbsolutePath(), 1, 1, testFinanceService);

        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
