public class MatriculationProcessor {
    private int yearDigit;
    private int month;
    private String town;

    // Constructor
    public MatriculationProcessor(String matricNo) {
        // Extract last 3 digits
        char lastDigitChar = matricNo.charAt(matricNo.length() - 2); // Last digit for year
        char secondLastDigitChar = matricNo.charAt(matricNo.length() - 3); // Second-last for month
        char thirdLastDigitChar = matricNo.charAt(matricNo.length() - 4); // Third-last for town

        // Debug
        System.out.println("Last Digit: " + lastDigitChar);
        System.out.println("Second Last Digit: " + secondLastDigitChar);
        System.out.println("Third Last Digit: " + thirdLastDigitChar);

        // Convert to integers
        this.yearDigit = Character.getNumericValue(lastDigitChar);
        int monthDigit = Character.getNumericValue(secondLastDigitChar);
        this.town = getTownFromDigit(Character.getNumericValue(thirdLastDigitChar));

        // Convert "0" to October (10)
        this.month = (monthDigit == 0) ? 10 : monthDigit;
    }

    public int getYearDigit() { return yearDigit; }
    public int getMonth() { return month; }
    public String getTown() { return town; }

    // Get town based on third-last digit
    private static String getTownFromDigit(int digit) {
        String[] towns = {"BEDOK", "BUKIT PANJANG", "CLEMENTI", "CHOA CHU KANG", "HOUGANG",
                          "JURONG WEST", "PASIR RIS", "TAMPINES", "WOODLANDS", "YISHUN"};
        return (digit >= 0 && digit < towns.length) ? towns[digit] : "UNKNOWN";
    }
}
