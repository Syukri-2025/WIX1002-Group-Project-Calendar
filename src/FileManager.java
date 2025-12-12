import java.io.*; 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; 
import java.util.List;

// NOTE: We don't import "org.w3c..." anymore. 
// We assume there is an "Event.java" file in the same folder as this file.

public class FileManager {
    
    // 1. SETTINGS
    // The file where we save our data
    private static final String FILE_PATH = "data/event.csv";
    
    // The format for dates (e.g., "2025-12-31T15:00:00")
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    // 2. LOADING (Read from File -> Create List)
    public static List<Event> loadEvents() {
        List<Event> eventList = new ArrayList<>();
        File file = new File(FILE_PATH);

        // Step A: Safety Check - Does the file exist?
        if (!file.exists()) {
            System.out.println("No save file found. Starting a fresh calendar.");
            return eventList; // Return empty list
        }

        // Step B: Read the file
        // "try" automatically closes the file when we are done (prevents errors)
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            
            String currentLine;
            
            // Loop: Read one line at a time until the file ends
            while ((currentLine = reader.readLine()) != null) {
                
                // Ignore empty lines
                if (currentLine.trim().isEmpty()) continue;

                // Step C: Break the line into pieces
                // Input:  "101, Study, Math revision, 2025-10-01T09:00, 2025-10-01T11:00"
                // Result: ["101", "Study", "Math revision", "2025...", "2025..."]
                String[] parts = currentLine.split(",");

                // Step D: Convert text to real data
                if (parts.length >= 5) {
                    // 1. Convert text "101" to number 101
                    int id = Integer.parseInt(parts[0].trim());
                    
                    // 2. Get the simple text parts
                    String title = parts[1].trim();
                    String description = parts[2].trim();
                    
                    // 3. Convert text dates to Java Date objects
                    LocalDateTime start = LocalDateTime.parse(parts[3].trim(), DATE_FORMAT);
                    LocalDateTime end   = LocalDateTime.parse(parts[4].trim(), DATE_FORMAT);

                    // 4. Create the Event and add to our list
                    Event newEvent = new Event(id, title, description, start, end);
                    eventList.add(newEvent);
                }
            }
        } catch (Exception e) {// Catch any error that occurs during reading/parsing
            System.out.println("Error loading events: " + e.getMessage());
        }
        
        return eventList;
    }

    
    // 3. SAVING (Take List -> Write to File)
    public static void saveEvents(List<Event> eventsToSave) {
        
        // "BufferedWriter" is like a fast typewriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            
            for (Event event : eventsToSave) {
                
                // Step A: Combine all data into one comma-separated string
                // Example: "101,Study,Math revision,2025-10-01T09:00,2025-10-01T11:00"
                String csvLine = String.format("%d,%s,%s,%s,%s",
                    event.getId(),
                    event.getTitle(),
                    event.getDescription(),
                    event.getStart().format(DATE_FORMAT),
                    event.getEnd().format(DATE_FORMAT)
                );
                
                // Step B: Write it to the file
                writer.write(csvLine);
                writer.newLine(); // Press "Enter" to go to next line
            }
            
            System.out.println("Saved successfully!");
            
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
}