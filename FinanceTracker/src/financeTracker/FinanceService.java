package financeTracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Currency;

public class FinanceService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyExchange currencyService;
    private final RecommendationEngine recommendationService;
    
    public FinanceService(AccountRepository accountRepository, 
                         TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currencyService = new CurrencyExchange();
        this.recommendationService = new RecommendationEngine();
    }
    
    public void addTransaction(Transaction transaction) {
        // Update account balance
        Account account = accountRepository.getAccountById(transaction.getAccountId());
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        
        BigDecimal newBalance;
        if (transaction.getType() == Transaction.TransactionType.INCOME) {
            newBalance = account.getBalance().add(transaction.getAmount());
        } else {
            newBalance = account.getBalance().subtract(transaction.getAmount());
        }
        
        accountRepository.updateAccountBalance(account.getId(), newBalance);
        transactionRepository.addTransaction(transaction);
    }
    
    public FinancialMetrics calculateMonthlyMetrics(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Transaction> monthlyTransactions = transactionRepository.getTransactionsByDateRange(startDate, endDate);
        
        FinancialMetrics metrics = new FinancialMetrics();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        for (Transaction transaction : monthlyTransactions) {
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else {
                totalExpenses = totalExpenses.add(transaction.getAmount());
            }
        }
        
        metrics.setTotalIncome(totalIncome);
        metrics.setTotalExpenses(totalExpenses);
        metrics.setNetCashFlow(totalIncome.subtract(totalExpenses));
        
        // Calculate savings rate (only if there's income)
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savings = totalIncome.subtract(totalExpenses);
            BigDecimal savingsRate = savings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                                          .multiply(BigDecimal.valueOf(100));
            metrics.setSavingsRate(savingsRate);
        }
        
        // Calculate expense to income ratio
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal expenseRatio = totalExpenses.divide(totalIncome, 4, RoundingMode.HALF_UP)
                                                 .multiply(BigDecimal.valueOf(100));
            metrics.setExpenseToIncomeRatio(expenseRatio);
        }
        
        return metrics;
    }
    
    public BigDecimal getTotalNetWorth(Currency targetCurrency) {
        List<Account> accounts = accountRepository.getAllAccounts();
        BigDecimal totalNetWorth = BigDecimal.ZERO;
        
        for (Account account : accounts) {
            BigDecimal convertedBalance = currencyService.convertCurrency(
                account.getBalance(), account.getCurrency(), targetCurrency);
            totalNetWorth = totalNetWorth.add(convertedBalance);
        }
        
        return totalNetWorth;
    }
    
    public String getFinancialRecommendation(int year, int month) {
        FinancialMetrics metrics = calculateMonthlyMetrics(year, month);
        return recommendationService.getRandomRecommendation(metrics);
    }
    
    public List<Account> getAccountsByUserId(int userId) {
        List<Account> allAccounts = accountRepository.getAllAccounts();
        List<Account> userAccounts = new ArrayList<>();
        
        for (Account account : allAccounts) {
            if (account.getUserId() == userId) {
                userAccounts.add(account);
            }
        }
        
        return userAccounts;
    }
    
    public Account getAccountById(int accountId) {
        return accountRepository.getAccountById(accountId);
    }
    
    // Delegate methods to repositories
    public void addAccount(Account account) {
        accountRepository.addAccount(account);
    }
    
    public List<Account> getAllAccounts() {
        return accountRepository.getAllAccounts();
    }
    
    public List<Transaction> getAccountTransactions(int accountId) {
        return transactionRepository.getTransactionsByAccount(accountId);
    }
    
    public CurrencyExchange getCurrencyService() {
        return currencyService;
    }
}