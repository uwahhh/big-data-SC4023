import java.io.*;
import java.util.*;

public class ColumnarStorage {
    private DataAnalyzer dataAnalyzer;
    
    public ColumnarStorage() {
        dataAnalyzer = new DataAnalyzer();
    }
    
    // Load CSV file with error handling
    public void loadCSV(String filePath) {
        dataAnalyzer.loadCSV(filePath);
    }
    
    // Get the data analyzer instance
    public DataAnalyzer getDataAnalyzer() {
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
            dataAnalyzer.buildYearMonthTownIndex();
            // Debug
            // Check output directory for composite index
            // System.err.println("composite index file: " + outputDir + "/composite_index.txt");
            // Write composite index to file
            dataAnalyzer.writeCompositeIndexToFile(outputDir + "/composite_index.txt");

            
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
            dataAnalyzer = new DataAnalyzer();
            
            // Load each column from its file
            List<String> months = new ArrayList<>();
            List<String> towns = new ArrayList<>();
            List<Double> floorAreas = new ArrayList<>();
            List<Double> resalePrices = new ArrayList<>();
            
            // Load each column in parallel, dropping incomplete rows
            try (
                BufferedReader monthsReader = new BufferedReader(new FileReader(inputDir + "/months.txt"));
                BufferedReader townsReader  = new BufferedReader(new FileReader(inputDir + "/towns.txt"));
                BufferedReader areasReader  = new BufferedReader(new FileReader(inputDir + "/floor_areas.txt"));
                BufferedReader pricesReader = new BufferedReader(new FileReader(inputDir + "/resale_prices.txt"))
            ) {
                String monthLine, townLine, areaLine, priceLine;
                while ((monthLine = monthsReader.readLine()) != null
                    && (townLine  = townsReader.readLine())   != null
                    && (areaLine  = areasReader.readLine())   != null
                    && (priceLine = pricesReader.readLine())  != null) {
                    monthLine = monthLine.trim();
                    townLine  = townLine.trim();
                    areaLine  = areaLine.trim();
                    priceLine = priceLine.trim();

                    if (monthLine.isEmpty() || townLine.isEmpty()
                        || areaLine.isEmpty()  || priceLine.isEmpty()) {
                        System.err.println("Skipping incomplete row due to missing cell: "
                            + monthLine + ", " + townLine + ", " + areaLine + ", " + priceLine);
                        continue;  // drop incomplete row
                    }
                    try {
                        double area  = Double.parseDouble(areaLine);
                        double price = Double.parseDouble(priceLine);
                        months.add(monthLine);
                        towns.add(townLine);
                        floorAreas.add(area);
                        resalePrices.add(price);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid numeric row: "
                            + monthLine + ", " + townLine
                            + ", " + areaLine + ", " + priceLine);
                    }
                }
            }
            
            // Load the data into the DataAnalyzer
            dataAnalyzer.setMonths(months);
            dataAnalyzer.setTowns(towns);
            dataAnalyzer.setFloorAreas(floorAreas);
            dataAnalyzer.setResalePrices(resalePrices);
            System.out.println("Column store loaded successfully from " + inputDir);
        } catch (IOException e) {
            System.err.println("Error loading column store: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
