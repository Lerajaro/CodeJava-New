package riskcalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;

public class EquivalenceClassesAnalyzer2 {
    // Declare ageDetailLevel as a class-level variable
    private static String ageDetailLevel;
    private static String[] inputHeaders;

    public static void main(String[] args) {
        // Specify the path to your CSV file
        String csvFilePath = "riskcalculator/test-data/100_rows.csv";

        try {
            // Read CSV file and create equivalence classes based on user input
            Map<String, Integer> equivalenceClasses = createEquivalenceClasses(csvFilePath);

            // Display sizes of equivalence classes
            displayEquivalenceClassSizes(equivalenceClasses);
            
            // // Print basic information about the dataset and chosen QIs
            // printDatasetInformation(csvFilePath, selectedQIs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] getDetailedAgeValues() {
        String ageCsvFilePath = "riskcalculator/hierarchies/age.csv";
        String[] ageHeaders;

        try (BufferedReader br = new BufferedReader(new FileReader(ageCsvFilePath))) {
            // Read the header line to get detailed age values
            String ageHeadersLine = br.readLine();
            ageHeaders = ageHeadersLine.split(";");

            // Display detailed age values to the user
            System.out.println("Choose a detailed Age value:");
            for (int i = 0; i < ageHeaders.length; i++) {
                System.out.println((i + 1) + ". " + ageHeaders[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
            ageHeaders = new String[0]; // In case of an error, set empty array
        }

        return ageHeaders;
    }

    private static int getUserChoice(String[] options) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.print("Enter your choice: ");
            try {
                choice = Integer.parseInt(scanner.nextLine()) - 1; // Adjust to 0-based index
                if (choice >= 0 && choice < options.length) {
                    break;
                } else {
                    System.out.println("Invalid choice. Please enter a number between 1 and " + options.length);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return choice;
    }

    private static String getAgeDetailLevel() {
        String[] ageHeaders = getDetailedAgeValues();
        if (ageHeaders.length == 0) {
            return null; // Return null in case of an error
        }

        int choice = getUserChoice(ageHeaders);
        return ageHeaders[choice]; // returns a String, like "5-Jahr"
    }

    // Modify the createEquivalenceClasses method

    private static Map<String, Integer> createEquivalenceClasses(String csvFilePath) throws IOException {
        Map<String, Integer> equivalenceClasses = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            // Read the header line to get column indices
            inputHeaders = br.readLine().split(",");

            // Get user input for QIs to consider
            int[] selectedQIIndices = getUserInput(inputHeaders);

            if (selectedQIIndices.length == 0) {
                System.out.println("No valid Quasi-Identifying Variables selected. Exiting.");
                return equivalenceClasses;
            }
            
            // Inside the main logic where Age is processed
            for (int selectedQI : selectedQIIndices) {
                if ("Age".equalsIgnoreCase(inputHeaders[selectedQI])) {
                    ageDetailLevel = getAgeDetailLevel();
                    if (ageDetailLevel != null) {
                        // Map the selected detail level to hierarchy values
                        //String mappedValue = mapToHierarchyValue(selectedQI, ageDetailLevel);
                        
                        // Use the mapped value in further processing
                        System.out.println("Chosen Age Detail Level: " + ageDetailLevel);
                        //System.out.println("Mapped Age Value: " + mappedValue);
                        
                        // Modify the code accordingly for your calculations
                    }
                }
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
            while ((line = br.readLine()) != null) {
                // Split the line into columns
                String[] columns = line.split(",");
                // Construct the key for each selected quasi-identifying variable
                StringBuilder keyBuilder = new StringBuilder();
                for (int index : selectedQIIndices) {
                    
                    // If the current QI is "Age," append both the QI value and the detailed level
                    if ("Age".equalsIgnoreCase(inputHeaders[index])) {
                        String hierarchyValue = mapToHierarchyValue(line, index, ageDetailLevel);
                        System.out.println("Mapped Age Value: " + hierarchyValue);
                        keyBuilder.append(hierarchyValue).append("_");
                    } else {
                        keyBuilder.append(columns[index]).append("_");
                    }
                }
                String key = keyBuilder.toString();
            
                // Debug print to check the constructed keys
                System.out.println("Constructed Key: " + key);
            
                // Update the count in the equivalence class
                equivalenceClasses.put(key, equivalenceClasses.getOrDefault(key, 0) + 1);
            }
            
            
        }

        return equivalenceClasses;
    }

    // Modify the getUserInput method

    private static int[] getUserInput(String[] availableQIs) {
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
        int[] selectedQIs = new int[selectedIndices.length];

        for (int i = 0; i < selectedIndices.length; i++) {
            try {
                selectedQIs[i] = Integer.parseInt(selectedIndices[i].trim()) - 1; // Adjust to 0-based index
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return new int[0]; // Return an empty array in case of an error
            }
        }

        return selectedQIs;
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

    // Add a new method for mapping the selected detailed level

    private static String mapToHierarchyValue(String inputLine, int selectedQI, String selectedDetailLevel) {
        try (BufferedReader br = new BufferedReader(new FileReader("riskcalculator/hierarchies/" + inputHeaders[selectedQI].toLowerCase() + ".csv"))) {
            System.out.println("Now Input-LINE: " + inputLine);
            String valueToBeUpdated = inputLine.split(",")[selectedQI];
            // Read the header line to get column names
            String[] headers = br.readLine().split(";");
            
            // Find the index of the selected detail level in the hierarchy file
            int detailLevelIndex = -1;
    
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase(selectedDetailLevel)) {
                    detailLevelIndex = i;
                    break;
                }
            }
    
            System.out.println("Selected Detail Level: " + selectedDetailLevel);
            System.out.println("Detail Level Index: " + detailLevelIndex);
    
            // Read the corresponding row for the selected detail level
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");  // Change this line to use semicolon as delimiter
                if (values[0].equals(valueToBeUpdated)) {
                    if (detailLevelIndex >= 0 && detailLevelIndex < values.length) {
                        return values[detailLevelIndex];
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
