import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;// Import Event class

public class FileManager {
    // Relative path to the file (works on all PCs)
    private static final String EVENT_FILE = "data/event.csv";
    
    // Standard Date Format found in your PDF (e.g., 2025-10-05T11:00:00)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    

    // loadEvents(): Reads data/event.csv line-by-line and converts text into Event objects.
    
    public static List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        File file = new File(EVENT_FILE);

        // Check if file exists first
        if (!file.exists()) {
            System.out.println("No saved events found. Starting with empty list.");
            return events;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Read line by line
            while ((line = br.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) continue;

                // Split by comma: "1,Meeting,Description,Date,Date"
                String[] data = line.split(",");

                // Safety check: ensure line has all 5 parts
                if (data.length >= 5) {
                    int id = Integer.parseInt(data[0].trim());
                    String title = data[1].trim();
                    String description = data[2].trim();
                    // Convert String dates back to LocalDateTime objects
                    LocalDateTime start = LocalDateTime.parse(data[3].trim(), DATE_FORMATTER);
                    LocalDateTime end = LocalDateTime.parse(data[4].trim(), DATE_FORMATTER);

                    // Create the object and add to list
                    events.add(new Event(id, title, description, start, end));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing data: " + e.getMessage());
        }
        
        return events;
    }

    
     // saveEvents(): Takes a list of events and writes them back to data/event.csv.
    
    public static void saveEvents(List<Event> events) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(EVENT_FILE))) {
            for (Event event : events) {
                // Build the CSV line: id,title,description,start,end
                String line = String.format("%d,%s,%s,%s,%s",
                    event.getEventId(),
                    event.getTitle(),
                    event.getDescription(),
                    event.getStartDateTime().format(DATE_FORMATTER),
                    event.getEndDateTime().format(DATE_FORMATTER)
                );
                
                // Write to file and go to next line
                bw.write(line);
                bw.newLine();
            }
            System.out.println("Events saved successfully!");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
// saveEvents(): Takes a list of events and writes them back to data/event.csv.
