import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] matricNo = {"U2223198G", "U2746929C", "U2346025D",};
        // Load Data
        ColumnarStorage storage = new ColumnarStorage();
        storage.loadCSV("ResalePricesSingapore.csv");
        
        for (String matric : matricNo) {
            processMatriculation(matric, storage);
        }
    }

    public static void processMatriculation(String matricNo, ColumnarStorage storage) {
        // Process Matriculation Number
        MatriculationProcessor matricProcessor = new MatriculationProcessor(matricNo);
        int yearLastDigit = matricProcessor.getYearDigit();
        int month = matricProcessor.getMonth();
        String town = matricProcessor.getTown();
        int targetYear = 2010 + yearLastDigit;
        // Debug
        // System.out.println("Matriculation Number: " + matricNo);
        // System.out.println("Year Digit: " + yearLastDigit);
        // System.out.println("Month: " + month);
        // System.out.println("Town: " + town);
        // System.out.println("Target Year: " + targetYear);


        // Get Filtered Prices
        List<Double> filteredPrices = storage.filterPrices(town, yearLastDigit, month);

        // Compute Statistics
        double minPrice = storage.getMinPrice(filteredPrices);
        double avgPrice = storage.getAveragePrice(filteredPrices);
        double stdDev = storage.getStdDev(filteredPrices);
        double minPricePerSqm = storage.getMinPricePerSqm(town, yearLastDigit, month);

        // Write CSV Output
        generateCSV(matricNo, targetYear, month, town, minPrice, avgPrice, stdDev, minPricePerSqm);
    }

    public static void generateCSV(String matricNo, int year, int month, String town,
                                   double minPrice, double avgPrice, double stdDev, double minPricePerSqm) {
        String fileName = "ScanResult_" + matricNo + ".csv";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Year,Month,Town,Category,Value\n");
            writeRow(writer, year, month, town, "Minimum Price", minPrice);
            writeRow(writer, year, month, town, "Average Price", avgPrice);
            writeRow(writer, year, month, town, "Standard Deviation of Price", stdDev);
            writeRow(writer, year, month, town, "Minimum Price per Square Meter", minPricePerSqm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeRow(BufferedWriter writer, int year, int month, String town, String category, double value) throws IOException {
        writer.write(year + "," + String.format("%02d", month) + "," + town + "," + category + "," + (value == 0 ? "No result" : String.format("%.2f", value)) + "\n");
    }
}

