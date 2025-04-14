import java.io.*;
import java.util.*;

public class ColumnarStorage {
    private PropertyDataAnalyzer dataAnalyzer;
    
    public ColumnarStorage() {
        dataAnalyzer = new PropertyDataAnalyzer();
    }
    
    // Load CSV file with error handling
    public void loadCSV(String filePath) {
        dataAnalyzer.loadCSV(filePath);
    }
    
    // Get the data analyzer instance
    public PropertyDataAnalyzer getDataAnalyzer() {
        return dataAnalyzer;
    }
    
    // Columnstore specific methods
    public void createColumnStore(String outputDir) {
        try {
            // Create output directory if it doesn't exist
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Write each column to a separate file
            writeColumnToFile(outputDir + "/months.txt", dataAnalyzer.getMonths());
            writeColumnToFile(outputDir + "/towns.txt", dataAnalyzer.getTowns());
            writeColumnToFile(outputDir + "/floor_areas.txt", dataAnalyzer.getFloorAreas());
            writeColumnToFile(outputDir + "/resale_prices.txt", dataAnalyzer.getResalePrices());
            
            System.out.println("Column store created successfully in " + outputDir);
        } catch (IOException e) {
            System.err.println("Error creating column store: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void writeColumnToFile(String filePath, List<?> column) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Object value : column) {
                writer.println(value);
            }
        }
    }
    
    public void loadColumnStore(String inputDir) {
        try {
            // Clear existing data
            dataAnalyzer = new PropertyDataAnalyzer();
            
            // Load each column from its file
            List<String> months = new ArrayList<>();
            List<String> towns = new ArrayList<>();
            List<Double> floorAreas = new ArrayList<>();
            List<Double> resalePrices = new ArrayList<>();
            
            // Read months
            try (BufferedReader reader = new BufferedReader(new FileReader(inputDir + "/months.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    months.add(line.trim());
                }
            }
            
            // Read towns
            try (BufferedReader reader = new BufferedReader(new FileReader(inputDir + "/towns.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    towns.add(line.trim());
                }
            }
            
            // Read floor areas
            try (BufferedReader reader = new BufferedReader(new FileReader(inputDir + "/floor_areas.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    floorAreas.add(Double.parseDouble(line.trim()));
                }
            }
            
            // Read resale prices
            try (BufferedReader reader = new BufferedReader(new FileReader(inputDir + "/resale_prices.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    resalePrices.add(Double.parseDouble(line.trim()));
                }
            }
            
            // Load the data into the PropertyDataAnalyzer
            dataAnalyzer.setMonths(months);
            dataAnalyzer.setTowns(towns);
            dataAnalyzer.setFloorAreas(floorAreas);
            dataAnalyzer.setResalePrices(resalePrices);
            
            // // Initialize TownZoneMapper with the loaded towns
            // if (!towns.isEmpty()) {
            //     String[] townsArray = towns.toArray(new String[0]);
            //     TownZoneMapper.initialize(townsArray);
            //     System.out.println("Initialized TownZoneMapper with " + townsArray.length + " towns from column store");
            // }
            
            System.out.println("Column store loaded successfully from " + inputDir);
        } catch (IOException e) {
            System.err.println("Error loading column store: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
