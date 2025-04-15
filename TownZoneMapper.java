import java.util.HashMap;
import java.util.Map;

public class TownZoneMapper {
    // Stores only the start and end index for each year
    private static final Map<Integer, int[]> yearIndexBounds = new HashMap<>();

    /**
     * Initialize the year index bounds (start and end index for each year)
     * @param months Array of month strings in the format "YYYY-MM"
     */
    public static void initialize(String[] months) {
        if (months == null || months.length == 0) return;

        yearIndexBounds.clear();

        for (int i = 0; i < months.length; i++) {
            String monthStr = months[i];
            if (monthStr.contains("-")) {
                String[] parts = monthStr.split("-");
                if (parts.length >= 1) {
                    try {
                        int year = Integer.parseInt(parts[0]);

                        // If it's the first time we've seen this year
                        if (!yearIndexBounds.containsKey(year)) {
                            yearIndexBounds.put(year, new int[] { i, i }); // start and end same for now
                        } else {
                            // Update only the end index
                            yearIndexBounds.get(year)[1] = i;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid year format in: " + monthStr);
                    }
                }
            }
        }

        // Debug: Print mappings
        System.out.println("Year bounds initialized:");
        for (Map.Entry<Integer, int[]> entry : yearIndexBounds.entrySet()) {
            int[] bounds = entry.getValue();
            System.out.println("Year: " + entry.getKey() + " Start: " + bounds[0] + ", End: " + bounds[1]);
        }
    }

    public static int getStartIndex(int year) {
        int[] bounds = yearIndexBounds.get(year);
        return bounds != null ? bounds[0] : -1;
    }

    public static int getEndIndex(int year) {
        int[] bounds = yearIndexBounds.get(year);
        return bounds != null ? bounds[1] : -1;
    }

    public static boolean hasYear(int year) {
        return yearIndexBounds.containsKey(year);
    }

    public static Integer[] getAllYears() {
        return yearIndexBounds.keySet().toArray(new Integer[0]);
    }

    public static Map<Integer, int[]> getYearIndexBounds() {
        return yearIndexBounds;
    }
}
