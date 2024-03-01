package riskcalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EquivalenceClassesAnalyzer {

    public static void main(String[] args) {
        // Specify the path to your CSV file
        String csvFilePath = "riskcalculator\\test-data\\10000_rows.csv";

        try {
            // Read CSV file and create equivalence classes
            Map<String, Integer> equivalenceClasses = createEquivalenceClasses(csvFilePath);

            // Display sizes of equivalence classes
            displayEquivalenceClassSizes(equivalenceClasses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Integer> createEquivalenceClasses(String csvFilePath) throws IOException {
        Map<String, Integer> equivalenceClasses = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            // Read the header line to get column indices
            String[] headers = br.readLine().split(",");

            // Find indices of quasi-identifying variables
            int geschlechtIndex = indexOf(headers, "Geschlecht");
            int ageIndex = indexOf(headers, "Age");
            int inzidenzortIndex = indexOf(headers, "Inzidenzort");
            int geburtsdatumIndex = indexOf(headers, "Geburtsdatum");
            int diagnosedatumIndex = indexOf(headers, "Diagnosedatum");
            int diagnoseIcd10CodeIndex = indexOf(headers, "Diagnose_ICD10_Code");

            String line;
            while ((line = br.readLine()) != null) {
                // Split the line into columns
                String[] columns = line.split(",");

                // Concatenate the values of quasi-identifying variables to create a key
                String key = columns[geschlechtIndex] + "_" +
                             columns[ageIndex] + "_" +
                             columns[inzidenzortIndex]; 
                            //  + "_" +
                            //  columns[geburtsdatumIndex] + "_" +
                            //  columns[diagnosedatumIndex] + "_" +
                            //  columns[diagnoseIcd10CodeIndex];

                // Update the count in the equivalence class
                equivalenceClasses.put(key, equivalenceClasses.getOrDefault(key, 0) + 1);
            }
        }

        return equivalenceClasses;
    }

    private static void displayEquivalenceClassSizes(Map<String, Integer> equivalenceClasses) {
        for (Map.Entry<String, Integer> entry : equivalenceClasses.entrySet()) {
            System.out.println("Equivalence Class: " + entry.getKey() + ", Size: " + entry.getValue());
        }
    }

    private static int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1; // Return -1 if the value is not found
    }
}

