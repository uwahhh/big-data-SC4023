import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Debug 1 Matriculation Number
        String[] matricNo = {"U2223931F"};
        // String[] matricNo = {"U2223191G", "U2746929C", "U2346025D",};
        
        // Create a ColumnarStorage instance
        ColumnarStorage storage = new ColumnarStorage();
        
        // Check if column store exists
        String columnStoreDir = "column_store";
        File columnStoreFile = new File(columnStoreDir);
        
        if (columnStoreFile.exists() && columnStoreFile.isDirectory()) {
            // If column store exists, load data from it (faster)
            System.out.println("Loading data from column store...");
            storage.loadColumnStore(columnStoreDir);
        } else {
            // If column store doesn't exist, load from CSV and create column store
            System.out.println("Loading data from CSV file...");
            storage.loadCSV("ResalePricesSingapore.csv");
            
            // Create column store for future use
            System.out.println("Creating column store...");
            storage.createColumnStore(columnStoreDir);
        }
        
        // Process each matriculation number
        for (String matric : matricNo) {
            processMatriculationTown(matric, storage);
        }
    }

    public static void processMatriculationTown(String matricNo, ColumnarStorage storage) {
        // Process Matriculation Number
        MatriculationProcessor matricProcessor = new MatriculationProcessor(matricNo);
        int yearLastDigit = matricProcessor.getYearDigit();
        int month = matricProcessor.getMonth();
        String targetTown = matricProcessor.getTown();
        
        // Target year is 2014 to 2023
        int targetYear = 2010 + yearLastDigit;
        if(targetYear < 2014)
            targetYear += 10;
        
        // Debug
        System.out.println("\nProcessing Matriculation Number: " + matricNo);
        System.out.println("Year Digit: " + yearLastDigit);
        System.out.println("Month: " + month);
        System.out.println("Target Town: " + targetTown);
        System.out.println("Target Year: " + targetYear);
        
        // Process using town index
        System.out.println("\nProcessing with town index:");
        List<Double> filteredPricesWithIndex = storage.getDataAnalyzer().filterPricesWithZone(targetTown, targetYear, month);
        System.out.println("Number of properties found with town index: " + filteredPricesWithIndex.size());
        
        // Process without town index
        System.out.println("\nProcessing without town index:");
        List<Double> filteredPricesWithoutIndex = storage.getDataAnalyzer().filterPricesWithoutZone(targetTown, targetYear, month);
        System.out.println("Number of properties found without town index: " + filteredPricesWithoutIndex.size());
        
        // Compare results
        if (filteredPricesWithIndex.size() != filteredPricesWithoutIndex.size()) {
            System.out.println("\nWARNING: Different number of properties found using different methods!");
            System.out.println("With town index: " + filteredPricesWithIndex.size());
            System.out.println("Without town index: " + filteredPricesWithoutIndex.size());
        }
        
        // Use the results from town index method (more efficient)
        if (!filteredPricesWithIndex.isEmpty()) {
            // Compute Statistics
            double minPrice = storage.getDataAnalyzer().getMinPrice(filteredPricesWithIndex);
            double avgPrice = storage.getDataAnalyzer().getAveragePrice(filteredPricesWithIndex);
            double stdDev = storage.getDataAnalyzer().getStdDev(filteredPricesWithIndex);
            double minPricePerSqm = storage.getDataAnalyzer().getMinPricePerSqm(targetTown, targetYear, month);
            
            // Generate CSV for the target town
            generateCSV(matricNo, targetYear, month, targetTown, minPrice, avgPrice, stdDev, minPricePerSqm);
        }
    }

    private static void generateCSV(String matricNo, int year, int month, String location, 
                                   double minPrice, double avgPrice, double stdDev, double minPricePerSqm) {
        String fileName = "ScanResult_" + matricNo + ".csv";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Year,Month,Location,Category,Value\n");
            writeRow(writer, year, month, location, "Minimum Price", minPrice);
            writeRow(writer, year, month, location, "Average Price", avgPrice);
            writeRow(writer, year, month, location, "Standard Deviation of Price", stdDev);
            writeRow(writer, year, month, location, "Minimum Price per Square Meter", minPricePerSqm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeRow(BufferedWriter writer, int year, int month, String location, String category, double value) throws IOException {
        writer.write(year + "," + String.format("%02d", month) + "," + location + "," + category + "," + (value == 0 ? "No result" : String.format("%.2f", value)) + "\n");
    }
}

