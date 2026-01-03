import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

    
    public static boolean isTimeSlotFree(
            List<Event> events,
            LocalDateTime newStart,
            LocalDateTime newEnd) {

        for (Event event : events) {
            if (newStart.isBefore(event.getEnd()) &&
                newEnd.isAfter(event.getStart())) {
                return false; // Conflict found
            }
        }
        return true; // No conflict
    }

    
    public static List<Event> searchByKeyword(
            List<Event> events,
            String keyword) {

        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            if (event.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                event.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(event);
            }
        }
        return result;
    }

   
    public static List<Event> searchByDate(
            List<Event> events,
            LocalDate date) {

        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            if (event.getStart().toLocalDate().equals(date)) {
                result.add(event);
            }
        }
        return result;
    }

    /**
     * Search events within a date range (inclusive)
     * @param events List of events to search
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of events that fall within the range
     */
    public static List<Event> searchByDateRange(
            List<Event> events,
            LocalDate startDate,
            LocalDate endDate) {

        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            LocalDate eventDate = event.getStart().toLocalDate();
            // Check if event date is within range (inclusive)
            if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                result.add(event);
            }
        }
        return result;
    }
    
    /**
     * Search events by additional fields (location, category, priority)
     * @param events List of events
     * @param fieldsMap Map of additional fields
     * @param keyword Keyword to search
     * @return List of matching events
     */
    public static List<Event> searchByAdditionalFields(
            List<Event> events,
            java.util.Map<Integer, AdditionalFields> fieldsMap,
            String keyword) {
        
        List<Event> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        for (Event event : events) {
            AdditionalFields fields = fieldsMap.get(event.getId());
            if (fields != null) {
                if (fields.getLocation().toLowerCase().contains(lowerKeyword) ||
                    fields.getCategory().toLowerCase().contains(lowerKeyword) ||
                    fields.getPriority().toLowerCase().contains(lowerKeyword)) {
                    result.add(event);
                }
            }
        }
        return result;
    }
}
