import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * RecurrenceLogic handles recurring events for Event objects.
 * It uses the Recurrence class to generate all occurrences.
 */
public class RecurrenceLogic {

    /**
     * Generate all recurring instances of an Event
     * @param event The original Event object
     * @return List of Event objects representing each occurrence
     */
    public static List<Event> generateRecurringEvents(Event event) {
        List<Event> recurringEvents = new ArrayList<>();

        // Check if the event has recurrence settings
        if (event.getRecurrence() != null && event.getRecurrence().isRecurring()) {
            Recurrence recurrence = event.getRecurrence();        // Get recurrence details
            LocalDateTime start = event.getStart();               // Original start time
            LocalDateTime end = event.getEnd();                   // Original end time

            // Guard: if start/end are missing, can't generate occurrences — return original
            if (start == null || end == null) {
                recurringEvents.add(event);
                return recurringEvents;
            }

            List<LocalDateTime> occurrences = recurrence.generateOccurrences(start);

            // Guard: if no occurrences were generated, return original
            if (occurrences == null || occurrences.isEmpty()) {
                recurringEvents.add(event);
                return recurringEvents;
            }

            // Loop through all generated dates to create new Event objects
            for (LocalDateTime occurrenceStart : occurrences) {
                // Calculate the duration of the original event
                long durationMinutes = Duration.between(start, end).toMinutes();
                LocalDateTime occurrenceEnd = occurrenceStart.plusMinutes(durationMinutes);

                // Create a new Event for this occurrence
                // Use id=0 so the caller / persistence layer can assign a new id if needed
                Event recurringEvent = new Event(
                        0,                             // New ID (0 means generate new)
                        event.getTitle(),              // Same title
                        event.getDescription(),        // Same description
                        occurrenceStart,               // Start date/time
                        occurrenceEnd                  // End date/time
                );

                // Do NOT copy the recurrence object to the generated instance by default.
                // This prevents accidental re-expansion of occurrences later.
                // If you do want them to carry recurrence metadata, uncomment the line below.
                // recurringEvent.setRecurrence(recurrence);

                recurringEvents.add(recurringEvent);
            }
        } else {
            // If no recurrence, just return the original event in the list
            recurringEvents.add(event);
        }

        return recurringEvents;
    }
}