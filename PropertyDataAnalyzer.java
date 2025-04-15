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
    
    // Get list of resale prices matching criteria using year index
    public List<List<Double>> filterPricesWithYearIndexSharedScan(String targetTown, int year, int startMonth) {
        
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        int startYearIndex = TownZoneMapper.getStartIndex(year);
        int endYearIndex = TownZoneMapper.getEndIndex(year);
        List<Double> allPrices = new ArrayList<>();
        List<Integer> filteredPrices = new ArrayList<>();
        List<Double> allArea = new ArrayList<>();

        for (int i = startYearIndex; i <= endYearIndex; i++) {
       
                    String[] dateParts = months.get(i).split("-");
                    int dataMonth = Integer.parseInt(dateParts[1]);
                
                        if (towns.get(i).equals(targetTown)) {
                    if (floorAreas.get(i) >= 80 &&
                        (dataMonth == startMonth || dataMonth == nextMonth)) {

                        allPrices.add(resalePrices.get(i));
                        allArea.add(floorAreas.get(i));
                        // System.out.println(filteredPrices);
                        }
                    }
                }
                // System.out.println("filss"+filteredPrices.size());
                //     for (int j=0;j<filteredPrices.size();j++)
                //     {
                //         // System.out.println("fil"+filteredPrices.size());
                //             allPrices.add(resalePrices.get(filteredPrices.get(j)));
                //             allArea.add(floorAreas.get(filteredPrices.get(j)));
                //             // minPrice = getMinPrice(allPrices);
                //             // avgPrice = roundToTwoDecimalPlaces(getAveragePrice(allPrices));
                //             // sdPrice = roundToTwoDecimalPlaces(calculateStandardDeviation(allPrices));
                //             // sdArea = roundToTwoDecimalPlaces(getMinPricePerSqm(allPrices,allArea));
                      
                // }
                
        
            // List<Double> test = sharedScan(targetTown, year, startMonth);
            // System.out.println("1num"+allPrices.size());
        return Arrays.asList(allPrices,allArea);
    }
    
    public List<List<Double>> filterPricesWithYearIndex(String targetTown, int year, int startMonth) {
        
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        int startYearIndex = TownZoneMapper.getStartIndex(year);
        int endYearIndex = TownZoneMapper.getEndIndex(year);
        List<Double> allPrices = new ArrayList<>();
        List<Integer> filteredPrices = new ArrayList<>();
        List<Double> allArea = new ArrayList<>();


        for (int i = startYearIndex; i <= endYearIndex; i++) {
                    filteredPrices.add(i);
                    // System.out.println(filteredPrices);
        }

        for (int m = filteredPrices.size() - 1; m >= 0; m--) {
       
            String[] dateParts = months.get(filteredPrices.get(m)).split("-");
            
            int dataMonth = Integer.parseInt(dateParts[1]);
        
            if (dataMonth != startMonth && dataMonth != nextMonth) {
                    filteredPrices.remove(m);
                    // System.out.println(filteredPrices);
            }

        }

        for (int k = filteredPrices.size() - 1; k >= 0; k--) {
            if (!towns.get(filteredPrices.get(k)).equals(targetTown)) {
                filteredPrices.remove(k);
            }
        }

        for (int l = filteredPrices.size() - 1; l >= 0; l--) {
            if (floorAreas.get(filteredPrices.get(l)) < 80) {
                filteredPrices.remove(l);
            }
        }

                    for (int j=0;j<filteredPrices.size();j++)
                    {
                        // System.out.println("fil"+filteredPrices.size());
                            allPrices.add(resalePrices.get(filteredPrices.get(j)));
                            allArea.add(floorAreas.get(filteredPrices.get(j)));
                            // minPrice = getMinPrice(allPrices);
                            // avgPrice = roundToTwoDecimalPlaces(getAveragePrice(allPrices));
                            // sdPrice = roundToTwoDecimalPlaces(calculateStandardDeviation(allPrices));
                            // sdArea = roundToTwoDecimalPlaces(getMinPricePerSqm(allPrices,allArea));
                      
                }

                
        
            // List<Double> test = sharedScan(targetTown, year, startMonth);
            // System.out.println("2num"+filteredPrices.size());
        return Arrays.asList(allPrices,allArea);
    }
  


    // Get list of resale prices matching criteria without using year index
    public List<List<Double>> filterPricesWithoutYearIndex(String targetTown, int year, int startMonth) {
        List<Double> allPrices = new ArrayList<>();
        List<Integer> filteredPrices = new ArrayList<>();
        List<Double> allArea = new ArrayList<>();
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;

        for (int i = 0; i < towns.size(); i++) {
            if (towns.get(i).equals(targetTown)) {
                   filteredPrices.add(i);
            }
        }
      
        for (int m = filteredPrices.size() - 1; m >= 0; m--) {
            String[] dateParts = months.get(filteredPrices.get(m)).split("-");

            int dataMonth = Integer.parseInt(dateParts[1]);
             if (dataMonth != startMonth && dataMonth != nextMonth) {
                filteredPrices.remove(m);
            }
        }
        for (int k = filteredPrices.size() - 1; k >= 0; k--) {
            String[] dateParts = months.get(filteredPrices.get(k)).split("-");
            int dataYear = Integer.parseInt(dateParts[0]);

            if (dataYear != year) {
                filteredPrices.remove(k);
            }
        }
        for(int l = filteredPrices.size() - 1; l >= 0; l--) {
            if (floorAreas.get(filteredPrices.get(l)) < 80) {
                filteredPrices.remove(l);
            }
        }
        // System.out.println("3num"+filteredPrices.size());
        for (int j=0;j<filteredPrices.size();j++)
                    {
                        // System.out.println("fil"+filteredPrices.size());
                            allPrices.add(resalePrices.get(filteredPrices.get(j)));
                            allArea.add(floorAreas.get(filteredPrices.get(j)));
                      
                }

    
        return Arrays.asList(allPrices, allArea);
    }

    public List<List<Double>> filterPricesWithoutYearIndexSharedScan(String targetTown, int year, int startMonth) {
        List<Integer> filteredPrices = new ArrayList<>();
        int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
        int nextYear = (startMonth == 12) ? year + 1 : year;
        List<Double> allPrices = new ArrayList<>();
        List<Double> allArea = new ArrayList<>();

        for (int i = 0; i < towns.size(); i++) {
            if (towns.get(i).equals(targetTown)) {
                String[] dateParts = months.get(i).split("-");
                int dataYear = Integer.parseInt(dateParts[0]);
                int dataMonth = Integer.parseInt(dateParts[1]);
    
                if (floorAreas.get(i) >= 80 &&
                    ((dataYear == year && dataMonth == startMonth) ||
                     (dataYear == nextYear && dataMonth == nextMonth))) {
                    
                    allPrices.add(resalePrices.get(i));
                    allArea.add(floorAreas.get(i));
                }
            }
        }
// System.out.println("4num"+allPrices.size());
    
        return Arrays.asList(allPrices, allArea);
    }
    
    // public List<Double> sharedScan(String targetTown, int year, int startMonth) {
    //         List<Double> matchingPriceData = new ArrayList<>();
    //         List<Double> matchingAreaData = new ArrayList<>();
    //         List<Double> allPrices = new ArrayList<>();
    //         List<Double> allArea = new ArrayList<>();
    //         double minPrice = Double.MAX_VALUE;
    //         double totalPrice = 0.0;
    //         double minArea = Double.MAX_VALUE;
    //         double totalArea = 0.0;
    //         double avgPrice = Double.MAX_VALUE;
    //         double sdPrice = Double.MAX_VALUE;
    //         double sdArea = Double.MAX_VALUE;
       
    //         List<Integer> filteredPrices = new ArrayList<>();

    //         // int nextMonth = (startMonth == 12) ? 1 : startMonth + 1;
    //         // System.out.println(filteredPrices);
    //             try {

    //                 // int minprices.isEmpty() ? 0 : Collections.min(prices);

    //                 // for (int i = startYearIndex; i <= endYearIndex; i++) {
    //                 //     if (towns.get(i).equals(targetTown)) {
    //                 //         String[] dateParts = months.get(i).split("-");
    //                 //         int dataMonth = Integer.parseInt(dateParts[1]);
                
    //                 //         if (floorAreas.get(i) >= 80 &&
    //                 //             (dataMonth == startMonth || dataMonth == nextMonth)) {
    //                 //             filteredPrices.add(resalePrices.get(i));
    //                 //             prices.add(i);
    //                 //         }
    //                 //     }
    //                 // }
    //             //     for (int i = startYearIndex; i <= endYearIndex; i++) {
       
    //             //         String[] dateParts = months.get(i).split("-");
    //             //         int dataMonth = Integer.parseInt(dateParts[1]);
                    
    //             //             if (towns.get(i).equals(targetTown)) {
    //             //         if (floorAreas.get(i) >= 80 &&
    //             //             (dataMonth == startMonth || dataMonth == nextMonth)) {
    //             //             filteredPrices.add(i);
    //             //             // System.out.println(filteredPrices);
    //             //             }
    //             //         }
                  
    //             // }
                    
    //                 for (int i=0;i<filteredPrices.size();i++)
    //                 {
    //                     System.out.println("fil"+filteredPrices.size());
    //                             allPrices.add(resalePrices.get(filteredPrices.get(i)));
    //                             allArea.add(floorAreas.get(filteredPrices.get(i)));
    //                             minPrice = getMinPrice(allPrices);
    //                             avgPrice = roundToTwoDecimalPlaces(getAveragePrice(allPrices));
    //                             sdPrice = roundToTwoDecimalPlaces(calculateStandardDeviation(allPrices));
    //                            sdArea = roundToTwoDecimalPlaces(getMinPricePerSqm(allPrices,allArea));
                      
    //             }
    //             //  System.out.println(allPrices);
    //             // System.out.println("fil"+filteredPrices.size());
    //                 // System.out.println("min"+minPrice);
    //                 // System.out.println("avg"+avgPrice);
    //                 // System.out.println("sd"+sdPrice);
    //                 // System.out.println("min"+sdArea);
    //             } catch (Exception e) {
    //                 System.out.println("Error reading files: " + e.getMessage());
    //                 return null;
    //             }
        

    
    //         double avgArea = roundToTwoDecimalPlaces(totalArea / 10);


    //         return Arrays.asList(minPrice, minArea, avgPrice, avgArea, sdPrice, sdArea);
    //     }


    // public List<Double> filterPrices(String targetTown, int year, int startMonth) {
    //     return filterPricesWithYearIndex(targetTown, year, startMonth);
    // }

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
    public double getMinPricePerSqm(List<Double> prices, List<Double> area) {
        // List<Integer> indexes = filterPricesWithYearIndex(targetTown, year, startMonth);
        
        double minPricePerSqm = Double.MAX_VALUE;
        for (int i=0; i<prices.size();i++) {
            double pricePerSqm = prices.get(i) / area.get(i);
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