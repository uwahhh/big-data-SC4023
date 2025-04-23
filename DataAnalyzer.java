import java.io.*;
import java.util.*;

public class DataAnalyzer {
    private List<String> months, towns, flat_types, street_name, storey_range, flat_model, block;
    private List<Double> floorAreas, resalePrices, lease_commence_date;
    private Map<String, List<Integer>> ZoneMap;  // Map to store all indexes for each town
    private Map<String, List<Integer>> yearMonthTownIndex; // Composite key index for year, month, town

    public DataAnalyzer() {
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
        yearMonthTownIndex = new HashMap<>();
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
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
            // Get list of resale prices matching criteria using year index (shared scan)
            public List<Integer> fpMonthIndexSharedScan(String targetTown, int year, int startMonth) {
                List<Integer> filtered = new ArrayList<>();
                List<Integer> YearIndices = TownZoneMapper.getYearIndices(year);
                List<Integer> MonthIndices = TownZoneMapper.getMonthIndices(startMonth);
               
                if (MonthIndices == null || MonthIndices.isEmpty()) {
                    System.out.println("Filtered prices with month index shared scan: 0");
                    return filtered;
                }
                int nextMonth = (startMonth == 12) ? 1 : startMonth + 1; 
                List<Integer> NextMonthIndices = TownZoneMapper.getMonthIndices(nextMonth);
                
                for(int idx: YearIndices) {
                    if ((MonthIndices.contains(idx) ||NextMonthIndices.contains(idx))&&
                         (targetTown.equals(towns.get(idx)) && floorAreas.get(idx) >= 80)) {
                        filtered.add(idx);
                    }
                }
                
                // System.out.println("Filtered prices with month index shared scan: " + filtered.size());
                return filtered;
            }
        
    // Get list of resale prices matching criteria using year index (shared scan)
    public List<Integer> filterPricesWithYearIndexSharedScan(String targetTown, int year, int startMonth) {
        List<Integer> filtered = new ArrayList<>();
        List<Integer> yearIndices = TownZoneMapper.getYearIndices(year);
        // List<Integer> townIndices = ZoneMap.getOrDefault(targetTown, Collections.emptyList());
        if (yearIndices == null || yearIndices.isEmpty()) {
            System.out.println("Filtered prices with year index shared scan: 0");
            return filtered;
        }
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        for (int idx : yearIndices) {

            String[] parts = months.get(idx).split("-");
            int dataMonth = Integer.parseInt(parts[1]);
            if (targetTown.equals(towns.get(idx)) && floorAreas.get(idx) >= 80 && (dataMonth == startMonth || dataMonth == nextMonth)) {
                filtered.add(idx);
            }
        }
        System.out.println("Filtered prices with year index shared scan: " + filtered.size());
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
        List<Integer> yearIndices = TownZoneMapper.getYearIndices(year);
        List<Integer> filtered = new ArrayList<>();

        for (int i = yearIndices.get(0); i <= yearIndices.get(yearIndices.size() - 1); i++) filtered.add(i);

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

    // Build composite key index for year, month, and town
    public void buildYearMonthTownIndex() {
        for (int i = 0; i < months.size(); i++) {
            String[] parts = months.get(i).split("-");
            if (parts.length != 2) continue; // Skip invalid month format
            String key = parts[0] + "_" + parts[1] + "_" + towns.get(i);
            yearMonthTownIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }
        System.out.println("Composite index built for key format: year_month_town");
    }

    // Filter by month candidates using precomputed month indices (focus on year, month, town)
    public List<Integer> filterWithHashing(String targetTown, int year, int startMonth) {
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        List<Integer> matched = new ArrayList<>();

        String k1 = year + "_" + startMonth + "_" + targetTown;
        String k2 = year + "_" + nextMonth + "_" + targetTown;

        matched.addAll(yearMonthTownIndex.getOrDefault(k1, Collections.emptyList()));
        matched.addAll(yearMonthTownIndex.getOrDefault(k2, Collections.emptyList()));
        matched.removeIf(idx -> floorAreas.get(idx) < 80); // Filter by area
        return matched;
    }
    
    // Filter by month candidates using precomputed month indices (focus on months only)
    public List<Integer> filterByMonthCandidates(List<Integer> monthCandidates, int year, int startMonth) {
        List<Integer> filtered = new ArrayList<>();
        for (int idx : monthCandidates) {
            if (matchesWindow(idx, year, startMonth, true)) {
                filtered.add(idx);
            }
        }
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
    
    // private boolean matchesYearWindow(int idx, int startMonth) {
    //     String[] parts = months.get(idx).split("-");

    //     int dataMonth = Integer.parseInt(parts[1]);
    //     int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
    //     boolean inWindow;

    //         inWindow = (dataMonth == startMonth || dataMonth == nextMonth);

    //     return floorAreas.get(idx) >= 80 && inWindow;
    // }
    // Helper to check date and area criteria, to be used in filter methods
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

    public void writeCompositeIndexToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Map.Entry<String, List<Integer>> entry : yearMonthTownIndex.entrySet()) {
                writer.write(entry.getKey() + " : " + entry.getValue());
                writer.newLine();
            }
            System.out.println("Composite index written to " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write composite index to file.");
            e.printStackTrace();
        }
    }
    
    
    // Setters for loading data from column store
    public void setMonths(List<String> months) {
        this.months = months;
        // rebuildZoneMap(); // mapping handled in Main
    }
    
    public void setTowns(List<String> towns) {
        this.towns = towns;
        // Rebuild town-to-indices map for filtering
        ZoneMap.clear();
        for (int i = 0; i < towns.size(); i++) {
            ZoneMap.computeIfAbsent(towns.get(i), k -> new ArrayList<>()).add(i);
        }
        // rebuildZoneMap(); // mapping handled in Main
    }
    
    public void setFloorAreas(List<Double> floorAreas) {
        this.floorAreas = floorAreas;
    }
    
    public void setResalePrices(List<Double> resalePrices) {
        this.resalePrices = resalePrices;
    }
}