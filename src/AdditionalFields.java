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
     * Convert to CSV format with proper escaping
     */
    public String toCsvString() {
        return String.format("%d,%s,%s,%s", 
            eventId, 
            escapeCsvField(location), 
            escapeCsvField(category), 
            escapeCsvField(priority));
    }

    /**
     * Create from CSV line with proper parsing
     */
    public static AdditionalFields fromCsvLine(String csvLine) {
        String[] parts = parseCsvLine(csvLine);
        if (parts.length >= 4) {
            try {
                int id = Integer.parseInt(parts[0].trim());
                String loc = parts[1].trim();
                String cat = parts[2].trim();
                String pri = parts[3].trim();
                return new AdditionalFields(id, loc, cat, pri);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing additional fields: " + e.getMessage());
                return null;
            }
        }
        return null;
    }
    
    /**
     * Escape CSV field by replacing commas with placeholder
     */
    private static String escapeCsvField(String field) {
        if (field == null) return "";
        return field.replace(",", ";;");
    }
    
    /**
     * Parse CSV line handling escaped commas
     */
    private static String[] parseCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        // Restore commas that were escaped
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replace(";;", ",");
        }
        return parts;
    }
}
