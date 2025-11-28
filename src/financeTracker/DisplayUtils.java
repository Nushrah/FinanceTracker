package financeTracker;
public class DisplayUtils {
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";
    
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    public static void printHeader(String title) {
        System.out.println("\n" + CYAN + "╔═══════════════════════════════════════════════════╗");
        System.out.println("║" + centerText(title, 49) + "║");
        System.out.println("╚═══════════════════════════════════════════════════╝" + RESET);
    }
    
    public static void printSection(String title) {
        System.out.println("\n" + BLUE + "┌───────────────────────────────────────────────────┐");
        System.out.println("│ " + BOLD + title + RESET + BLUE);
        System.out.println("└───────────────────────────────────────────────────┘" + RESET);
    }
    
    public static void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }
    
    public static void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }
    
    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }
    
    public static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
    
    public static void pressEnterToContinue() {
        System.out.print(YELLOW + "\nPress Enter to continue..." + RESET);
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    public static void printSeparator() {
        System.out.println(CYAN + "─────────────────────────────────────────────────────" + RESET);
    }
}