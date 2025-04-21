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
    
    // Get list of resale prices matching criteria using year index (shared scan)
    public List<Integer> filterPricesWithYearIndexSharedScan(String targetTown, int year, int startMonth) {
        int startIdx = TownZoneMapper.getStartIndex(year);
        int endIdx   = TownZoneMapper.getEndIndex(year);
        List<Integer> filtered    = new ArrayList<>();
        List<Integer> townIndices = ZoneMap.getOrDefault(targetTown, Collections.emptyList());
        for (int i = startIdx; i <= endIdx; i++) {
            for (int idx : townIndices) {
                if (idx == i && matchesWindow(idx, year, startMonth, true)) {
                    filtered.add(idx);
                }
            }
        }
        return filtered;
    }
    
    // Get list of resale prices matching criteria without using shared scan
    public List<Integer> filterPricesWithoutYearIndexSharedScan(String targetTown, int year, int startMonth) {
        List<Integer> filtered = new ArrayList<>();
        int nextYear = (startMonth == 12) ? year + 1 : year;
        for (int i = 0; i < towns.size(); i++) {
            if (!towns.get(i).equals(targetTown)) continue;
            String[] parts = months.get(i).split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (floorAreas.get(i) >= 80 &&
                ((y == year && m == startMonth) ||
                 (y == nextYear && m == ((startMonth == 12) ? 1 : startMonth + 1)))) {
                filtered.add(i);
            }
        }
        return filtered;
    }
    
    // Get list of resale prices matching criteria using year index
    public List<Integer> filterPricesWithYearIndex(String targetTown, int year, int startMonth) {
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        int startIdx  = TownZoneMapper.getStartIndex(year);
        int endIdx    = TownZoneMapper.getEndIndex(year);
        List<Integer> filtered = new ArrayList<>();

        for (int i = startIdx; i <= endIdx; i++) filtered.add(i);

        filtered.removeIf(idx -> {
            int m = Integer.parseInt(months.get(idx).split("-")[1]);
            return m != startMonth && m != nextMonth;
        });
        filtered.removeIf(idx -> !towns.get(idx).equals(targetTown));
        filtered.removeIf(idx -> floorAreas.get(idx) < 80);

        return filtered;
    }
    
    // Get list of resale prices matching criteria without using year index
    public List<Integer> filterPricesWithoutYearIndex(String targetTown, int year, int startMonth) {
        // first filter by date window & minimum area
        List<Integer> filtered = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            if (matchesWindow(i, year, startMonth, false)) {
                filtered.add(i);
            }
        }
        // then keep only the target town
        filtered.removeIf(idx -> !towns.get(idx).equals(targetTown));
        return filtered;
    }
    
    // Compute minimum price from filtered row-indices
    public double getMinPrice(List<Integer> indices) {
        if (indices.isEmpty()) return 0;
        double min = Double.MAX_VALUE;
        for (int idx : indices) {
            min = Math.min(min, resalePrices.get(idx));
        }
        return min == Double.MAX_VALUE ? 0 : min;
    }

    // Compute average price from filtered row-indices
    public double getAveragePrice(List<Integer> indices) {
        if (indices.isEmpty()) return 0;
        double sum = 0;
        for (int idx : indices) {
            sum += resalePrices.get(idx);
        }
        return sum / indices.size();
    }

    // Compute standard deviation from filtered row-indices
    public double getStdDev(List<Integer> indices) {
        if (indices.isEmpty()) return 0;
        double mean = getAveragePrice(indices);
        double sumSq = 0;
        for (int idx : indices) {
            double val = resalePrices.get(idx);
            sumSq += Math.pow(val - mean, 2);
        }
        return Math.sqrt(sumSq / indices.size());
    }

    // Get minimum price per square meter from filtered row-indices
    public double getMinPricePerSqm(List<Integer> indices) {
        if (indices.isEmpty()) return 0;
        double minPpsm = Double.MAX_VALUE;
        for (int idx : indices) {
            double p = resalePrices.get(idx);
            double a = floorAreas.get(idx);
            double ppsm = p / a;
            if (ppsm < minPpsm) {
                minPpsm = ppsm;
            }
        }
        return minPpsm == Double.MAX_VALUE ? 0 : minPpsm;
    }
    
    // Helper to check date and area criteria
    private boolean matchesWindow(int idx, int year, int startMonth, boolean useYearIndex) {
        String[] parts = months.get(idx).split("-");
        int dataYear  = Integer.parseInt(parts[0]);
        int dataMonth = Integer.parseInt(parts[1]);
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        boolean inWindow;
        if (useYearIndex) {
            inWindow = (dataMonth == startMonth || dataMonth == nextMonth);
        } else {
            int nextYear = (startMonth == 12) ? year + 1 : year;
            inWindow = ((dataYear == year && dataMonth == startMonth) ||
                        (dataYear == nextYear && dataMonth == nextMonth));
        }
        return floorAreas.get(idx) >= 80 && inWindow;
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