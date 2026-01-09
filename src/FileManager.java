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
    // Use System.getProperty("user.dir") to ensure we're always in the project root
    private static final String FILE_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "event.csv";
    private static final String ADDITIONAL_FILE_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "additional.csv";
    
    // The format for dates (e.g., "2025-12-31T15:00:00")
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // CSV Helper Methods
    private static String escapeCsvField(String field) {
        if (field == null) return "";
        return field.replace(",", ";;");
    }
    
    private static String unescapeCsvField(String field) {
        if (field == null) return "";
        return field.replace(";;", ",");
    }

    // 2. LOADING (Read from File -> Create List)
    public static List<Event> loadEvents() {
        List<Event> eventList = new ArrayList<>();
        File file = new File(FILE_PATH);

        // DEBUG: Print file information
        System.out.println("=== LOADING EVENTS DEBUG ===");
        System.out.println("Looking for file at: " + FILE_PATH);
        System.out.println("Absolute path: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        
        // Step A: Safety Check - Does the file exist?
        if (!file.exists()) {
            System.out.println("No save file found. Starting a fresh calendar.");
            return eventList; // Return empty list
        }
        
        System.out.println("File found! Loading events...");

        // Step B: Read the file
        // "try" automatically closes the file when we are done (prevents errors)
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            
            String currentLine;
            // Loop: Read one line at a time until the file ends
            while ((currentLine = reader.readLine()) != null) {
                
                // Ignore empty lines
                if (currentLine.trim().isEmpty()) continue;

                // Step C: Break the line into pieces
                // Input:  "101, Study, Math revision, 2025-10-01T09:00, 2025-10-01T11:00, 30"
                // Result: ["101", "Study", "Math revision", "2025...", "2025...", "30"]
                String[] parts = currentLine.split(",");

                // Step D: Convert text to real data
                if (parts.length >= 5) {
                    // 1. Convert text "101" to number 101
                    int id = Integer.parseInt(parts[0].trim());
                    
                    // 2. Get the simple text parts (unescape commas)
                    String title = unescapeCsvField(parts[1].trim());
                    String description = unescapeCsvField(parts[2].trim());
                    
                    // 3. Convert text dates to Java Date objects
                    LocalDateTime start = LocalDateTime.parse(parts[3].trim(), DATE_FORMAT);
                    LocalDateTime end   = LocalDateTime.parse(parts[4].trim(), DATE_FORMAT);
                    
                    // 4. Get reminder minutes (default to 0 if not present)
                    int reminderMinutes = 0;
                    if (parts.length >= 6) {
                        try {
                            reminderMinutes = Integer.parseInt(parts[5].trim());
                        } catch (NumberFormatException e) {
                            reminderMinutes = 0;
                        }
                    }

                    // 5. Create the Event and add to our list
                    Event newEvent = new Event(id, title, description, start, end, reminderMinutes);
                    eventList.add(newEvent);
                }
            }
        } catch (Exception e) {// Catch any error that occurs during reading/parsing
            System.out.println("Error loading events: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Loaded " + eventList.size() + " event(s) from file.");
        System.out.println("=== END DEBUG ===");
        return eventList;
    }

    
    // 3. SAVING (Take List -> Write to File)
    public static void saveEvents(List<Event> eventsToSave) {
        
        File folder = new File(System.getProperty("user.dir") + File.separator + "data");
    if (!folder.exists()) {
        folder.mkdir(); 
    }
        // "BufferedWriter" is like a fast typewriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            
            for (Event event : eventsToSave) {
                
                // Step A: Combine all data into one comma-separated string
                // Example: "101,Study,Math revision,2025-10-01T09:00,2025-10-01T11:00,30"
                String csvLine = String.format("%d,%s,%s,%s,%s,%d",
                    event.getId(),
                    escapeCsvField(event.getTitle()),
                    escapeCsvField(event.getDescription()),
                    event.getStart().format(DATE_FORMAT),
                    event.getEnd().format(DATE_FORMAT),
                    event.getReminderMinutes()
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

    /**
     * Create a timestamped backup of the events CSV under data/backups
     * @param events list of events to write
     * @return path to the created backup file
     * @throws IOException if write fails
     */
    public static String backupEvents(List<Event> events) throws IOException {
        File backupsDir = new File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "backups");
        if (!backupsDir.exists()) {
            if (!backupsDir.mkdirs()) {
                throw new IOException("Could not create backups directory: " + backupsDir.getPath());
            }
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File backupFile = new File(backupsDir, "event-backup-" + timestamp + ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
            for (Event event : events) {
                String csvLine = String.format("%d,%s,%s,%s,%s,%d",
                        event.getId(),
                        escapeCsvField(event.getTitle()),
                        escapeCsvField(event.getDescription()),
                        event.getStart().format(DATE_FORMAT),
                        event.getEnd().format(DATE_FORMAT),
                        event.getReminderMinutes()
                );
                writer.write(csvLine);
                writer.newLine();
            }
        }

        return backupFile.getAbsolutePath();
    }

    /**
     * Restore events from a specific backup file
     * @param backupFilePath path to the backup file
     * @return List of events loaded from backup
     * @throws IOException if read fails
     */
    public static List<Event> restoreFromBackup(String backupFilePath) throws IOException {
        List<Event> eventList = new ArrayList<>();
        File file = new File(backupFilePath);

        if (!file.exists()) {
            throw new IOException("Backup file not found: " + backupFilePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().isEmpty()) continue;

                String[] parts = currentLine.split(",");
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0].trim());
                    String title = unescapeCsvField(parts[1].trim());
                    String description = unescapeCsvField(parts[2].trim());
                    LocalDateTime start = LocalDateTime.parse(parts[3].trim(), DATE_FORMAT);
                    LocalDateTime end = LocalDateTime.parse(parts[4].trim(), DATE_FORMAT);
                    
                    int reminderMinutes = 0;
                    if (parts.length >= 6) {
                        try {
                            reminderMinutes = Integer.parseInt(parts[5].trim());
                        } catch (NumberFormatException e) {
                            reminderMinutes = 0;
                        }
                    }

                    Event newEvent = new Event(id, title, description, start, end, reminderMinutes);
                    eventList.add(newEvent);
                }
            }
        }

        return eventList;
    }

    /**
     * List all backup files in the backups directory
     * @return Array of backup file names
     */
    public static String[] listBackupFiles() {
        File backupsDir = new File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "backups");
        if (!backupsDir.exists() || !backupsDir.isDirectory()) {
            return new String[0];
        }

        File[] files = backupsDir.listFiles((dir, name) -> name.endsWith(".csv"));
        if (files == null || files.length == 0) {
            return new String[0];
        }

        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }
        return fileNames;
    }
    
    // ================ ADDITIONAL FIELDS MANAGEMENT ================
    
    /**
     * Load additional fields from additional.csv
     * @return Map of eventId to AdditionalFields
     */
    public static java.util.Map<Integer, AdditionalFields> loadAdditionalFields() {
        java.util.Map<Integer, AdditionalFields> fieldsMap = new java.util.HashMap<>();
        File file = new File(ADDITIONAL_FILE_PATH);
        
        if (!file.exists()) {
            System.out.println("No additional fields file found.");
            return fieldsMap;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            boolean firstLine = true;
            
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().isEmpty()) continue;
                
                // Skip header line
                if (firstLine && currentLine.startsWith("eventId")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                
                AdditionalFields fields = AdditionalFields.fromCsvLine(currentLine);
                if (fields != null) {
                    fieldsMap.put(fields.getEventId(), fields);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading additional fields: " + e.getMessage());
        }
        
        return fieldsMap;
    }
    
    /**
     * Save additional fields to additional.csv
     * @param fieldsMap Map of eventId to AdditionalFields
     */
    public static void saveAdditionalFields(java.util.Map<Integer, AdditionalFields> fieldsMap) {
        File folder = new File(System.getProperty("user.dir") + File.separator + "data");
        if (!folder.exists()) {
            folder.mkdir();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ADDITIONAL_FILE_PATH))) {
            // Write header
            writer.write("eventId,location,category,priority");
            writer.newLine();
            
            // Write data
            for (AdditionalFields fields : fieldsMap.values()) {
                writer.write(fields.toCsvString());
                writer.newLine();
            }
            
            System.out.println("Additional fields saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving additional fields: " + e.getMessage());
        }
    }
    
    /**
     * Backup additional fields
     * @param fieldsMap Map of additional fields
     * @return Path to backup file
     * @throws IOException if backup fails
     */
    public static String backupAdditionalFields(java.util.Map<Integer, AdditionalFields> fieldsMap) throws IOException {
        File backupsDir = new File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "backups");
        if (!backupsDir.exists()) {
            if (!backupsDir.mkdirs()) {
                throw new IOException("Could not create backups directory");
            }
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File backupFile = new File(backupsDir, "additional-backup-" + timestamp + ".csv");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
            writer.write("eventId,location,category,priority");
            writer.newLine();
            
            for (AdditionalFields fields : fieldsMap.values()) {
                writer.write(fields.toCsvString());
                writer.newLine();
            }
        }
        
        return backupFile.getAbsolutePath();
    }
    
    /**
     * Restore additional fields from backup
     * @param backupFilePath Path to backup file
     * @return Map of restored additional fields
     * @throws IOException if restore fails
     */
    public static java.util.Map<Integer, AdditionalFields> restoreAdditionalFields(String backupFilePath) throws IOException {
        java.util.Map<Integer, AdditionalFields> fieldsMap = new java.util.HashMap<>();
        File file = new File(backupFilePath);
        
        if (!file.exists()) {
            throw new IOException("Backup file not found: " + backupFilePath);
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            boolean firstLine = true;
            
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().isEmpty()) continue;
                
                if (firstLine && currentLine.startsWith("eventId")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                
                AdditionalFields fields = AdditionalFields.fromCsvLine(currentLine);
                if (fields != null) {
                    fieldsMap.put(fields.getEventId(), fields);
                }
            }
        }
        
        return fieldsMap;
    }

    // ================ Notified reminders persistence ================
    private static final String NOTIFIED_FILE_PATH = "data/notified_reminders.txt";

    public static java.util.Set<Integer> loadNotifiedReminders() {
        java.util.Set<Integer> set = new java.util.HashSet<>();
        File file = new File(NOTIFIED_FILE_PATH);
        if (!file.exists()) return set;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    set.add(Integer.parseInt(line.trim()));
                } catch (NumberFormatException e) {
                    // skip invalid lines
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading notified reminders: " + e.getMessage());
        }
        return set;
    }

    public static void saveNotifiedReminders(java.util.Set<Integer> set) {
        File folder = new File("data");
        if (!folder.exists()) folder.mkdir();
        File file = new File(NOTIFIED_FILE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Integer id : set) {
                writer.write(String.valueOf(id));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving notified reminders: " + e.getMessage());
        }
    }
}
