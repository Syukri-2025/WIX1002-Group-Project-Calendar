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
}
