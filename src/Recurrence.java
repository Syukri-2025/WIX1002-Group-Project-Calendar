import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Recurrence class handles recurring events in the calendar.
 * It supports daily, weekly, and monthly repetition.
 */
public class Recurrence {

    // Enum to define types of recurrence
    public enum Frequency {
        DAILY,      // Event repeats every X days
        WEEKLY,     // Event repeats every X weeks
        MONTHLY     // Event repeats every X months
    }

    private Frequency frequency;        // Type of recurrence (daily, weekly, monthly)
    private int interval;               // Number of units between occurrences (e.g., every 2 days)
    private LocalDateTime endDate;      // Date when recurrence ends

    /**
     * Constructor to create a Recurrence object
     * @param frequency Type of recurrence
     * @param interval How often it repeats
     * @param endDate When recurrence ends
     */
    public Recurrence(Frequency frequency, int interval, LocalDateTime endDate) {
        this.frequency = frequency;
        this.interval = interval;
        this.endDate = endDate;
    }

    // ================== Getters and Setters ==================

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    // ================== Core Methods ==================

    /**
     * Generate all occurrences of a recurring event
     * starting from the given start date.
     * @param start Start date of the event
     * @return List of LocalDateTime for each occurrence
     */
    public List<LocalDateTime> generateOccurrences(LocalDateTime start) {
        List<LocalDateTime> occurrences = new ArrayList<>();  // List to store all occurrences
        LocalDateTime current = start;                        // Start from the event's start date

        // Loop until we reach the end date
        while (current.isBefore(endDate) || current.isEqual(endDate)) {
            occurrences.add(current);  // Add current date to list

            // Update the current date based on frequency and interval
            switch (frequency) {
                case DAILY:
                    current = current.plusDays(interval);   // Add X days
                    break;
                case WEEKLY:
                    current = current.plusWeeks(interval);  // Add X weeks
                    break;
                case MONTHLY:
                    current = current.plusMonths(interval); // Add X months
                    break;
            }
        }

        return occurrences;
    }

    /**
     * Check if this recurrence object represents a valid recurring event
     * @return true if the event is recurring, false otherwise
     */
    public boolean isRecurring() {
        // An event is recurring if frequency is set, interval > 0, and endDate is defined
        return frequency != null && interval > 0 && endDate != null;
    }
}