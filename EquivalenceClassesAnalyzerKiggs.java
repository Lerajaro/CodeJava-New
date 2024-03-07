package riskcalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.util.ArrayList;


public class EquivalenceClassesAnalyzerKiggs {
    // Declare ageDetailLevel as a class-level variable
    private static String[] inputHeaders;
    private static int totalRows = 0;
    private static int rowsFilteredOut = 0;
    private static ArrayList<String> userQIChoice = new ArrayList<String>();

    public static void main(String[] args) {
        // Specify the path to your CSV file
        String csvFilePath = "riskcalculator/test-data/kiggs.csv";

        try {
            // Read CSV file and create equivalence classes based on user input
            Map<String, Integer> equivalenceClasses = createEquivalenceClasses(csvFilePath);

            // Display sizes of equivalence classes
            displayEquivalenceClassSizes(equivalenceClasses);
                // Get the threshold from the user
            int threshold = getUserThreshold();
            Map<String, Integer> filteredEquivalenceClasses = filterEquivalenceClasses(equivalenceClasses, threshold);
            displayEquivalenceClassSizes(filteredEquivalenceClasses);

            // printDatasetInformation(csvFilePath, selectedQIs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getUserThreshold() {
        Scanner scanner = new Scanner(System.in);
    
        System.out.print("Enter the threshold for minimum class size: ");
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static Map<String, Integer> filterEquivalenceClasses(Map<String, Integer> equivalenceClasses, int threshold) {
        // Create a new map to store filtered equivalence classes
        Map<String, Integer> filteredEquivalenceClasses = new HashMap<>();
    
        // Iterate through equivalenceClasses and add only those above the threshold
        for (Map.Entry<String, Integer> entry : equivalenceClasses.entrySet()) {
            if (entry.getValue() >= threshold) {
                filteredEquivalenceClasses.put(entry.getKey(), entry.getValue());
            } else {
                rowsFilteredOut += entry.getValue();
            }
        }
    
        return filteredEquivalenceClasses;
    }

    // Modify the createEquivalenceClasses method

    private static Map<String, Integer> createEquivalenceClasses(String csvFilePath) throws IOException {
        Map<String, Integer> equivalenceClasses = new HashMap<>();
        String[] qiStandardKiggsOptions = {"sex","sexa","age2","agegrpK","PAEagem_k","PAEagev_k","MIland","untj","untm","wo1","wo2","wob","mbtyp"};

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            // Take only the choices from the predefined Standard
            inputHeaders = br.readLine().split(",");

            // Get user input for QIs to consider
            ArrayList<Integer> selectedQIIndices = getUserInput(qiStandardKiggsOptions, inputHeaders);


            if (selectedQIIndices.isEmpty()) {
                System.out.println("No valid Quasi-Identifying Variables selected. Exiting.");
                return equivalenceClasses;
            }
            
            // Find indices of selected quasi-identifying variables
            for (int index : selectedQIIndices) {
                // Check if the index is -1 (not found), and skip iteration
                if (index == -1) {
                    System.out.println("Error: Quasi-Identifying Variable '" + inputHeaders[index] + "' not found.");
                    return equivalenceClasses;
                }
            }

            String line;
            // Now iterating over all lines of the input csv
            while ((line = br.readLine()) != null) {
                // Split the line into columns
                String[] columns = line.split(",");
                // Construct the key for each selected quasi-identifying variable
                StringBuilder keyBuilder = new StringBuilder();
                for (int index : selectedQIIndices) {

                    keyBuilder.append(columns[index]).append("_");
                }
                String key = keyBuilder.toString();
            
                // Debug print to check the constructed keys
                // System.out.println("Constructed Key: " + key);
            
                // Update the count in the equivalence class
                equivalenceClasses.put(key, equivalenceClasses.getOrDefault(key, 0) + 1);
            }
            
            
        }
        for (Map.Entry<String, Integer> entry : equivalenceClasses.entrySet()) {
            totalRows += entry.getValue();
        }
        return equivalenceClasses;
    }

    private static Map<String, Integer> getUniqueEquivalenceClasses(Map<String, Integer> equivalenceClasses) {
        Map<String, Integer> uniqueClasses = new HashMap<>();
    
        for (Map.Entry<String, Integer> entry : equivalenceClasses.entrySet()) {
            if (entry.getValue() == 1) {
                uniqueClasses.put(entry.getKey(), entry.getValue());
            }
        }
    
        return uniqueClasses;
    }
    // Modify the getUserInput method

    private static ArrayList<Integer> getUserInput(String[] availableQIs, String[] fullSetVariables) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Available Quasi-Identifying Variables:");
        for (int i = 0; i < availableQIs.length; i++) {
            System.out.println((i + 1) + ". " + availableQIs[i]);
        }

        String userInput;
        do {
            System.out.println("Enter the numbers of the QIs you want to consider (comma-separated):");
            userInput = scanner.nextLine();
        } while (!isValidInput(userInput, availableQIs.length));
        System.out.println("Your choice: ");

        // Split user input into selected QIs
        String[] selectedIndices = userInput.split(",");
        ArrayList<Integer> selectedQIsList = new ArrayList<>();

        for (String input : selectedIndices) {
            System.out.println("Now input = " + input);
            int idx = Integer.parseInt(input);
            try {
                String qiString = availableQIs[idx-1].toLowerCase(); // Convert to lowercase
                System.out.println("Selected QI: " + qiString);
                userQIChoice.add(qiString);
                for (int index = 0; index < fullSetVariables.length; index++) {
                    if (fullSetVariables[index].toLowerCase().equals(qiString)) {
                        System.out.println("Index of selected Qi: " + index);
                        selectedQIsList.add(index); // Adjust to 0-based index
                        break; // Stop searching once a match is found
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return new ArrayList<>(); // Return an empty array in case of an error
            }
        }
        
        System.out.println("Selected QI Indices: " + selectedQIsList);
        return selectedQIsList;
    }

    // Add a new method to validate user input

    private static boolean isValidInput(String userInput, int maxIndex) {
        String[] selectedIndices = userInput.split(",");
        for (String index : selectedIndices) {
            try {
                int idx = Integer.parseInt(index.trim()) - 1; // Adjust to 0-based index
                if (idx < 0 || idx >= maxIndex) {
                    System.out.println("Invalid index: " + (idx + 1));
                    return false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + index);
                return false;
            }
        }
        return true;
    }

  
    // Modify the displayEquivalenceClassSizes method

    private static void displayEquivalenceClassSizes(Map<String, Integer> equivalenceClasses) {
        int totalClasses = equivalenceClasses.size();
        int totalSizes = 0;
        int minSize = Integer.MAX_VALUE;
        int maxSize = Integer.MIN_VALUE;
        int minSizeCount = 0;
        int maxSizeCount = 0;

        for (Map.Entry<String, Integer> entry : equivalenceClasses.entrySet()) {
            int size = entry.getValue();
            totalSizes += size;

            if (size < minSize) {
                minSize = size;
                minSizeCount = 1;
            } else if (size == minSize) {
                minSizeCount++;
            }

            if (size > maxSize) {
                maxSize = size;
                maxSizeCount = 1;
            } else if (size == maxSize) {
                maxSizeCount++;
            }
        }

        double averageSize = (double) totalSizes / totalClasses;

        // Assuming equivalenceClasses is the map you want to filter
        Map<String, Integer> uniqueClasses = getUniqueEquivalenceClasses(equivalenceClasses);

        // Now you can iterate through uniqueClasses and process the cases
        for (Map.Entry<String, Integer> entry : uniqueClasses.entrySet()) {
            String uniqueKey = entry.getKey();
            int classSize = entry.getValue();
            // Process the unique case as needed
            System.out.println("Unique Key: " + uniqueKey + ", Class Size: " + classSize);
        }

        System.out.println("----- STATISTICS -----");
        System.out.println("Total Rows: " + totalRows);
        System.out.println("Rows filtered Out: " + rowsFilteredOut);
        System.out.println("Rows Remaining: " + (totalRows - rowsFilteredOut));
        System.out.println("QIs: " + userQIChoice);
        System.out.println("Number of Different Classes: " + totalClasses);
        System.out.println("Minimum Class Size: " + minSize + " (Count: " + minSizeCount + ")");
        System.out.println("Maximum Class Size: " + maxSize + " (Count: " + maxSizeCount + ")");
        System.out.println("Average Class Size: " + averageSize);
    }

    public static void TestStringArrays(String[] variables, String clarifyer) {
        System.out.println("\nTesting String-Array from " + clarifyer);
        int i = 0;
        for (String str : variables) {
            System.out.print(i + ". " + str + ", ");
            i += 1;
        }
        System.out.println("\n");
    }
}

