import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Debug 1 Matriculation Number
        String[] matricNo = {"U2223931F"};
        // String[] matricNo = {"U2223191G", "U2746929C", "U2346025D",};
        
        // Create a ColumnarStorage instance
        ColumnarStorage storage = new ColumnarStorage();
        
        // Check if column store exists and if there are any txt files in the directory
        // If column store exists, load data from it (faster)
        String columnStoreDir = "column_store";
        File columnStoreFile = new File(columnStoreDir);
        if (!columnStoreFile.exists()) {
            columnStoreFile.mkdir(); // Create the directory if it doesn't exist
        }
        File[] files = columnStoreFile.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null && files.length > 0) {
            // If there are txt files, load data from them (faster)
            System.out.println("Loading data from column store...");
            storage.loadColumnStore(columnStoreDir);
        } else {
            // If no txt files, load from CSV and create column store
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
        long startTime;
        long endTime;

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
        
        // Initialize TownZoneMapper
        TownZoneMapper.initialize(storage.getDataAnalyzer().getMonths().toArray(new String[0]));


        // Process using year index with sharedscan
        System.out.println("\nProcessing with year index with sharedscan:");
        startTime = System.nanoTime();
        List<Integer> filterPricesWithYearIndexSharedScan = storage.getDataAnalyzer().filterPricesWithYearIndexSharedScan(targetTown, targetYear, month);
        endTime = System.nanoTime();
        long timeSharedScan = endTime - startTime;
        System.out.println("Elapsed time for filtering with year index with sharedscan: " + timeSharedScan + " nanoseconds");

        // Process using year index without sharedscan
        System.out.println("\nProcessing with year index without sharedscan:");
        startTime = System.nanoTime();
        List<Integer> filterPricesWithYearIndex = storage.getDataAnalyzer().filterPricesWithYearIndex(targetTown, targetYear, month);
        endTime = System.nanoTime();
        long timeNoSharedScan = endTime - startTime;
        System.out.println("Elapsed time for filtering with year index without sharedscan: " + timeNoSharedScan + " nanoseconds");

        // Process without year index with shared scan
        System.out.println("\nProcessing without year index with shared scan:");
        startTime = System.nanoTime();
        List<Integer> filterPricesWithoutYearIndexSharedScan = storage.getDataAnalyzer().filterPricesWithoutYearIndexSharedScan(targetTown, targetYear, month);
        endTime = System.nanoTime();
        long timeNoIndexSharedScan = endTime - startTime;
        System.out.println("Elapsed time for filtering without year index with shared scan: " + timeNoIndexSharedScan + " nanoseconds");

        // Process without year index without sharedscan
        System.out.println("\nProcessing without year index without sharedscan:");
        startTime = System.nanoTime();
        List<Integer> filterPricesWithoutYearIndex = storage.getDataAnalyzer().filterPricesWithoutYearIndex(targetTown, targetYear, month);
        endTime = System.nanoTime();
        long timeNoIndexNoSharedScan = endTime - startTime;
        System.out.println("Elapsed time for filtering without year index without sharedscan: " + timeNoIndexNoSharedScan + " nanoseconds");

        // Use hashing to store the results
        storage.getDataAnalyzer().buildYearMonthTownIndex();

        // Timing in processMatriculationTown
        System.out.println("\nProcessing with hashing index sharedscan:");
        startTime = System.nanoTime();
        List<Integer> filterWithHashing = storage.getDataAnalyzer().filterWithHashing(targetTown, targetYear, month);
        endTime = System.nanoTime();
        long timeWithHashing = endTime - startTime;
        System.out.println("Elapsed time for hashing index sharedscan: " + timeWithHashing + " nanoseconds");
                

        // Determine the fastest filtering method
        Map<String, Long> times = new HashMap<>();
        times.put("yearIndexSharedScan", timeSharedScan);
        times.put("yearIndexNoSharedScan", timeNoSharedScan);
        times.put("noIndexSharedScan", timeNoIndexSharedScan);
        times.put("noIndexNoSharedScan", timeNoIndexNoSharedScan);
        times.put("hashingIndexSharedScan", timeWithHashing);
        
        String bestMethod = null;
        long bestTime = Long.MAX_VALUE;
        
        for (Map.Entry<String, Long> entry : times.entrySet()) {
            if (entry.getValue() < bestTime) {
                bestTime = entry.getValue();
                bestMethod = entry.getKey();
            }
        }
        
        List<Integer> bestResult = switch (bestMethod) {
            case "yearIndexSharedScan" -> filterPricesWithYearIndexSharedScan;
            case "yearIndexNoSharedScan" -> filterPricesWithYearIndex;
            case "noIndexSharedScan" -> filterPricesWithoutYearIndexSharedScan;
            case "noIndexNoSharedScan" -> filterPricesWithoutYearIndex;
            case "hashingIndexSharedScan" -> filterWithHashing;
            default -> Collections.emptyList();
        };
        
        System.out.println("\nFastest method: " + bestMethod + " (" + bestTime + " ns)");
        if (!bestResult.isEmpty()) {
            // Compute Statistics
            double minPrice = storage.getDataAnalyzer().getMinPrice(bestResult);
            double avgPrice = storage.getDataAnalyzer().getAveragePrice(bestResult);
            double stdDev = storage.getDataAnalyzer().getStdDev(bestResult);
            double minPricePerSqm = storage.getDataAnalyzer().getMinPricePerSqm(bestResult);
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
