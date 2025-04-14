import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TownZoneMapper {
    private static final Map<String, List<Integer>> Zonees = new HashMap<>();
    
    /**
     * Initialize the town indexes
     * @param towns Array of town names in the order they appear in the data
     */
    public static void initialize(String[] towns) {
        if (towns == null || towns.length == 0) {
            return;
        }
        
        // Clear existing mappings
        Zonees.clear();
        
        // Find all occurrences of each town
        for (int i = 0; i < towns.length; i++) {
            String town = towns[i].toUpperCase();
            Zonees.computeIfAbsent(town, k -> new ArrayList<>()).add(i);
        }
        
        // Debug: Print the number of indexes for each town
        System.out.println("Town index mapping initialized with " + Zonees.size() + " towns");
        for (Map.Entry<String, List<Integer>> entry : Zonees.entrySet()) {
            System.out.println("Town: " + entry.getKey() + ", Indexes: " + entry.getValue().size());
        }
    }
    
    /**
     * Get all indexes for a given town
     * @param town The town name in uppercase
     * @return List of indexes where this town appears, or empty list if town doesn't exist
     */
    public static List<Integer> getZonees(String town) {
        List<Integer> indexes = Zonees.getOrDefault(town.toUpperCase(), new ArrayList<>());
        System.out.println("Found " + indexes.size() + " indexes for town: " + town);
        return indexes;
    }
    
    /**
     * Get the first index for a given town
     * @param town The town name in uppercase
     * @return The first index where this town appears, or -1 if the town doesn't exist
     */
    public static int getFirstZone(String town) {
        List<Integer> indexes = Zonees.get(town.toUpperCase());
        return indexes != null && !indexes.isEmpty() ? indexes.get(0) : -1;
    }
    
    /**
     * Check if a town exists in our mapping
     * @param town The town name to check
     * @return true if the town exists in our mapping, false otherwise
     */
    public static boolean hasTown(String town) {
        return Zonees.containsKey(town.toUpperCase());
    }
    
    /**
     * Get all available towns
     * @return Array of all town names
     */
    public static String[] getAllTowns() {
        return Zonees.keySet().toArray(new String[0]);
    }
    
    /**
     * Get the number of occurrences of a town
     * @param town The town name to check
     * @return The number of times this town appears in the data
     */
    public static int getTownCount(String town) {
        List<Integer> indexes = Zonees.get(town.toUpperCase());
        return indexes != null ? indexes.size() : 0;
    }
} 