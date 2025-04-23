import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class TownZoneMapper {
    // Stores only the start and end index for each year
    private static final Map<Integer, List<Integer>> yearIndexMap = new HashMap<>();
    // Stores list of row indices for each month-of-year (1-12)
    private static final Map<Integer, List<Integer>> monthIndexMap = new HashMap<>();

    /**
     * Initialize the year index bounds (start and end index for each year)
     * @param months Array of month strings in the format "YYYY-MM"
     */
    public static void initialize(String[] months) {
        if (months == null) return;
        yearIndexMap.clear();
        monthIndexMap.clear();
        for (int i = 0; i < months.length; i++) {
            String m = months[i];
            if (!m.contains("-")) continue;
            String[] parts = m.split("-");
            try {
                int year = Integer.parseInt(parts[0]);
                int monthVal = Integer.parseInt(parts[1]);
                yearIndexMap.computeIfAbsent(year, k -> new ArrayList<>()).add(i);
                monthIndexMap.computeIfAbsent(monthVal, k -> new ArrayList<>()).add(i);
            } catch (NumberFormatException e) {
                System.err.println("Invalid date format in: " + m);
            }
        }
        // Debug: Print mapping from year to list of indices
        // System.out.println("Year indices initialized:");
        // for (Map.Entry<Integer, List<Integer>> entry : yearIndexMap.entrySet()) {
        //     System.out.println("Year: " + entry.getKey() + " Indices: " + entry.getValue());
        // }
    }

    /**
     * Returns list of all row indices for the given year.
     */
    public static List<Integer> getYearIndices(int year) {
        return yearIndexMap.getOrDefault(year, Collections.emptyList());
    }

    /**
     * Returns list of all row indices for the given month-of-year (1-12).
     */
    public static List<Integer> getMonthIndices(int month) {
        return monthIndexMap.getOrDefault(month, Collections.emptyList());
    }

    public static boolean hasYear(int year) {
        return yearIndexMap.containsKey(year);
    }

    public static Integer[] getAllYears() {
        return yearIndexMap.keySet().toArray(new Integer[0]);
    }

    public static Map<Integer, List<Integer>> getYearIndexMap() {
        return yearIndexMap;
    }
}
