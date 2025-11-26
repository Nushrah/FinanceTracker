package financeTracker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static FinanceService financeService;
    private static AuthService authService;
    
    public static void main(String[] args) {
        initializeServices();
        displayWelcomeMessage();
        
        // Authentication flow
        if (!handleAuthentication()) {
            System.out.println("Authentication failed. Exiting...");
            return;
        }
        
        // Main application loop
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1 -> addAccount();
                case 2 -> addTransaction();
                case 3 -> viewAccounts();
                case 4 -> viewTransactions();
                case 5 -> viewFinancialMetrics();
                case 6 -> importCSVTransactions();
                case 7 -> getFinancialRecommendation();
                case 8 -> viewTotalNetWorth();
                case 9 -> changePassword();
                case 0 -> {
                    running = false;
                    System.out.println("Thank you for using Finance Tracker!");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
    }
    
    private static void initializeServices() {
        AccountRepository accountRepository = new AccountRepository();
        TransactionRepository transactionRepository = new TransactionRepository();
        financeService = new FinanceService(accountRepository, transactionRepository);
        authService = new AuthService();
    }
    
    private static boolean handleAuthentication() {
        System.out.println("\n=== FINANCE TRACKER LOGIN ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        
        int choice = getIntInput("Choose option: ");
        
        switch (choice) {
            case 1 -> {
                return handleLogin();
            }
            case 2 -> {
                return handleRegistration();
            }
            case 3 -> {
                return false;
            }
            default -> {
                System.out.println("Invalid choice.");
                return false;
            }
        }
    }
    
    private static boolean handleLogin() {
        System.out.println("\n--- LOGIN ---");
        final int MAX_ATTEMPTS = 3;
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            User user = authService.login(username, password);
            if (user != null) {
                System.out.println("✓ Login successful! Welcome, " + user.getUsername());
                return true;
            } else {
                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;
                
                if (remaining > 0) {
                    System.out.println("✗ Login failed. Invalid username or password.");
                    System.out.println("Attempts remaining: " + remaining);
                    System.out.println("Please try again.\n");
                } else {
                    System.out.println("✗ Maximum login attempts (" + MAX_ATTEMPTS + ") exceeded.");
                    System.out.println("For security reasons, the application will now exit.");
                    return false;
                }
            }
        }
        return false;
    }
    
    private static boolean handleRegistration() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        
        System.out.print("Password (min 6 characters): ");
        String password = scanner.nextLine();
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        System.out.println("Available currencies: USD, HKD, EUR, CNY, SGD");
        System.out.print("Base currency: ");
        String currency = scanner.nextLine().toUpperCase();
        
        try {
            boolean success = authService.register(username, password, email, currency);
            if (success) {
                System.out.println("✓ Registration successful! Please login.");
                return handleLogin();
            } else {
                System.out.println("✗ Registration failed.");
                return false;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Registration error: " + e.getMessage());
            return false;
        }
    }
    
    private static void changePassword() {
        System.out.println("\n--- CHANGE PASSWORD ---");
        
        System.out.print("Current password: ");
        String currentPassword = scanner.nextLine();
        
        System.out.print("New password (min 6 characters): ");
        String newPassword = scanner.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("✗ New passwords do not match.");
            return;
        }
        
        if (newPassword.length() < 6) {
            System.out.println("✗ New password must be at least 6 characters long.");
            return;
        }
        
        boolean success = authService.changePassword(currentPassword, newPassword);
        if (success) {
            System.out.println("✓ Password changed successfully!");
        } else {
            System.out.println("✗ Failed to change password. Please check your current password.");
        }
    }
    
    private static void displayWelcomeMessage() {
        System.out.println("=========================================");
        System.out.println("      PERSONAL FINANCE TRACKER");
        System.out.println("=========================================");
    }
    
    private static void displayMainMenu() {
        User currentUser = authService.getCurrentUser();
        System.out.println("\n--- MAIN MENU (User: " + currentUser.getUsername() + ") ---");
        System.out.println("1. Add New Account");
        System.out.println("2. Add Transaction");
        System.out.println("3. View All Accounts");
        System.out.println("4. View Account Transactions");
        System.out.println("5. View Financial Metrics");
        System.out.println("6. Import CSV Transactions");
        System.out.println("7. Get Financial Recommendation");
        System.out.println("8. View Total Net Worth");
        System.out.println("9. Change Password");
        System.out.println("0. Exit");
        System.out.println("=========================================");
    }
    
    private static void addAccount() {
        System.out.println("\n--- ADD NEW ACCOUNT ---");
        
        System.out.print("Enter account name: ");
        String name = scanner.nextLine();
        
        System.out.println("Available account types:");
        for (Account.AccountType type : Account.AccountType.values()) {
            System.out.println("- " + type);
        }
        System.out.print("Enter account type: ");
        String typeStr = scanner.nextLine().toUpperCase();
        
        System.out.println("Available currencies: USD, HKD, EUR, CNY, SGD");
        System.out.print("Enter currency code: ");
        String currencyCode = scanner.nextLine().toUpperCase();
        
        BigDecimal balance = getBigDecimalInput("Enter initial balance: ");
        
        try {
            Account.AccountType type = Account.AccountType.valueOf(typeStr);
            Currency currency = Currency.getInstance(currencyCode);
            
            User currentUser = authService.getCurrentUser();
            Account account = new Account(currentUser.getId(), name, type, balance, currency);
            financeService.addAccount(account);
            
            System.out.println("✓ Account added successfully: " + account);
        } catch (Exception e) {
            System.out.println("Error adding account: " + e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    private static void importCSVTransactions() {
        System.out.println("\n--- IMPORT CSV TRANSACTIONS ---");
        
        viewAccounts();
        int accountId = getIntInput("Enter account ID for import: ");
        
        System.out.print("Enter CSV file path (e.g., bankstatement.csv): ");
        String filePath = scanner.nextLine();
        
        // Preview the CSV first
        try {
            CSVImportService importService = new CSVImportService(new TransactionRepository());
            System.out.println("\nPreviewing CSV file...");
            importService.previewCSV(filePath, 10);
            
            System.out.print("\nDo you want to import these transactions? (y/n): ");
            String confirm = scanner.nextLine();
            
            if (confirm.equalsIgnoreCase("y")) {
                User currentUser = authService.getCurrentUser();
                List<Transaction> imported = importService.importTransactionsFromCSV(
                    filePath, accountId, currentUser.getId(), financeService);
                
                System.out.println("✓ Successfully processed " + imported.size() + " transactions");
                
                Account updatedAccount = financeService.getAccountById(accountId);
                if (updatedAccount != null) {
                    System.out.println("✓ Account '" + updatedAccount.getName() + "' updated balance: " + 
                                     updatedAccount.getBalance() + " " + updatedAccount.getCurrency().getCurrencyCode());
                }
            } else {
                System.out.println("Import cancelled.");
            }
            
        } catch (Exception e) {
            System.out.println("Error importing CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void addTransaction() {
        System.out.println("\n--- ADD TRANSACTION ---");
        
        viewAccounts();
        int accountId = getIntInput("Enter account ID: ");
        
        System.out.print("Enter description: ");
        String description = scanner.nextLine();
        
        BigDecimal amount = getBigDecimalInput("Enter amount: ");
        
        System.out.println("Transaction types: INCOME, EXPENSE");
        System.out.print("Enter transaction type: ");
        String typeStr = scanner.nextLine().toUpperCase();
        
        try {
            Transaction.TransactionType type = Transaction.TransactionType.valueOf(typeStr);
            
            // GET CATEGORY BASED ON TRANSACTION TYPE
            String category = getCategoryFromUser(type);
            if (category == null) {
                System.out.println("Transaction cancelled.");
                return;
            }
            
            Transaction transaction = new Transaction(authService.getCurrentUser().getId(), accountId, description, amount, type, category, LocalDate.now());
            
            financeService.addTransaction(transaction);
            System.out.println("✓ Transaction added successfully: " + transaction);
        } catch (Exception e) {
            System.out.println("Error adding transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getCategoryFromUser(Transaction.TransactionType type) {
        System.out.println("\nPlease select a category:");
        
        Transaction.Category[] categories;
        if (type == Transaction.TransactionType.INCOME) {
            categories = Transaction.Category.getIncomeCategories();
            System.out.println("--- INCOME CATEGORIES ---");
        } else {
            categories = Transaction.Category.getExpenseCategories();
            System.out.println("--- EXPENSE CATEGORIES ---");
        }
        
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
        
        return categories[choice - 1].getDisplayName();
    }
    
    private static void viewAccounts() {
        System.out.println("\n--- ALL ACCOUNTS ---");
        
        try {
            User currentUser = authService.getCurrentUser();
            List<Account> accounts = financeService.getAccountsByUserId(currentUser.getId());
            
            if (accounts.isEmpty()) {
                System.out.println("No accounts found.");
            } else {
                for (Account account : accounts) {
                    System.out.println(account);
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving accounts: " + e.getMessage());
        }
        
    }

    
    private static void viewTransactions() {
        System.out.println("\n--- ACCOUNT TRANSACTIONS ---");
        viewAccounts();
        
        int accountId = getIntInput("Enter account ID to view transactions: ");
        List<Transaction> transactions = financeService.getAccountTransactions(accountId);
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for this account.");
        } else {
            System.out.println("\nTransactions:");
            for (Transaction transaction : transactions) {
                System.out.println(transaction);
            }
        }
    }
    
    private static void viewFinancialMetrics() {
        System.out.println("\n--- FINANCIAL METRICS ---");
        
        int year = getIntInput("Enter year: ");
        int month = getIntInput("Enter month (1-12): ");
        
        try {
            FinancialMetrics metrics = financeService.calculateMonthlyMetrics(year, month);
            System.out.println("\nFinancial Metrics for " + month + "/" + year + ":");
            System.out.println(metrics);
            
            // Percentage breakdown of category wise spending 
            try {
                ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(year, month);
                System.out.println();
                System.out.println(breakdown.toString());
            } catch (Exception ex) {
                System.out.println("Error calculating expense breakdown: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error calculating metrics: " + e.getMessage());
        }
    }
    
    private static void getFinancialRecommendation() {
        System.out.println("\n--- FINANCIAL RECOMMENDATION ---");
        
        int year = getIntInput("Enter year: ");
        int month = getIntInput("Enter month (1-12): ");
        
        try {
            String recommendation = financeService.getFinancialRecommendation(year, month);
            System.out.println("\n💡 Financial Recommendation:");
            System.out.println(recommendation);
        } catch (Exception e) {
            System.out.println("Error generating recommendation: " + e.getMessage());
        }
    }
    
    private static void viewTotalNetWorth() {
        System.out.println("\n--- TOTAL NET WORTH ---");
        
        System.out.println("Available currencies: USD, HKD, EUR, CNY, SGD");
        System.out.print("Enter currency code for net worth calculation: ");
        String currencyCode = scanner.nextLine().toUpperCase();
        
        try {
            Currency currency = Currency.getInstance(currencyCode);
            BigDecimal netWorth = financeService.getTotalNetWorth(currency);
            
            System.out.printf("Total Net Worth: %.2f %s%n", netWorth, currency.getCurrencyCode());
        } catch (Exception e) {
            System.out.println("Error calculating net worth: " + e.getMessage());
        }
    }
    
    
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    private static BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount (e.g., 100.50).");
            }
        }
    }
}