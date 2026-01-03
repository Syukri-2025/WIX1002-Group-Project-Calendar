/**
 * AdditionalFields holds custom fields for events
 * Stored separately in additional.csv
 */
public class AdditionalFields {
    private int eventId;
    private String location;      // field1: Event location
    private String category;      // field2: Event category (Work, Personal, Sport, etc.)
    private String priority;      // field3: Priority (High, Medium, Low)

    public AdditionalFields(int eventId, String location, String category, String priority) {
        this.eventId = eventId;
        this.location = location != null ? location : "";
        this.category = category != null ? category : "";
        this.priority = priority != null ? priority : "";
    }

    // Getters
    public int getEventId() {
        return eventId;
    }

    public String getLocation() {
        return location;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    // Setters
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setLocation(String location) {
        this.location = location != null ? location : "";
    }

    public void setCategory(String category) {
        this.category = category != null ? category : "";
    }

    public void setPriority(String priority) {
        this.priority = priority != null ? priority : "";
    }

    /**
     * Convert to CSV format
     */
    public String toCsvString() {
        return String.format("%d,%s,%s,%s", eventId, location, category, priority);
    }

    /**
     * Create from CSV line
     */
    public static AdditionalFields fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length >= 4) {
            int id = Integer.parseInt(parts[0].trim());
            String loc = parts[1].trim();
            String cat = parts[2].trim();
            String pri = parts[3].trim();
            return new AdditionalFields(id, loc, cat, pri);
        }
        return null;
    }
}
