import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.StringReader; // Needed for Analysis class's StringRead method

public class StandaloneAnalysisTest {

    public static String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public static void main(String[] args) {
        // Relative paths to the cleaned CSV files from the root of the patient-management-system project
        String csvPath1 = "patient-management-system/cleaned_数据2_1/cleaned_数据1_1.csv";
        String csvPath2 = "patient-management-system/cleaned_数据2_1/cleaned_数据1_2.csv";
        String csvPath3 = "patient-management-system/cleaned_数据2_1/cleaned_数据1_3.csv";
        String csvPath4 = "patient-management-system/cleaned_数据2_1/cleaned_数据1_4.csv";

        try {
            System.out.println("Attempting to read CSV files...");
            String csvContent1 = readFileAsString(csvPath1);
            System.out.println("Successfully read: " + csvPath1 + " (length: " + csvContent1.length() + ")");
            String csvContent2 = readFileAsString(csvPath2);
            System.out.println("Successfully read: " + csvPath2 + " (length: " + csvContent2.length() + ")");
            String csvContent3 = readFileAsString(csvPath3);
            System.out.println("Successfully read: " + csvPath3 + " (length: " + csvContent3.length() + ")");
            String csvContent4 = readFileAsString(csvPath4);
            System.out.println("Successfully read: " + csvPath4 + " (length: " + csvContent4.length() + ")");

            System.out.println("\nCalling Analysis.Statistic()...");
            LinkedHashMap<String, Double> statistics = Analysis.Statistic(csvContent1, csvContent2, csvContent3, csvContent4);

            System.out.println("\n--- Statistics Results ---");
            if (statistics != null && !statistics.isEmpty()) {
                for (Map.Entry<String, Double> entry : statistics.entrySet()) {
                    System.out.println(entry.getKey() + ": " + String.format("%.2f", entry.getValue()));
                }
            } else {
                System.out.println("No statistics were generated.");
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An error occurred during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 