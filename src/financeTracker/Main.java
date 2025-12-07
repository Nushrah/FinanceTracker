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
            System.out.println("Exiting...");
            return;
        }
        
        // Main application loop
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1 -> {
                    addAccount();
                    DisplayUtils.pressEnterToContinue();
                }
                case 2 -> {
                    viewAccounts();
                    DisplayUtils.pressEnterToContinue();
                }
                case 3 -> {
                    addTransaction();
                    DisplayUtils.pressEnterToContinue();
                }
                case 4 -> {
                    viewTransactions();
                    DisplayUtils.pressEnterToContinue();
                }
                case 5 -> {
                    importCSVTransactions();
                    DisplayUtils.pressEnterToContinue();
                }
                case 6 -> {
                    viewFinancialMetrics();
                    DisplayUtils.pressEnterToContinue();
                }
                case 7 -> {
                    getFinancialRecommendation();
                    DisplayUtils.pressEnterToContinue();
                }
                case 8 -> {
                    viewTotalNetWorth();
                    DisplayUtils.pressEnterToContinue();
                }
                case 9 -> {
                    changePassword();
                    DisplayUtils.pressEnterToContinue();
                }
                case 0 -> {
                    running = false;
                    DisplayUtils.printSuccess("Thank you for using Finance Tracker!");
                }
                default -> {
                    DisplayUtils.printError("Invalid choice. Please try again.");
                    DisplayUtils.pressEnterToContinue();
                }
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
        DisplayUtils.printHeader("WELCOME TO FINANCE TRACKER");
        
        System.out.println(DisplayUtils.GREEN + "✨ Manage your finances with ease and precision!" + DisplayUtils.RESET);
    System.out.println();
    
    System.out.println(DisplayUtils.YELLOW + "┌─────────────────────────────────────────────────────────────┐");
    System.out.println("│" + DisplayUtils.centerText(DisplayUtils.BOLD + "GET STARTED" + DisplayUtils.RESET + DisplayUtils.YELLOW, 74) + "│");
    System.out.println("├─────────────────────────────────────────────────────────────┤");
    System.out.println("│   1. " + DisplayUtils.GREEN + "➤ Login to Existing Account" + DisplayUtils.RESET + DisplayUtils.YELLOW + "                            │");
    System.out.println("│   2. " + DisplayUtils.CYAN + "⭐ Create New Account" + DisplayUtils.RESET + DisplayUtils.YELLOW + "                                  │");
    System.out.println("│   3. " + DisplayUtils.RED + "🚪 Exit Application" + DisplayUtils.RESET + DisplayUtils.YELLOW + "                                    │");
    System.out.println("└─────────────────────────────────────────────────────────────┘" + DisplayUtils.RESET);
    System.out.println();
        
        int choice = getIntInput(DisplayUtils.BOLD + "Choose an option (1-3): " + DisplayUtils.RESET);
        
        switch (choice) {
            case 1 -> {
                return handleLogin();
            }
            case 2 -> {
                return handleRegistration();
            }
            case 3 -> {
                DisplayUtils.printSuccess("Thank you for visiting! Goodbye! 👋");
                return false;
            }
            default -> {
                DisplayUtils.printError("Invalid choice. Please select 1, 2, or 3.");
                DisplayUtils.pressEnterToContinue();
                return handleAuthentication(); // Recursive call to show menu again
            }
        }
    }
    
    private static boolean handleLogin() {
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("LOGIN");
        
        final int MAX_ATTEMPTS = 3;
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("👤 Username: ");
            String username = scanner.nextLine();
            
            System.out.print("🔒 Password: ");
            String password = scanner.nextLine();
            
            User user = authService.login(username, password);
            if (user != null) {
                DisplayUtils.printSuccess("Login successful! Welcome, " + user.getUsername());
                return true;
            } else {
                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;
                
                if (remaining > 0) {
                    DisplayUtils.printError("Login failed. Invalid username or password.");
                    System.out.println(DisplayUtils.YELLOW + "Attempts remaining: " + remaining + DisplayUtils.RESET);
                    System.out.println();
                } else {
                    DisplayUtils.printError("Maximum login attempts (" + MAX_ATTEMPTS + ") exceeded.");
                    DisplayUtils.printError("For security reasons, the application will now exit.");
                    return false;
                }
            }
        }
        return false;
    }
    
    private static boolean handleRegistration() {
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("CREATE YOUR ACCOUNT");
        
        System.out.println(DisplayUtils.CYAN + "Join thousands of users managing their finances smarter! 🚀" + DisplayUtils.RESET);
        System.out.println();
        
        try {
            // Username
            System.out.print(DisplayUtils.BOLD + "👤 Username: " + DisplayUtils.RESET);
            String username = scanner.nextLine();
            
            // Password with validation
            String password;
            while (true) {
                System.out.print(DisplayUtils.BOLD + "🔒 Password (min 6 characters): " + DisplayUtils.RESET);
                password = scanner.nextLine();
                
                if (password.length() >= 6) {
                    break;
                } else {
                    DisplayUtils.printError("Password must be at least 6 characters long. Please try again.");
                }
            }
            
            // Confirm Password
            String confirmPassword;
            while (true) {
                System.out.print(DisplayUtils.BOLD + "🔒 Confirm Password: " + DisplayUtils.RESET);
                confirmPassword = scanner.nextLine();
                
                if (password.equals(confirmPassword)) {
                    break;
                } else {
                    DisplayUtils.printError("Passwords do not match. Please try again.");
                }
            }
            
            // Email
            System.out.print(DisplayUtils.BOLD + "📧 Email: " + DisplayUtils.RESET);
            String email = scanner.nextLine();
            
            // Currency selection with nice display
            DisplayUtils.printSection("SELECT BASE CURRENCY");
            System.out.println(DisplayUtils.BOLD + "Available Currencies:" + DisplayUtils.RESET);
            System.out.println("💵 USD - US Dollar");
            System.out.println("💶 EUR - Euro");
            System.out.println("💴 CNY - Chinese Yuan");
            System.out.println("💸 HKD - Hong Kong Dollar");
            System.out.println("💷 SGD - Singapore Dollar");
            System.out.println();
            
            String currency;
            while (true) {
                System.out.print(DisplayUtils.BOLD + "Enter currency code (e.g., USD): " + DisplayUtils.RESET);
                currency = scanner.nextLine().trim().toUpperCase();
                
                // Validate currency
                if (currency.matches("USD|HKD|EUR|CNY|SGD")) {
                    break;
                } else {
                    DisplayUtils.printError("Invalid currency. Please choose from: USD, HKD, EUR, CNY, SGD");
                }
            }
            
            // Summary before registration
            DisplayUtils.printSection("ACCOUNT SUMMARY");
            System.out.println(DisplayUtils.BOLD + "Username: " + DisplayUtils.RESET + username);
            System.out.println(DisplayUtils.BOLD + "Email: " + DisplayUtils.RESET + email);
            System.out.println(DisplayUtils.BOLD + "Base Currency: " + DisplayUtils.RESET + currency);
            System.out.println();
            
            System.out.print(DisplayUtils.YELLOW + "Create this account? (y/n): " + DisplayUtils.RESET);
            String confirmation = scanner.nextLine();
            
            if (!confirmation.equalsIgnoreCase("y")) {
                DisplayUtils.printWarning("Registration cancelled.");
                DisplayUtils.pressEnterToContinue();
                return handleAuthentication();
            }
            
            // Attempt registration
            DisplayUtils.printSection("CREATING ACCOUNT");
            System.out.print("Setting up your account");
            
            // Simple loading animation
            for (int i = 0; i < 3; i++) {
                System.out.print(".");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // Continue if interrupted
                }
            }
            System.out.println();
            
            boolean success = authService.register(username, password, email, currency);
            
            if (success) {
                DisplayUtils.printSuccess("🎉 Registration successful! Welcome to Finance Tracker!");
                System.out.println();
                DisplayUtils.printSuccess("Redirecting to login...");
                
                // Small delay before login
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    // Continue if interrupted
                }
                
                return handleLogin();
            } else {
                DisplayUtils.printError("Registration failed. Please try again.");
                DisplayUtils.pressEnterToContinue();
                return handleRegistration(); // Retry registration
            }
            
        } catch (IllegalArgumentException e) {
            DisplayUtils.printError("Registration error: " + e.getMessage());
            DisplayUtils.pressEnterToContinue();
            return handleRegistration(); // Retry on error
        }
    }
    
    private static void changePassword() {
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("CHANGE PASSWORD");
        
        System.out.println(DisplayUtils.YELLOW + "🔒 Security Recommendation: Use a strong, unique password" + DisplayUtils.RESET);
        System.out.println();
        
        try {
            // Current Password
            System.out.print(DisplayUtils.BOLD + "🔐 Current Password: " + DisplayUtils.RESET);
            String currentPassword = scanner.nextLine();
            
            // Validate current password isn't empty
            if (currentPassword.trim().isEmpty()) {
                DisplayUtils.printError("Current password cannot be empty.");
                DisplayUtils.pressEnterToContinue();
                return;
            }
            
            DisplayUtils.printSeparator();
            
            // New Password with strength checking
            String newPassword;
            while (true) {
                System.out.println(DisplayUtils.BOLD + "Create New Password" + DisplayUtils.RESET);
                System.out.println(DisplayUtils.CYAN + "Requirements:" + DisplayUtils.RESET);
                System.out.println("  • At least 6 characters long");
                System.out.println("  • Include letters and numbers (recommended)");
                System.out.println("  • Avoid common words (recommended)");
                System.out.println();
                
                System.out.print(DisplayUtils.BOLD + "🆕 New Password: " + DisplayUtils.RESET);
                newPassword = scanner.nextLine();
                
                // Basic password validation
                if (newPassword.length() < 6) {
                    DisplayUtils.printError("Password must be at least 6 characters long.");
                    System.out.println();
                    continue;
                }
                
                // Optional: Check if password is too common
                if (isCommonPassword(newPassword)) {
                    System.out.print(DisplayUtils.YELLOW + "⚠ This password is commonly used. Are you sure? (y/n): " + DisplayUtils.RESET);
                    String confirm = scanner.nextLine();
                    if (!confirm.equalsIgnoreCase("y")) {
                        System.out.println();
                        continue;
                    }
                }
                
                // Show password strength
                String strength = checkPasswordStrength(newPassword);
                System.out.println(DisplayUtils.BOLD + "Password Strength: " + strength + DisplayUtils.RESET);
                System.out.println();
                break;
            }
            
            // Confirm New Password
            String confirmPassword;
            while (true) {
                System.out.print(DisplayUtils.BOLD + "✅ Confirm New Password: " + DisplayUtils.RESET);
                confirmPassword = scanner.nextLine();
                
                if (newPassword.equals(confirmPassword)) {
                    break;
                } else {
                    DisplayUtils.printError("Passwords do not match. Please try again.");
                    System.out.println();
                }
            }
            
            DisplayUtils.printSeparator();
            
            // Summary before changing
            DisplayUtils.printSection("PASSWORD CHANGE SUMMARY");
            System.out.println(DisplayUtils.BOLD + "New Password Strength: " + checkPasswordStrength(newPassword) + DisplayUtils.RESET);
            System.out.println(DisplayUtils.BOLD + "Length: " + DisplayUtils.RESET + newPassword.length() + " characters");
            System.out.println();
            
            System.out.print(DisplayUtils.YELLOW + "Are you sure you want to change your password? (y/n): " + DisplayUtils.RESET);
            String confirmation = scanner.nextLine();
            
            if (!confirmation.equalsIgnoreCase("y")) {
                DisplayUtils.printWarning("Password change cancelled.");
                DisplayUtils.pressEnterToContinue();
                return;
            }
            
            // Attempt to change password
            DisplayUtils.printSection("UPDATING PASSWORD");
            System.out.print("Securely updating your password");
            
            // Loading animation
            for (int i = 0; i < 3; i++) {
                System.out.print(".");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // Continue if interrupted
                }
            }
            System.out.println();
            
            boolean success = authService.changePassword(currentPassword, newPassword);
            
            if (success) {
                DisplayUtils.printSuccess("🎉 Password changed successfully!");
                System.out.println();
                DisplayUtils.printSuccess("Your new password is now active.");
                
                // Security reminder
                System.out.println();
                DisplayUtils.printSection("SECURITY REMINDER");
                System.out.println(DisplayUtils.YELLOW + "• Don't share your password with anyone");
                System.out.println("• Use different passwords for different services");
                System.out.println("• Consider using a password manager");
                System.out.println("• Update your password regularly" + DisplayUtils.RESET);
                
            } else {
                DisplayUtils.printError("❌ Failed to change password.");
                DisplayUtils.printError("Please check your current password and try again.");
            }
            
        } catch (Exception e) {
            DisplayUtils.printError("An error occurred while changing password: " + e.getMessage());
        }
        
        DisplayUtils.pressEnterToContinue();
    }
    
    // Helper method to check password strength
    private static String checkPasswordStrength(String password) {
        if (password.length() < 6) {
            return DisplayUtils.RED + "Very Weak" + DisplayUtils.RESET;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        int strength = 0;
        if (hasLetter) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        
        switch (strength) {
            case 0:
            case 1:
                return DisplayUtils.RED + "Weak" + DisplayUtils.RESET;
            case 2:
            case 3:
                return DisplayUtils.YELLOW + "Moderate" + DisplayUtils.RESET;
            case 4:
                return DisplayUtils.GREEN + "Strong" + DisplayUtils.RESET;
            case 5:
                return DisplayUtils.GREEN + "Very Strong" + DisplayUtils.RESET;
            default:
                return DisplayUtils.YELLOW + "Moderate" + DisplayUtils.RESET;
        }
    }
    
    // Helper method to check for common passwords (basic implementation)
    private static boolean isCommonPassword(String password) {
        String[] commonPasswords = {
            "password", "123456", "12345678", "1234", "qwerty", 
            "abc123", "password1", "admin", "welcome", "monkey"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common) || common.equals(lowerPassword)) {
                return true;
            }
        }
        return false;
    }
    
    private static void displayWelcomeMessage() {
        DisplayUtils.clearScreen();
        System.out.println(DisplayUtils.CYAN + "╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║" + DisplayUtils.centerText("", 62) + "║");
        System.out.println("║" + DisplayUtils.centerText("💰 PERSONAL FINANCE TRACKER", 62) + "║");
        System.out.println("║" + DisplayUtils.centerText("", 62) + "║");
        System.out.println("║" + DisplayUtils.centerText("Take Control of Your Financial Future", 62) + "║");
        System.out.println("║" + DisplayUtils.centerText("", 62) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + DisplayUtils.RESET);
        System.out.println(DisplayUtils.YELLOW + "           Track • Analyze • Grow • Succeed" + DisplayUtils.RESET);
        
        // Add a small pause for dramatic effect
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Continue if interrupted
        }
    }
    
    private static void displayMainMenu() {
    	
        User currentUser = authService.getCurrentUser();
        
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("PERSONAL FINANCE TRACKER");
        System.out.println(" Welcome, " + DisplayUtils.BOLD + currentUser.getUsername() + DisplayUtils.RESET);
        DisplayUtils.printSeparator();
        
        System.out.println(DisplayUtils.BLUE + DisplayUtils.BOLD + "💰 ACCOUNT MANAGEMENT" + DisplayUtils.RESET);
        System.out.println("1. Add New Account");
        System.out.println("2. View All Accounts");
        
        System.out.println(DisplayUtils.BLUE + DisplayUtils.BOLD + "\n💳 TRANSACTION MANAGEMENT" + DisplayUtils.RESET);
        System.out.println("3. Add Transaction");
        System.out.println("4. View Account Transactions");
        System.out.println("5. Import CSV Transactions");
        
        System.out.println(DisplayUtils.BLUE + DisplayUtils.BOLD + "\n📊 FINANCIAL ANALYSIS" + DisplayUtils.RESET);
        System.out.println("6. View Financial Metrics");
        System.out.println("7. Get Financial Recommendation");
        System.out.println("8. View Total Net Worth");
        
        System.out.println(DisplayUtils.BLUE + DisplayUtils.BOLD + "\n👤 USER SETTINGS" + DisplayUtils.RESET);
        System.out.println("9. Change Password");
        System.out.println("0. Exit");
        
        DisplayUtils.printSeparator();
    }
    
    private static void addAccount() {
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("ADD NEW ACCOUNT");
        
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
            
            DisplayUtils.printSuccess("Account added successfully!");
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
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("YOUR ACCOUNTS");
        
        try {
            User currentUser = authService.getCurrentUser();
            List<Account> accounts = financeService.getAccountsByUserId(currentUser.getId());
            
            if (accounts.isEmpty()) {
                DisplayUtils.printWarning("No accounts found.");
            } else {
                System.out.println(DisplayUtils.BOLD + "Total Accounts: " + accounts.size() + DisplayUtils.RESET);
                DisplayUtils.printSeparator();

                for (Account account : accounts) {
                    String balanceColor = account.getBalance().compareTo(BigDecimal.ZERO) >= 0 
                        ? DisplayUtils.GREEN : DisplayUtils.RED;

                    System.out.printf("🏦 %s\n", account.getName());
                    System.out.printf("   Type: %s | Balance: %s%.2f %s%s\n", 
                        account.getType(), 
                        balanceColor, 
                        account.getBalance(), 
                        account.getCurrency().getCurrencyCode(),
                        DisplayUtils.RESET);
                    System.out.printf("   ID: %d\n\n", account.getId());
                }
            }
        } catch (Exception e) {
            DisplayUtils.printError("Error retrieving accounts: " + e.getMessage());
        }
        
    }

    
    private static void viewTransactions() {
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("ACCOUNT TRANSACTIONS");
        viewAccounts();
        DisplayUtils.printSeparator();
        
        int accountId = getIntInput("Enter account ID to view transactions: ");
        List<Transaction> transactions = financeService.getAccountTransactions(accountId);
        
        if (transactions.isEmpty()) {
            DisplayUtils.printWarning("No transactions found for this account.");
        } else {
            System.out.println(DisplayUtils.BOLD + "Recent Transactions:" + DisplayUtils.RESET);
            DisplayUtils.printSeparator();
            for (Transaction transaction : transactions) {
                String typeColor = transaction.getType() == Transaction.TransactionType.INCOME 
                ? DisplayUtils.GREEN : DisplayUtils.RED;
            String typeSymbol = transaction.getType() == Transaction.TransactionType.INCOME ? "⬆️" : "⬇️";
            
            System.out.printf("%s %s %s\n", typeSymbol, typeColor, transaction.getDescription());
            System.out.printf("   Amount: %s%.2f%s | Category: %s\n", 
                typeColor, transaction.getAmount(), DisplayUtils.RESET, transaction.getCategory());
            System.out.printf("   Date: %s\n\n", transaction.getDate());
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
                try {
                    System.out.print("\nDo you want an account-specific expense breakdown? (y/n): ");
                    String scopeChoice = scanner.nextLine();
                    if (scopeChoice.equalsIgnoreCase("y")) {
                        viewAccounts();
                        int accountId = getIntInput("Enter account ID for breakdown: ");
                        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(year, month, accountId);
                        System.out.println();
                        System.out.println("Expense breakdown for account " + accountId + ":");
                        System.out.println(breakdown.toString());
                    } else {
                        ExpenseCategoryBreakdown breakdown = financeService.calculateExpenseCategoryPercentages(year, month);
                        System.out.println();
                        System.out.println(breakdown.toString());
                    }
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
        DisplayUtils.clearScreen();
        DisplayUtils.printHeader("TOTAL NET WORTH");
        
        System.out.println("Available currencies: USD, HKD, EUR, CNY, SGD");
        System.out.print("Enter currency code for net worth calculation: ");
        String currencyCode = scanner.nextLine().toUpperCase();
        
        try {
            Currency currency = Currency.getInstance(currencyCode);
            BigDecimal netWorth = financeService.getTotalNetWorth(currency);
            
            DisplayUtils.printSection("NET WORTH RESULT");
            
            String netWorthColor = netWorth.compareTo(BigDecimal.ZERO) >= 0 ? DisplayUtils.GREEN : DisplayUtils.RED;
            
            System.out.println(DisplayUtils.BOLD + "Total Net Worth: " + netWorthColor + 
                String.format("%.2f %s", netWorth, currency.getCurrencyCode()) + DisplayUtils.RESET);
                
        } catch (Exception e) {
            DisplayUtils.printError("Error calculating net worth: " + e.getMessage());
        }
        
        DisplayUtils.pressEnterToContinue();
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