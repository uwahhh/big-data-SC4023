import java.io.*;
import java.util.*;

public class PropertyDataAnalyzer {
    private List<String> months, towns, flat_types, street_name, storey_range, flat_model, block;
    private List<Double> floorAreas, resalePrices, lease_commence_date;
    private Map<String, List<Integer>> ZoneMap;  // Map to store all indexes for each town

    public PropertyDataAnalyzer() {
        months = new ArrayList<>();
        towns = new ArrayList<>();
        flat_types = new ArrayList<>();
        block = new ArrayList<>();
        street_name = new ArrayList<>();
        storey_range = new ArrayList<>();
        floorAreas = new ArrayList<>();
        flat_model = new ArrayList<>();
        lease_commence_date = new ArrayList<>();
        resalePrices = new ArrayList<>();
        ZoneMap = new HashMap<>();
    }

    // Load CSV file with error handling
    public void loadCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) { 
                    isHeader = false; 
                    continue; 
                }

                String[] values = line.split(",");
                if (values.length < 10) continue; // Avoid out-of-bounds error

                try {
                    months.add(values[0].trim());
                    String town = values[1].trim();
                    towns.add(town);
                    flat_types.add(values[2].trim());
                    block.add(values[3].trim());
                    street_name.add(values[4].trim());
                    storey_range.add(values[5].trim());
                    floorAreas.add(Double.parseDouble(values[6].trim()));
                    flat_model.add(values[7].trim());
                    lease_commence_date.add(Double.parseDouble(values[8].trim()));
                    resalePrices.add(Double.parseDouble(values[9].trim()));
                    
                    // Add index to town map
                    ZoneMap.computeIfAbsent(town, k -> new ArrayList<>()).add(towns.size() - 1);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid row: " + line);
                }
            }
            
            // Initialize TownZoneMapper after loading all data
            if (!months.isEmpty()) {
                String[] monthsArray = months.toArray(new String[0]);
                TownZoneMapper.initialize(monthsArray);
                System.out.println("Initialized TownZoneMapper with " + monthsArray.length + " months from CSV");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Integer> getFilteredIndexesWithYearIndex(String targetTown, int year, int startMonth) {
        List<Integer> indexes = new ArrayList<>();
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        int nextYear = (startMonth == 12) ? year + 1 : year;
    
        // Get all indexes for the target year from TownZoneMapper
        List<Integer> yearIndexes = TownZoneMapper.getYearIndexes(year);
        System.out.println("Using year index filtering for year " + year + " with " + yearIndexes.size() + " indexes");
        // System.out.println("yearrrrr"+yearIndexes);
        // Only iterate through indexes for the target year
        for (int i : yearIndexes) {
            // Check if this record is for the target town
            if (towns.get(i).equals(targetTown)) {
                String[] dateParts = months.get(i).split("-");
                int dataMonth = Integer.parseInt(dateParts[1]);
        
                if (floorAreas.get(i) >= 80 && 
                ((dataMonth == nextMonth || dataMonth == startMonth)))  {
                indexes.add(i);
            }
            }
        }
        
        // Also check the next year if needed
        if (nextYear != year) {
            List<Integer> nextYearIndexes = TownZoneMapper.getYearIndexes(nextYear);
            for (int i : nextYearIndexes) {
                // Check if this record is for the target town
                if (towns.get(i).equals(targetTown)) {
                    String[] dateParts = months.get(i).split("-");
                    int dataMonth = Integer.parseInt(dateParts[1]);
            
                    if (floorAreas.get(i) >= 80 && 
                        ((dataMonth == nextMonth || dataMonth == startMonth)))  {
                        indexes.add(i);
                    }

                }
            }
        }
    
        return indexes;
    }

    private List<Integer> getFilteredIndexesWithoutYearIndex(String targetTown, int year, int startMonth) {
        List<Integer> indexes = new ArrayList<>();
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        int nextYear = (startMonth == 12) ? year + 1 : year;
        
        // Traditional filtering - iterate through all records
        for (int i = 0; i < towns.size(); i++) {
            if (towns.get(i).equals(targetTown)) {
                String[] dateParts = months.get(i).split("-");
                int dataYear = Integer.parseInt(dateParts[0]);
                int dataMonth = Integer.parseInt(dateParts[1]);
        
                if (floorAreas.get(i) >= 80 &&
                    ((dataYear == year && dataMonth == startMonth) ||
                     (dataYear == nextYear && dataMonth == nextMonth))) {
        
                    indexes.add(i);
                }
            }
        }
        
        return indexes;
    }
    
    // Get list of resale prices matching criteria using year index
    public List<Double> filterPricesWithYearIndex(String targetTown, int year, int startMonth) {
        List<Integer> indexes = getFilteredIndexesWithYearIndex(targetTown, year, startMonth);
        List<Double> filteredPrices = new ArrayList<>();

        for (int index : indexes) {
            filteredPrices.add(resalePrices.get(index));
        }
        return filteredPrices;
    }

    // Get list of resale prices matching criteria without using year index
    public List<Double> filterPricesWithoutYearIndex(String targetTown, int year, int startMonth) {
        List<Integer> indexes = getFilteredIndexesWithoutYearIndex(targetTown, year, startMonth);
        List<Double> filteredPrices = new ArrayList<>();

        for (int index : indexes) {
            filteredPrices.add(resalePrices.get(index));
        }
        return filteredPrices;
    }

    // Keep the original method for backward compatibility
    public List<Double> filterPrices(String targetTown, int year, int startMonth) {
        return filterPricesWithYearIndex(targetTown, year, startMonth);
    }

    // Compute minimum price
    public double getMinPrice(List<Double> prices) {
        return prices.isEmpty() ? 0 : Collections.min(prices);
    }

    // Compute average price
    public double getAveragePrice(List<Double> prices) {
        return prices.isEmpty() ? 0 : prices.stream().mapToDouble(a -> a).average().orElse(0);
    }

    // Compute standard deviation
    public double getStdDev(List<Double> prices) {
        if (prices.isEmpty()) return 0;
        double mean = getAveragePrice(prices);
        return Math.sqrt(prices.stream().mapToDouble(p -> Math.pow(p - mean, 2)).sum() / prices.size());
    }

    // Get minimum price per square meter for a town
    public double getMinPricePerSqm(String targetTown, int year, int startMonth) {
        List<Integer> indexes = getFilteredIndexesWithYearIndex(targetTown, year, startMonth);
        
        double minPricePerSqm = Double.MAX_VALUE;
        for (int index : indexes) {
            double pricePerSqm = resalePrices.get(index) / floorAreas.get(index);
            if (pricePerSqm < minPricePerSqm) {
                minPricePerSqm = pricePerSqm;
            }
        }

        return (minPricePerSqm == Double.MAX_VALUE) ? 0 : minPricePerSqm;
    }
    
    // Getters for data access
    public List<String> getMonths() {
        return months;
    }
    
    public List<String> getTowns() {
        return towns;
    }
    
    public List<Double> getFloorAreas() {
        return floorAreas;
    }
    
    public List<Double> getResalePrices() {
        return resalePrices;
    }
    
    // Setters for loading data from column store
    public void setMonths(List<String> months) {
        this.months = months;
        rebuildZoneMap();
    }
    
    public void setTowns(List<String> towns) {
        this.towns = towns;
        rebuildZoneMap();
    }
    
    public void setFloorAreas(List<Double> floorAreas) {
        this.floorAreas = floorAreas;
    }
    
    public void setResalePrices(List<Double> resalePrices) {
        this.resalePrices = resalePrices;
    }
    
    // Rebuild the town index map after loading data from column store
    private void rebuildZoneMap() {
        // Initialize the TownZoneMapper with the months array
        if (!months.isEmpty()) {
            String[] monthsArray = months.toArray(new String[0]);
            TownZoneMapper.initialize(monthsArray);
            System.out.println("Initialized TownZoneMapper with " + monthsArray.length + " months");
        }
    }
} 