import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SchedulerApp {

    //--> using the arraylist so that program can be 
    //detected easily 

    private ArrayList<Event> events = new ArrayList<>();
    private java.util.Map<Integer, AdditionalFields> additionalFieldsMap = new java.util.HashMap<>();

     public SchedulerApp() {
    // This pulls the data from the CSV file into your list when the app starts
    this.events = new ArrayList<>(FileManager.loadEvents());
    this.additionalFieldsMap = FileManager.loadAdditionalFields();
}
    //--> addEvent method
   public void addEvent(Event adding){
    events.add(adding);
    FileManager.saveEvents(this.events); // Save change to file!
}
    public Event findTitle(String title){
        
        for(Event adding : events ){
            if(adding.getTitle().equalsIgnoreCase(title))
                return adding;
        }
        return null; //---error 

    }
public void updateEvent(String oldTitle, Event update) {
    Event existing = findTitle(oldTitle);
    if (existing != null) {
        // Update the fields in the object
        existing.setTitle(update.getTitle());
        existing.setStart(update.getStart()); // Use the LocalDateTime setter
        existing.setDescription(update.getDescription());
        existing.setEnd(update.getEnd());

        // Step B: IMPORTANT - Sync changes to your CSV file
        // After updating the list, you must tell the file manager to rewrite the file
        FileManager.saveEvents(this.events); 
    } else {
        System.out.println("Event not found: " + oldTitle);
    }
}

    public void deleteEvent(String title){
        Event existing = findTitle(title);
        if(existing != null){
            events.remove(existing);
            FileManager.saveEvents(this.events); // Save change to file!
            System.out.println("Deleted event: " + title + " (ID: " + existing.getId() + ")");
        } else {
            System.out.println("Event not found: " + title);
        }
    }
    
    /**
     * Delete all events with the given title
     * @param title Title of events to delete
     * @return Number of events deleted
     */
    public int deleteAllEventsByTitle(String title){
        int count = 0;
        java.util.Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if (event.getTitle().equalsIgnoreCase(title)) {
                iterator.remove();
                count++;
            }
        }
        if (count > 0) {
            FileManager.saveEvents(this.events);
            System.out.println("Deleted " + count + " event(s) with title: " + title);
        }
        return count;
    }

public ArrayList<Event> getEvents(){
    return events;

}

public void checkReminders() {
    LocalDateTime now = LocalDateTime.now();

    for (Event e : events) {
        if (e.getStart() == null) continue;
        long mins = Duration.between(now, e.getStart()).toMinutes();
        
        // Check if event is within the reminder window
        if (e.getReminderMinutes() > 0 && mins >= 0 && mins <= e.getReminderMinutes()) {
            System.out.println("Alert: You have an event in " + mins + " minutes: " + e.getTitle());
        }
    }
}

/**
 * Get upcoming events sorted by time
 * @param withinMinutes Only return events within this many minutes
 * @return List of upcoming events
 */
public java.util.List<Event> getUpcomingEvents(int withinMinutes) {
    LocalDateTime now = LocalDateTime.now();
    java.util.List<Event> upcoming = new java.util.ArrayList<>();
    
    for (Event e : events) {
        if (e.getStart() == null) continue;
        long mins = Duration.between(now, e.getStart()).toMinutes();
        
        // Event is in the future and within the time window
        if (mins >= 0 && mins <= withinMinutes) {
            upcoming.add(e);
        }
    }
    
    // Sort by start time
    upcoming.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));
    return upcoming;
}

public void showStatistics() {
        analytics.printStatistics(events);
    }

    // -------- Backup APIs --------
    private ScheduledExecutorService backupScheduler = null;

    /**
     * Create a one-off backup now and return the backup path or null if failed
     */
    public String backupEventsNow() {
        try {
            String path = FileManager.backupEvents(this.events);
            System.out.println("Backup created: " + path);
            return path;
        } catch (IOException e) {
            System.out.println("Backup failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restore events from a backup file
     * @param backupFileName name of the backup file
     * @return true if restore was successful
     */
    public boolean restoreFromBackup(String backupFileName) {
        try {
            String backupPath = "data/backups/" + backupFileName;
            List<Event> restoredEvents = FileManager.restoreFromBackup(backupPath);
            this.events = new ArrayList<>(restoredEvents);
            FileManager.saveEvents(this.events);
            System.out.println("Restored " + restoredEvents.size() + " events from " + backupPath);
            return true;
        } catch (IOException e) {
            System.out.println("Restore failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Start automatic backups every N minutes. If already running, this is a no-op.
     */
    public void startBackupService(long intervalMinutes) {
        if (backupScheduler != null && !backupScheduler.isShutdown()) return; // already running
        backupScheduler = Executors.newSingleThreadScheduledExecutor();
        backupScheduler.scheduleAtFixedRate(() -> {
            String path = backupEventsNow();
            if (path != null) System.out.println("Automatic backup saved to " + path);
        }, 0, Math.max(1, intervalMinutes), TimeUnit.MINUTES);
    }

    /** Stop the automatic backup service (if running) */
    public void stopBackupService() {
        if (backupScheduler != null) {
            backupScheduler.shutdownNow();
            backupScheduler = null;
        }
    }

    /**
     * Check if a new event conflicts with any existing events
     * @param start start time of new event
     * @param end end time of new event
     * @param excludeId event ID to exclude from check (for updates, use -1 to check all)
     * @return conflicting event if found, null otherwise
     */
    public Event hasConflict(LocalDateTime start, LocalDateTime end, int excludeId) {
        for (Event event : events) {
            if (event.getId() == excludeId) continue; // Skip the event being updated
            
            // Check if time ranges overlap
            // Events overlap if: start < event.end AND end > event.start
            if (start.isBefore(event.getEnd()) && end.isAfter(event.getStart())) {
                return event; // Found a conflict
            }
        }
        return null; // No conflict
    }



   // public static void main(String[] args) {
    //     Scanner scanner = new Scanner(System.in) ;
    //    int option = 0;

    //    while (true) {

    //     System.out.println(" Choose an option: ");
    //     System.out.println(" 1. View ");
    //     System.out.println(" 2. Add  ");
    //     System.out.println(" 3. Exit  ");
    //     System.out.println(" Enter your option:  ");

    //     if(option==1){
    //         //called GUI under calendarView 

    //     } else if(option==2){
    //         System.out.println(" Event Title: ");
    //         String Title = scanner.nextLine();
    //         System.out.println(" Date: ");
    //         int Date = scanner.nextInt();
    //         //called Event file
            


    //     } else if(option == 3){
    //         System.out.println(" Existing window: ");
    //         break;

    //     }else{
    //         System.out.println("Invalid option. Please try again");
    //     }
        







    //    } scanner.close();
  //  }
  
    // ================ ADDITIONAL FIELDS MANAGEMENT ================
    
    public void addAdditionalFields(int eventId, AdditionalFields fields) {
        additionalFieldsMap.put(eventId, fields);
        FileManager.saveAdditionalFields(additionalFieldsMap);
    }
    
    public AdditionalFields getAdditionalFields(int eventId) {
        return additionalFieldsMap.get(eventId);
    }
    
    public java.util.Map<Integer, AdditionalFields> getAllAdditionalFields() {
        return additionalFieldsMap;
    }
    
    public String backupAdditionalFieldsNow() {
        try {
            String path = FileManager.backupAdditionalFields(additionalFieldsMap);
            System.out.println("Additional fields backup created: " + path);
            return path;
        } catch (IOException e) {
            System.out.println("Additional fields backup failed: " + e.getMessage());
            return null;
        }
    }
    
    public boolean restoreAdditionalFieldsFromBackup(String backupFileName) {
        try {
            String backupPath = "data/backups/" + backupFileName;
            additionalFieldsMap = FileManager.restoreAdditionalFields(backupPath);
            FileManager.saveAdditionalFields(additionalFieldsMap);
            System.out.println("Restored additional fields from " + backupPath);
            return true;
        } catch (IOException e) {
            System.out.println("Restore additional fields failed: " + e.getMessage());
            return false;
        }
    }
}

