import java.io.*;
import java.util.*;

public class ColumnarStorage {
    private List<String> months, towns, flat_types, street_name, storey_range, flat_model;
    private List<Double> floorAreas, resalePrices, block, lease_commence_date;

    public ColumnarStorage() {
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
                    towns.add(values[1].trim());
                    flat_types.add(values[2].trim());
                    block.add(Double.parseDouble(values[3].trim()));
                    street_name.add(values[4].trim());
                    storey_range.add(values[5].trim());
                    floorAreas.add(Double.parseDouble(values[6].trim()));
                    flat_model.add(values[7].trim());
                    lease_commence_date.add(Double.parseDouble(values[8].trim()));
                    resalePrices.add(Double.parseDouble(values[9].trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid row: " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // for (int i = 0; i < 5 && i < months.size(); i++) {
        //     System.out.println("Row " + i + ": Month=" + months.get(i) +
        //             ", Town=" + towns.get(i) +
        //             ", FloorArea=" + floorAreas.get(i) +
        //             ", ResalePrice=" + resalePrices.get(i));
        // }
        
    }

    private List<Integer> getFilteredIndexes(String targetTown, int year, int startMonth) {
        List<Integer> indexes = new ArrayList<>();
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        int nextYear = (startMonth == 12) ? year + 1 : year;
    
        // Debug
        System.out.println("Filtering for: " + targetTown + 
                           " | Year: " + year + 
                           " | Start Month: " + startMonth + 
                           " | Next Month: " + nextMonth + 
                           " | Next Year: " + nextYear);
        
        for (int i = 0; i < towns.size(); i++) {
            String[] dateParts = months.get(i).split("-");
            int dataYear = Integer.parseInt(dateParts[0]);
            int dataMonth = Integer.parseInt(dateParts[1]);
    
            if (towns.get(i).equalsIgnoreCase(targetTown) &&
                floorAreas.get(i) >= 80 &&
                ((dataYear == year && dataMonth == startMonth) ||
                 (dataYear == nextYear && dataMonth == nextMonth))) {
    
                indexes.add(i);
            }
        }

        // Debug
        System.out.println("Filtered Indexes: " + indexes);
        for (int index : indexes) {
            System.out.println("Index: " + index + " | Month: " + months.get(index) + 
                               " | Town: " + towns.get(index) + 
                               " | Price: " + resalePrices.get(index));
        }
    
        return indexes;
    }
    
    // Get list of resale prices matching criteria
    public List<Double> filterPrices(String targetTown, int year, int startMonth) {
        List<Integer> indexes = getFilteredIndexes(targetTown, year, startMonth);
        // Debug
        System.out.println("Indexes: " + indexes);
        List<Double> filteredPrices = new ArrayList<>();

        for (int index : indexes) {
            // Debug
            System.out.println("Index: " + index + ", Town: " + towns.get(index) + ", Year: " + year + ", Month: " + startMonth);
            filteredPrices.add(resalePrices.get(index));
        }
        return filteredPrices;
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

    // Get minimum price per square meter
    public double getMinPricePerSqm(String targetTown, int year, int startMonth) {
        List<Integer> indexes = getFilteredIndexes(targetTown, year, startMonth);
        double minPricePerSqm = Double.MAX_VALUE;

        for (int index : indexes) {
            double pricePerSqm = resalePrices.get(index) / floorAreas.get(index);
            if (pricePerSqm < minPricePerSqm) {
                minPricePerSqm = pricePerSqm;
            }
        }

        return (minPricePerSqm == Double.MAX_VALUE) ? 0 : minPricePerSqm;
    }
}
