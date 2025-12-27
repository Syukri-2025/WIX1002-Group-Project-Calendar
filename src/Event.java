import java.time.LocalDateTime;

public class Event {
    private int id;
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;

    // Constructor
    public Event(int id, String title, String description, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
    }

    public Event(String title2, String date) {
        //TODO Auto-generated constructor stub
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

    public Object getDate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDate'");
    }

    public void setDate(Object date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDate'");
    }

    public LocalDateTime getEndDateTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEndDateTime'");
    }

    public LocalDateTime getStartDateTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStartDateTime'");
    }

    public Object getEventId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEventId'");
    }
}