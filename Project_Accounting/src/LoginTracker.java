import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LoginTracker {

    public static Set<Integer> readLoggedInDaysFromFile(String filename) {
        Set<Integer> days = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    int day = Integer.parseInt(line.trim());
                    if (day >= 1 && day <= 7) {
                        days.add(day);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid day entry in file: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read login file: " + e.getMessage());
        }
        return days;
    }
}
