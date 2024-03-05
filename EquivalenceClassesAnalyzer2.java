package riskcalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;


public class EquivalenceClassesAnalyzer2 {
    // Declare ageDetailLevel as a class-level variable
    private static Map<String, String> chosenDetailLevels = new HashMap<>();
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

    private static String[] getDetailedQIValues(String QI) {
        // Implement similar logic as getDetailedAgeValues() for Inzidenzort
        String qiCsvFilePath = ("riskcalculator/hierarchies/" + QI.toLowerCase() +".csv");
        String[] qiHeaders;

        try (BufferedReader br = new BufferedReader(new FileReader(qiCsvFilePath))) {
            String qiHeadersLine = br.readLine();
            qiHeaders = qiHeadersLine.split(";");

            // Display detailed qi values to the user
            System.out.println("Choose a detailed " + QI + " value:");
            for (int i = 0; i < qiHeaders.length; i++) {
                System.out.println((i + 1) + ". " + qiHeaders[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            qiHeaders = new String[0]; // In case of an error, set empty array
        }
        return qiHeaders;
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

    private static String getQIDetailLevel(String QI) {
        String[] qiHeaders = getDetailedQIValues(QI);
        if (qiHeaders.length == 0) {
            return null; // Return null in case of an error
        }

        int choice = getUserChoice(qiHeaders);
        return qiHeaders[choice]; // returns a String, like "5-Jahr"
    }

    // Modify the createEquivalenceClasses method

    private static Map<String, Integer> createEquivalenceClasses(String csvFilePath) throws IOException {
        Map<String, Integer> equivalenceClasses = new HashMap<>();

        String[] redactionQIs = {"diagnose_icd10_code", "inzidenzort"};
        String[] dateQIs = {"diagnosedatum", "geburtsdatum"};

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            // Read the header line to get column indices
            inputHeaders = br.readLine().split(",");

            // Get user input for QIs to consider
            int[] selectedQIIndices = getUserInput(inputHeaders);

            for (int selectedQI : selectedQIIndices) {
                String QI = inputHeaders[selectedQI];
                String detailLevel = getQIDetailLevel(QI);
                if (detailLevel != null) {
                    chosenDetailLevels.put(QI, detailLevel);
                }
            }

            if (selectedQIIndices.length == 0) {
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
                    String QI = inputHeaders[index];
                    System.out.println("QI: " + QI);
                    String detailLevel = chosenDetailLevels.get(QI);
                    String hierarchyValue;
                    String hierarchyBuilder;
                    // If the current QI is "Age," append both the QI value and the detailed level
                    if (detailLevel != null) {
                        if(Arrays.asList(redactionQIs).contains(QI.toLowerCase())) {
                            hierarchyBuilder = "Redaction Based";
                            hierarchyValue =mapToRedactionHierarchyValue(line, index, detailLevel);
                        } else if (Arrays.asList(dateQIs).contains(QI.toLowerCase())) {
                            hierarchyBuilder = "Date Based";
                            hierarchyValue = mapToDateHierarchyValue(line, index, detailLevel);
                        } else {
                            hierarchyBuilder = "Regular CSV Based";
                            hierarchyValue = mapToHierarchyValue(line, index, detailLevel);
                        }
                        System.out.println("Hierarchy: " + hierarchyBuilder);
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
            String valueToBeUpdated = inputLine.split(",")[selectedQI];
            System.out.println("Input Value: " + valueToBeUpdated);

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
        
            // Read the corresponding row for the selected detail level
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");  // Change this line to use semicolon as delimiter
                if (values[0].equals(valueToBeUpdated)) {
                    if (detailLevelIndex >= 0 && detailLevelIndex < values.length) {
                        return values[detailLevelIndex];
                    }  else {
                        // Handle the case when detail level is not found (return a default value)
                        return "DEFAULT_VALUE"; // Change this to an appropriate default value
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static String mapToRedactionHierarchyValue(String inputLine, int selectedVariable, String selectedDetailLevel) {
        try (BufferedReader br = new BufferedReader(new FileReader("riskcalculator/hierarchies/" + inputHeaders[selectedVariable].toLowerCase() + ".csv"))) {
            //System.out.println("Now Input-LINE: " + inputLine);
            String valueToBeUpdated = inputLine.split(",")[selectedVariable];

            // Read the header line to get column names
            String[] headers = br.readLine().split(";");
            int lineLength = headers.length;
            // Find the index of the selected detail level in the hierarchy file
            int detailLevelIndex = Arrays.asList(headers).indexOf(selectedDetailLevel);
    
            //System.out.println("Selected Detail Level: " + selectedDetailLevel);
            // System.out.println("Detail Level Index: " + detailLevelIndex);

            // Read the corresponding row for the selected detail level
            String redactedValue = valueToBeUpdated.substring(0, lineLength-detailLevelIndex-1) + "*".repeat(detailLevelIndex);
            System.out.println("Input Value: " + valueToBeUpdated);
            System.out.println("Output Value: " + redactedValue);
            return redactedValue;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String mapToDateHierarchyValue(String inputLine, int selectedVariable, String selectedDetailLevel) {
        try (BufferedReader br = new BufferedReader(new FileReader("riskcalculator/hierarchies/" + inputHeaders[selectedVariable].toLowerCase() + ".csv"))) {
            String valueToBeUpdated = inputLine.split(",")[selectedVariable];
            System.out.println("Input Value: " + valueToBeUpdated);

            // Read the first line after the header to get detail levels
            String[] detailLevels = br.readLine().split(";");
            int detailLevelIndex = Arrays.asList(detailLevels).indexOf(selectedDetailLevel);

            // Read the corresponding row for the selected detail level
            String line;
            int rowCount = 0;
            while ((line = br.readLine()) != null) {
                rowCount++;
                if (rowCount == 1) {
                    String[] hierarchyLine = line.split(";");
                    String format = hierarchyLine[detailLevelIndex];
              
                    // Parse the date and apply the format based on the chosen detail level
                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat(format);
                    String suffix = "";
                    try {
                        Date date = inputDateFormat.parse(valueToBeUpdated);
                        // Adjust the format for Jahrzehnt to replace the last digit with "0s"
                        if (selectedDetailLevel.equals("Jahrzehnt")) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            int year = calendar.get(Calendar.YEAR);
                            year = (year / 10) * 10;  // Replace the last digit with "0"
                            calendar.set(Calendar.YEAR, year);
                            date = calendar.getTime();
                            suffix = "s";
                        }

                        String updatedValue = outputDateFormat.format(date) + suffix;
                        System.out.println("Updated Value: " + updatedValue);
                        return updatedValue;
                    } catch (ParseException e) {
                        e.printStackTrace();
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
