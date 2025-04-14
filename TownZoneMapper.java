import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TownZoneMapper {
    private static final Map<Integer, List<Integer>> yearIndexes = new HashMap<>();
    
    /**
     * Initialize the year indexes
     * @param months Array of month strings in the format "YYYY-MM"
     */
    public static void initialize(String[] months) {
        if (months == null || months.length == 0) {
            return;
        }
        
        // Clear existing mappings
        yearIndexes.clear();
        
        // Find all occurrences of each year
        for (int i = 0; i < months.length; i++) {
            String monthStr = months[i];
            if (monthStr.contains("-")) {
                String[] parts = monthStr.split("-");
                if (parts.length >= 1) {
                    try {
                        int year = Integer.parseInt(parts[0]);
                        yearIndexes.computeIfAbsent(year, k -> new ArrayList<>()).add(i);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid year format in: " + monthStr);
                    }
                }
            }
        }
        
        // Debug: Print the number of indexes for each year
        System.out.println("Year index mapping initialized with " + yearIndexes.size() + " years");
        for (Map.Entry<Integer, List<Integer>> entry : yearIndexes.entrySet()) {
            System.out.println("Year: " + entry.getKey() + ", Indexes: " + entry.getValue().size());
        }
    }
    
    /**
     * Get all indexes for a given year
     * @param year The year to get indexes for
     * @return List of indexes for the given year, or empty list if year doesn't exist
     */
    public static List<Integer> getYearIndexes(int year) {
        List<Integer> indexes = yearIndexes.getOrDefault(year, new ArrayList<>());
        System.out.println("Found " + indexes.size() + " indexes for year: " + year);
        return indexes;
    }
    
    /**
     * Get the first index for a given year
     * @param year The year to get the first index for
     * @return The first index for the given year, or -1 if the year doesn't exist
     */
    public static int getFirstYearIndex(int year) {
        List<Integer> indexes = yearIndexes.get(year);
        return indexes != null && !indexes.isEmpty() ? indexes.get(0) : -1;
    }
    
    /**
     * Get the last index for a given year
     * @param year The year to get the last index for
     * @return The last index for the given year, or -1 if the year doesn't exist
     */
    public static int getLastYearIndex(int year) {
        List<Integer> indexes = yearIndexes.get(year);
        return indexes != null && !indexes.isEmpty() ? indexes.get(indexes.size() - 1) : -1;
    }
    
    /**
     * Check if a year exists in our mapping
     * @param year The year to check
     * @return true if the year exists in our mapping, false otherwise
     */
    public static boolean hasYear(int year) {
        return yearIndexes.containsKey(year);
    }
    
    /**
     * Get all available years
     * @return Array of all years
     */
    public static Integer[] getAllYears() {
        return yearIndexes.keySet().toArray(new Integer[0]);
    }
    
    /**
     * Get the number of occurrences of a year
     * @param year The year to check
     * @return The number of times this year appears in the data
     */
    public static int getYearCount(int year) {
        List<Integer> indexes = yearIndexes.get(year);
        return indexes != null ? indexes.size() : 0;
    }
} 