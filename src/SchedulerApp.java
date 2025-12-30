import java.util.ArrayList;
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

     public SchedulerApp() {
    // This pulls the data from the CSV file into your list when the app starts
    this.events = new ArrayList<>(FileManager.loadEvents());
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
    }
}

public ArrayList<Event> getEvents(){
    return events;

}

public void checkReminders() {
    LocalDateTime now = LocalDateTime.now();

    for (Event e : events) {
        if (e.getStart() == null) continue;
        long mins = Duration.between(now, e.getStart()).toMinutes();
        if (mins >= 0 && mins <= 30) {
            System.out.println("Alert: You have an event in 30 minutes: " + e.getTitle());
        }
    }
}

public void showStatistics() {
        Analytics.printStatistics(events);
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
}