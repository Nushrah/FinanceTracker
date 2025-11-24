package financeTracker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVImportService {
    private final TransactionRepository transactionRepository;
    private final Scanner scanner;
    
    public CSVImportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.scanner = new Scanner(System.in);
    }
    
    public List<Transaction> importTransactionsFromCSV(String filePath, int accountId, int userId, FinanceService financeService) {
        List<Transaction> importedTransactions = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM");
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                // Skip header row and empty lines
                if (lineNumber == 1 || line.trim().isEmpty()) {
                    continue;
                }
                
                // Skip summary rows and balance rows
                if (line.contains("B/F BALANCE") || line.contains("C/F BALANCE") || 
                    line.contains("Transaction Summary")) {
                    continue;
                }
                
                String[] fields = parseCSVLine(line);
                if (fields.length >= 6) {
                    try {
                        Transaction transaction = parseBankStatementTransaction(fields, accountId, userId, formatter);
                        if (transaction != null) {
                            // Let user categorize this transaction
                            categorizeTransaction(transaction);
                            importedTransactions.add(transaction);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing line " + lineNumber + ": " + e.getMessage());
                        System.err.println("Line content: " + line);
                    }
                } else {
                    System.err.println("Skipping line " + lineNumber + " - insufficient fields: " + line);
                }
            }
            
            // Save all valid transactions to database
            int savedCount = 0;
            for (Transaction transaction : importedTransactions) {
                try {
                    financeService.addTransaction(transaction);
                    savedCount++;
                } catch (Exception e) {
                    System.err.println("Failed to save transaction: " + transaction.getDescription() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("✓ Successfully imported " + savedCount + " transactions out of " + importedTransactions.size() + " parsed");
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to import CSV file: " + e.getMessage(), e);
        }
        
        return importedTransactions;
    }
    
    /**
     * Let user categorize each transaction based on its type (income/expense)
     */
    private void categorizeTransaction(Transaction transaction) {
        System.out.println("\n--- CATEGORIZE TRANSACTION ---");
        System.out.println("Description: " + transaction.getDescription());
        System.out.println("Amount: " + transaction.getAmount() + " (" + transaction.getType() + ")");
        System.out.println("Date: " + transaction.getDate());
        
        // Use the appropriate categories based on transaction type
        Transaction.Category[] categories;
        if (transaction.getType() == Transaction.TransactionType.INCOME) {
            categories = Transaction.Category.getIncomeCategories();
            System.out.println("\n--- INCOME CATEGORIES ---");
        } else {
            categories = Transaction.Category.getExpenseCategories();
            System.out.println("\n--- EXPENSE CATEGORIES ---");
        }
        
        // Display categories with numbers
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ". " + categories[i].getDisplayName());
        }
        
        int choice = 0;
        while (choice < 1 || choice > categories.length) {
            System.out.print("Enter category (1-" + categories.length + "): ");
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > categories.length) {
                    System.out.println("Please enter a number between 1 and " + categories.length + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        
        String category = categories[choice - 1].getDisplayName();
        transaction.setCategory(category);
        
        System.out.println("✓ Categorized as: " + category);
    }
    
    /**
     * Custom CSV parser that handles the specific format with optional fields
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        fields.add(currentField.toString().trim());
        
        return fields.toArray(new String[0]);
    }
    
    private Transaction parseBankStatementTransaction(String[] fields, int accountId, int userId, DateTimeFormatter formatter) {
        // Field mapping for bank statement format:
        // [0] = CCY (currency - may be empty for HKD)
        // [1] = Date (e.g., "1 Feb", "7 Feb")
        // [2] = Transaction Details
        // [3] = Deposit amount (may be empty)
        // [4] = Withdrawal amount (may be empty) 
        // [5] = Balance
        
        String currency = fields[0].trim();
        String dateStr = fields[1].trim();
        String description = fields[2].trim();
        String depositStr = fields[3].trim();
        String withdrawalStr = fields[4].trim();
        
        // Skip rows without actual transaction amounts
        if ((depositStr.isEmpty() && withdrawalStr.isEmpty()) || description.isEmpty()) {
            return null;
        }
        
        // Parse date - assuming current year and adding year to the date
        LocalDate date = parseDateWithCurrentYear(dateStr, formatter);
        
        // Determine amount and transaction type
        BigDecimal amount;
        Transaction.TransactionType type;
        
        if (!depositStr.isEmpty()) {
            // This is an INCOME transaction (deposit)
            amount = new BigDecimal(depositStr);
            type = Transaction.TransactionType.INCOME;
        } else {
            // This is an EXPENSE transaction (withdrawal)
            amount = new BigDecimal(withdrawalStr);
            type = Transaction.TransactionType.EXPENSE;
        }
        
        
        Transaction transaction = new Transaction(userId, accountId, description, amount, type, "Temporary", date);
        
        // Add notes with original currency if different from account currency
        if (!currency.isEmpty() && !currency.equals("HKD")) {
            transaction.setNotes("Original currency: " + currency);
        }
        
        return transaction;
    }
    
    /**
     * Parse date string like "1 Feb", "7 Feb" by adding current year
     */
    private LocalDate parseDateWithCurrentYear(String dateStr, DateTimeFormatter formatter) {
        try {
            // Add current year to the date string
            int currentYear = LocalDate.now().getYear();
            String fullDateStr = dateStr + " " + currentYear;
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("d MMM yyyy");
            return LocalDate.parse(fullDateStr, fullFormatter);
        } catch (Exception e) {
            System.err.println("Failed to parse date: " + dateStr + ", using current date");
            return LocalDate.now();
        }
    }
    
    /**
     * Preview CSV file without importing - useful for debugging
     */
    public void previewCSV(String filePath, int maxLines) {
        System.out.println("=== CSV FILE PREVIEW ===");
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null && lineNumber < maxLines) {
                lineNumber++;
                System.out.printf("Line %2d: %s%n", lineNumber, line);
                
                if (lineNumber == 1) {
                    // Show header analysis
                    String[] headers = parseCSVLine(line);
                    System.out.println("Detected headers: " + String.join(" | ", headers));
                }
            }
            
            if (lineNumber >= maxLines) {
                System.out.println("... (showing first " + maxLines + " lines)");
            }
            
        } catch (IOException e) {
            System.err.println("Failed to preview CSV file: " + e.getMessage());
        }
    }
    
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}