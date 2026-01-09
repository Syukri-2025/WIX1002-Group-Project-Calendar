import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import javax.swing.*;

public class SchedulerApp {

    //--> using the arraylist so that program can be 
    //detected easily 

    private ArrayList<Event> events = new ArrayList<>();
    private java.util.Map<Integer, AdditionalFields> additionalFieldsMap = new java.util.HashMap<>();

     public SchedulerApp() {
    // This pulls the data from the CSV file into your list when the app starts
    this.events = new ArrayList<>(FileManager.loadEvents());
    this.additionalFieldsMap = FileManager.loadAdditionalFields();

    // Load persisted notified reminders and prune IDs not belonging to current events
    java.util.Set<Integer> loaded = FileManager.loadNotifiedReminders();
    if (loaded != null) {
        notifiedReminders.addAll(loaded);
        java.util.Set<Integer> currentIds = new java.util.HashSet<>();
        for (Event ev : this.events) currentIds.add(ev.getId());
        notifiedReminders.retainAll(currentIds);
    }

    // Expire old notified reminders (older than 7 days based on event end)
    expireNotifiedReminders(7);

    // Detect missed reminders (reminder time in past but event start within last 1 day)
    java.util.List<Event> missed = new java.util.ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    for (Event ev : this.events) {
        if (ev.getReminderMinutes() <= 0) continue;
        if (notifiedReminders.contains(ev.getId())) continue;
        LocalDateTime reminderTime = ev.getStart().minusMinutes(ev.getReminderMinutes());
        if (reminderTime.isBefore(now) && ev.getStart().isAfter(now.minusDays(1))) {
            missed.add(ev);
        }
    }

    if (!missed.isEmpty()) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Missed reminders detected:\n\n");
            for (Event me : missed) {
                sb.append("• ").append(me.getTitle()).append(" at ")
                  .append(me.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            }
            String[] options = {"Notify now", "Mark as read", "Ignore"};
            int choice = JOptionPane.showOptionDialog(null, sb.toString(), "Missed Reminders",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                // Notify now: spawn background thread to show individual dialogs
                new Thread(() -> {
                    for (Event me : missed) {
                        triggerReminderForEvent(me, Duration.between(LocalDateTime.now(), me.getStart()).toMinutes());
                    }
                }).start();
            } else if (choice == 1) {
                for (Event me : missed) {
                    notifiedReminders.add(me.getId());
                }
                FileManager.saveNotifiedReminders(notifiedReminders);
            } else {
                // ignore
            }
        });
    }
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
            // Already notified? skip
            if (notifiedReminders.contains(e.getId())) continue;
            notifiedReminders.add(e.getId());
            FileManager.saveNotifiedReminders(notifiedReminders);

            String message = String.format("Reminder: \"%s\" starts in %d minute(s) at %s",
                e.getTitle(), mins, e.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Log and show popup on EDT
            System.out.println(message);
            javax.swing.SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, message, "Event Reminder", JOptionPane.INFORMATION_MESSAGE);
            });
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

    // -------- Reminder Service --------
    private ScheduledExecutorService reminderScheduler = null;
    private ScheduledExecutorService oneShotScheduler = null; // for snoozes / one-shot tasks
    private java.util.Set<Integer> notifiedReminders = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private java.util.Map<Integer, ScheduledFuture<?>> snoozeTasks = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Start reminder service to check for upcoming reminders every N seconds.
     * @param intervalSeconds interval in seconds (e.g., 60)
     */
    public void startReminderService(long intervalSeconds) {
        if (reminderScheduler != null && !reminderScheduler.isShutdown()) return;
        reminderScheduler = Executors.newSingleThreadScheduledExecutor();
        reminderScheduler.scheduleAtFixedRate(() -> checkReminders(), 0, Math.max(1, intervalSeconds), TimeUnit.SECONDS);

        if (oneShotScheduler == null || oneShotScheduler.isShutdown()) {
            oneShotScheduler = Executors.newSingleThreadScheduledExecutor();
        }
    }

    public void stopReminderService() {
        if (reminderScheduler != null) {
            reminderScheduler.shutdownNow();
            reminderScheduler = null;
        }
        if (oneShotScheduler != null) {
            oneShotScheduler.shutdownNow();
            oneShotScheduler = null;
        }
        // cancel any pending snooze tasks
        for (ScheduledFuture<?> f : snoozeTasks.values()) {
            if (f != null) f.cancel(true);
        }
        snoozeTasks.clear();
    }

    /**
     * Schedule a snooze for an event (cancels existing snooze for that event if present)
     */
    public void scheduleSnooze(int eventId, long minutes) {
        if (minutes <= 0) minutes = 5; // default
        // cancel existing
        cancelSnooze(eventId);
        if (oneShotScheduler == null || oneShotScheduler.isShutdown()) {
            oneShotScheduler = Executors.newSingleThreadScheduledExecutor();
        }
        ScheduledFuture<?> future = oneShotScheduler.schedule(() -> {
            Event ev = null;
            for (Event e : events) if (e.getId() == eventId) ev = e;
            if (ev != null) {
                triggerReminderForEvent(ev, Duration.between(LocalDateTime.now(), ev.getStart()).toMinutes());
            }
        }, minutes, TimeUnit.MINUTES);
        snoozeTasks.put(eventId, future);
    }

    public void cancelSnooze(int eventId) {
        ScheduledFuture<?> f = snoozeTasks.remove(eventId);
        if (f != null) f.cancel(true);
    }

    private void triggerReminderForEvent(Event e, long mins) {
        // If already notified skip
        if (notifiedReminders.contains(e.getId())) return;

        ReminderDialog.Result res = performReminderDialog(e, mins);
        if (res == null) return;

        if (res.action == ReminderDialog.Action.DISMISS) {
            notifiedReminders.add(e.getId());
            FileManager.saveNotifiedReminders(notifiedReminders);
        } else if (res.action == ReminderDialog.Action.SNOOZE) {
            scheduleSnooze(e.getId(), res.minutes);
        }
    }

    private ReminderDialog.Result performReminderDialog(Event e, long mins) {
        final ReminderDialog.Result[] holder = new ReminderDialog.Result[1];
        Runnable r = () -> holder[0] = ReminderDialog.show(e, mins);
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeAndWait(r);
            }
        } catch (Exception ex) {
            System.out.println("Error showing reminder dialog: " + ex.getMessage());
            return null;
        }
        return holder[0];
    }

    /**
     * Clear persisted notified reminders (useful for debugging or resetting state)
     */
    public void clearNotifiedReminders() {
        notifiedReminders.clear();
        FileManager.saveNotifiedReminders(notifiedReminders);
    }

    /**
     * Remove notified reminder IDs for events whose end date is older than given days
     */
    public void expireNotifiedReminders(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        java.util.Set<Integer> toRemove = new java.util.HashSet<>();
        for (Integer id : notifiedReminders) {
            for (Event e : events) {
                if (e.getId() == id && e.getEnd() != null && e.getEnd().isBefore(threshold)) {
                    toRemove.add(id);
                    break;
                }
            }
        }
        if (!toRemove.isEmpty()) {
            notifiedReminders.removeAll(toRemove);
            FileManager.saveNotifiedReminders(notifiedReminders);
        }
    }

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

