import java.time.DayOfWeek;
import java.time.Month;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Analytics class provides statistics about events in the calendar.
 */
public class analytics {

    /**
     * Finds the busiest day of the week based on a list of events.
     * @param events List of all events
     * @return DayOfWeek that has the most events, or null if none
     */
    public static DayOfWeek busiestDayOfWeek(List<Event> events) {
        if (events == null || events.isEmpty()) return null;

        Map<DayOfWeek, Integer> dayCount = new HashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) dayCount.put(d, 0);

        for (Event e : events) {
            if (e == null || e.getStart() == null) continue;
            DayOfWeek day = e.getStart().getDayOfWeek();
            dayCount.put(day, dayCount.get(day) + 1);
        }

        DayOfWeek busiest = null; int max = -1;
        for (Map.Entry<DayOfWeek,Integer> ent : dayCount.entrySet()) {
            if (ent.getValue() > max) { max = ent.getValue(); busiest = ent.getKey(); }
        }
        return busiest;
    }

    /** Busiest hour (0-23) by start time */
    public static int busiestHour(List<Event> events) {
        if (events == null || events.isEmpty()) return -1;
        int[] counts = new int[24];
        for (Event e : events) {
            if (e == null || e.getStart() == null) continue;
            counts[e.getStart().getHour()]++;
        }
        int best = -1, bestCount = -1;
        for (int h=0; h<24; h++) if (counts[h] > bestCount) { bestCount = counts[h]; best = h; }
        return best;
    }

    /** Busiest month */
    public static Month busiestMonth(List<Event> events) {
        if (events == null || events.isEmpty()) return null;
        Map<Month, Integer> counts = new HashMap<>();
        for (Month m : Month.values()) counts.put(m, 0);
        for (Event e : events) {
            if (e == null || e.getStart() == null) continue;
            Month m = e.getStart().getMonth();
            counts.put(m, counts.get(m) + 1);
        }
        Month best = null; int bestCount = -1;
        for (Map.Entry<Month,Integer> ent : counts.entrySet()) {
            if (ent.getValue() > bestCount) { bestCount = ent.getValue(); best = ent.getKey(); }
        }
        return best;
    }

    /** Average events per week over the period covered by events */
    public static double averageEventsPerWeek(List<Event> events) {
        if (events == null || events.isEmpty()) return 0.0;
        Optional<LocalDateTime[]> bounds = findBounds(events);
        if (!bounds.isPresent()) return 0.0;
        LocalDateTime min = bounds.get()[0], max = bounds.get()[1];
        long days = ChronoUnit.DAYS.between(min, max) + 1;
        double weeks = Math.max(1.0, days / 7.0);
        return events.size() / weeks;
    }

    private static Optional<LocalDateTime[]> findBounds(List<Event> events) {
        LocalDateTime min = null, max = null;
        for (Event e : events) {
            if (e == null || e.getStart() == null) continue;
            if (min == null || e.getStart().isBefore(min)) min = e.getStart();
            if (max == null || e.getStart().isAfter(max)) max = e.getStart();
        }
        return (min == null) ? Optional.empty() : Optional.of(new LocalDateTime[] { min, max });
    }

    /** Longest event by duration (minutes) */
    public static Event longestEvent(List<Event> events) {
        if (events == null || events.isEmpty()) return null;
        Event best = null; long bestMins = -1;
        for (Event e : events) {
            if (e == null || e.getStart() == null || e.getEnd() == null) continue;
            long mins = Duration.between(e.getStart(), e.getEnd()).toMinutes();
            if (mins > bestMins) { bestMins = mins; best = e; }
        }
        return best;
    }

    /** Count recurring vs single events */
    public static Map<String,Integer> countRecurringVsSingle(List<Event> events) {
        Map<String,Integer> out = new HashMap<>(); out.put("recurring", 0); out.put("single", 0);
        if (events == null || events.isEmpty()) return out;
        for (Event e : events) {
            if (e == null) continue;
            if (e.getRecurrence() != null && e.getRecurrence().isRecurring()) out.put("recurring", out.get("recurring") + 1);
            else out.put("single", out.get("single") + 1);
        }
        return out;
    }

    /** Average event duration in minutes */
    public static double averageEventDuration(List<Event> events) {
        if (events == null || events.isEmpty()) return 0.0;
        long totalMinutes = 0;
        int count = 0;
        for (Event e : events) {
            if (e == null || e.getStart() == null || e.getEnd() == null) continue;
            totalMinutes += Duration.between(e.getStart(), e.getEnd()).toMinutes();
            count++;
        }
        return count > 0 ? (double) totalMinutes / count : 0.0;
    }

    /** Count events by category */
    public static Map<String, Integer> eventsByCategory(List<Event> events, Map<Integer, AdditionalFields> additionalFieldsMap) {
        Map<String, Integer> counts = new HashMap<>();
        if (events == null || additionalFieldsMap == null) return counts;
        
        for (Event e : events) {
            if (e == null) continue;
            AdditionalFields fields = additionalFieldsMap.get(e.getId());
            String category = (fields != null && fields.getCategory() != null && !fields.getCategory().isEmpty()) 
                ? fields.getCategory() : "No Category";
            counts.put(category, counts.getOrDefault(category, 0) + 1);
        }
        return counts;
    }

    /** Count events by priority */
    public static Map<String, Integer> eventsByPriority(List<Event> events, Map<Integer, AdditionalFields> additionalFieldsMap) {
        Map<String, Integer> counts = new HashMap<>();
        if (events == null || additionalFieldsMap == null) return counts;
        
        for (Event e : events) {
            if (e == null) continue;
            AdditionalFields fields = additionalFieldsMap.get(e.getId());
            String priority = (fields != null && fields.getPriority() != null && !fields.getPriority().isEmpty()) 
                ? fields.getPriority() : "No Priority";
            counts.put(priority, counts.getOrDefault(priority, 0) + 1);
        }
        return counts;
    }

    /** Count upcoming vs past events */
    public static Map<String, Integer> upcomingVsPastEvents(List<Event> events) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("upcoming", 0);
        counts.put("past", 0);
        if (events == null) return counts;
        
        LocalDateTime now = LocalDateTime.now();
        for (Event e : events) {
            if (e == null || e.getStart() == null) continue;
            if (e.getStart().isAfter(now)) {
                counts.put("upcoming", counts.get("upcoming") + 1);
            } else {
                counts.put("past", counts.get("past") + 1);
            }
        }
        return counts;
    }

    /** Distribution of events by time of day */
    public static Map<String, Integer> eventsByTimeOfDay(List<Event> events) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Morning (6-12)", 0);
        counts.put("Afternoon (12-18)", 0);
        counts.put("Evening (18-22)", 0);
        counts.put("Night (22-6)", 0);
        if (events == null) return counts;
        
        for (Event e : events) {
            if (e == null || e.getStart() == null) continue;
            int hour = e.getStart().getHour();
            if (hour >= 6 && hour < 12) {
                counts.put("Morning (6-12)", counts.get("Morning (6-12)") + 1);
            } else if (hour >= 12 && hour < 18) {
                counts.put("Afternoon (12-18)", counts.get("Afternoon (12-18)") + 1);
            } else if (hour >= 18 && hour < 22) {
                counts.put("Evening (18-22)", counts.get("Evening (18-22)") + 1);
            } else {
                counts.put("Night (22-6)", counts.get("Night (22-6)") + 1);
            }
        }
        return counts;
    }

    /** Print an extended statistics summary */
    public static void printStatistics(List<Event> events) {
        System.out.println("=== Calendar Statistics ===");
        System.out.println("Total Events: " + (events == null ? 0 : events.size()));
        System.out.println("Busiest Day of the Week: " + busiestDayOfWeek(events));
        int hr = busiestHour(events);
        System.out.println("Busiest Hour: " + (hr >= 0 ? hr + ":00" : "N/A"));
        System.out.println("Busiest Month: " + busiestMonth(events));
        System.out.printf("Average Events per Week: %.2f\n", averageEventsPerWeek(events));
        Event longest = longestEvent(events);
        if (longest != null) {
            long mins = Duration.between(longest.getStart(), longest.getEnd()).toMinutes();
            System.out.println("Longest Event: " + longest.getTitle() + " (" + mins + " minutes)");
        } else {
            System.out.println("Longest Event: N/A");
        }
        Map<String,Integer> counts = countRecurringVsSingle(events);
        System.out.println("Recurring Events: " + counts.get("recurring") + ", Single Events: " + counts.get("single"));
    }
}