import javax.swing.*;

public class ReminderDialog {
    public enum Action { DISMISS, SNOOZE }

    public static class Result {
        public final Action action;
        public final int minutes; // valid if action == SNOOZE

        public Result(Action action, int minutes) {
            this.action = action;
            this.minutes = minutes;
        }
    }

    /**
     * Show the reminder dialog for the event. This method must be called on the EDT.
     */
    public static Result show(Event e, long minutesUntil) {
        String message = String.format("%s\nStarts in %d minute(s) at %s",
                e.getTitle(), minutesUntil,
                e.getStart().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        String[] options = {"Dismiss", "Snooze 5 min", "Snooze 10 min", "Snooze 15 min", "Snooze custom"};
        int choice = JOptionPane.showOptionDialog(null, message, "Event Reminder",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                options, options[1]);

        switch (choice) {
            case 0:
                return new Result(Action.DISMISS, 0);
            case 1:
                return new Result(Action.SNOOZE, 5);
            case 2:
                return new Result(Action.SNOOZE, 10);
            case 3:
                return new Result(Action.SNOOZE, 15);
            case 4: {
                String input = JOptionPane.showInputDialog(null, "Enter snooze minutes:", "Snooze", JOptionPane.PLAIN_MESSAGE);
                if (input == null) return new Result(Action.DISMISS, 0);
                try {
                    int mins = Integer.parseInt(input.trim());
                    if (mins <= 0) mins = 5;
                    return new Result(Action.SNOOZE, mins);
                } catch (NumberFormatException ex) {
                    return new Result(Action.SNOOZE, 5);
                }
            }
            default:
                return new Result(Action.DISMISS, 0);
        }
    }
}