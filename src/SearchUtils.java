import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

    /**
     * Checks whether a new event time slot overlaps with existing events
     * @param events List of existing events
     * @param newStart Start time of new event
     * @param newEnd End time of new event
     * @return true if time slot is free, false if conflict exists
     */
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

    /**
     * Search events by keyword (title or description)
     * @param events List of events
     * @param keyword Keyword to search
     * @return List of matched events
     */
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

    /**
     * Search events by date
     * @param events List of events
     * @param date Date to search
     * @return List of events on the given date
     */
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
}
