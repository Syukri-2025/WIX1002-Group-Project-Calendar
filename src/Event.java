import java.time.LocalDateTime;

public class Event {
    private int id;
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private Recurrence recurrence;
    private int reminderMinutes; // How many minutes before event to remind (0 = no reminder)

    // Constructor
    public Event(int id, String title, String description, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.reminderMinutes = 0; // Default: no reminder
    }
    
    // Constructor with reminder
    public Event(int id, String title, String description, LocalDateTime start, LocalDateTime end, int reminderMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.reminderMinutes = reminderMinutes;
    }

   

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    // Recurrence getter/setter
    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }
    
    // Reminder getter/setter
    public int getReminderMinutes() {
        return reminderMinutes;
    }
    
    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

}
