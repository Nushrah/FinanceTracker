import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {

    public static void main(String [] args) throws FileNotFoundException {	
        
        Scanner in = new Scanner(System.in);

        System.out.print("Please state your name: ");
        String name = in.nextLine();

        System.out.print("Please input your savings: ");
        int savings = in.nextInt();
        in.nextLine(); // consume leftover newline

        System.out.print("Please input your currency of choice: ");
        String currency = in.nextLine();
        
        System.out.print("Please input the file pathname: ");
        String filepathname = in.nextLine();
        Scanner inFile = null;

        try {
            inFile = new Scanner(new File(filepathname));
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Exiting program.");
            System.exit(0);
        }

        // Skip header row
        if (inFile.hasNextLine()) {
            inFile.nextLine();
        }

        System.out.println("\n--- Transaction Records ---");
        while (inFile.hasNextLine()) {
            String line = inFile.nextLine();
            String[] parts = line.split(",");

            String date = parts[0];
            String transactionName = parts[1];
            String category = parts[2];
            double amount = Double.parseDouble(parts[3]);

            System.out.println("Date: " + date);
            System.out.println("Transaction: " + transactionName);
            System.out.println("Category: " + category);
            System.out.println("Amount: " + amount + " " + currency);
            System.out.println();
        }
    }    
}
