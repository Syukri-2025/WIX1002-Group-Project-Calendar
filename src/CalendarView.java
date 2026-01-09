import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.io.*;

public class CalendarView {
    private SchedulerApp app = new SchedulerApp();

    /**
     * Generate the next sequential ID by finding the max existing ID and adding 1
     * 
     * @return Next available ID
     */
    private int generateNextId() {
        int maxId = 0;
        for (Event event : app.getEvents()) {
            if (event.getId() > maxId) {
                maxId = event.getId();
            }
        }
        return maxId + 1;
    }

    public void showMainMenu() {
        JFrame frame = new JFrame("Scheduler");
        JButton addButton = new JButton("Add Event");
        JButton updateButton = new JButton("Update Event");
        JButton deleteButton = new JButton("Delete Event");
        JButton viewButton = new JButton("View Events");
        JButton calendarButton = new JButton("Show Calendar");
        JButton weekButton = new JButton("Week View");
        JButton searchButton = new JButton("Search Events");
        JButton analyticsButton = new JButton("Analytics");
        JButton backupButton = new JButton("Backup Events");
        JButton restoreButton = new JButton("Restore Backup");
        JButton clearNotifiedButton = new JButton("Clear Notified Reminders");

        addButton.addActionListener(adding -> showAddEventForm());
        updateButton.addActionListener(adding -> showUpdateEventForm());
        deleteButton.addActionListener(adding -> showDeleteEventForm());
        viewButton.addActionListener(adding -> showEvents());
        calendarButton.addActionListener(adding -> showCalendarPrompt());
        weekButton.addActionListener(adding -> showWeekViewPrompt());
        searchButton.addActionListener(adding -> showSearchMenu());
        analyticsButton.addActionListener(adding -> showAnalytics());
        backupButton.addActionListener(adding -> performBackup());
        restoreButton.addActionListener(adding -> performRestore());
        clearNotifiedButton.addActionListener(a -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Clear all persisted notified reminders? This cannot be undone.", "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                app.clearNotifiedReminders();
                JOptionPane.showMessageDialog(null, "Cleared persisted notified reminders.", "Cleared", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 3, 10, 10));  // 3 columns, 10px horizontal gap, 10px vertical gap
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(viewButton);
        panel.add(calendarButton);
        panel.add(weekButton);
        panel.add(searchButton);
        panel.add(analyticsButton);
        panel.add(backupButton);
        panel.add(restoreButton);
        panel.add(clearNotifiedButton);

        frame.add(panel);
        frame.setSize(700, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                app.stopReminderService();
                app.stopBackupService();
            }
        });
        frame.setVisible(true);
    }

    private void performBackup() {
        try {
            String backupPath = app.backupEventsNow();
            String additionalBackupPath = app.backupAdditionalFieldsNow();

            if (backupPath != null) {
                File file = new File(backupPath);
                String absolutePath = file.getAbsolutePath();

                StringBuilder message = new StringBuilder();
                message.append("Events backup completed to:\n").append(absolutePath);

                if (additionalBackupPath != null) {
                    File additionalFile = new File(additionalBackupPath);
                    message.append("\n\nAdditional fields backup completed to:\n")
                            .append(additionalFile.getAbsolutePath());
                }

                JOptionPane.showMessageDialog(null,
                        message.toString(),
                        "Backup Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Backup failed. Please try again.",
                        "Backup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error during backup: " + e.getMessage(),
                    "Backup Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRestore() {
        try {
            String[] backupFiles = FileManager.listBackupFiles();

            if (backupFiles.length == 0) {
                JOptionPane.showMessageDialog(null,
                        "No backup files found in data/backups/",
                        "No Backups",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String selectedBackup = (String) JOptionPane.showInputDialog(
                    null,
                    "Select a backup file to restore:",
                    "Restore Backup",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    backupFiles,
                    backupFiles[backupFiles.length - 1] // Default to most recent
            );

            if (selectedBackup != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        "This will replace all current events with the backup.\nAre you sure?",
                        "Confirm Restore",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = app.restoreFromBackup(selectedBackup);
                    if (success) {
                        JOptionPane.showMessageDialog(null,
                                "Events restored successfully from " + selectedBackup,
                                "Restore Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Failed to restore from backup.",
                                "Restore Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error during restore: " + e.getMessage(),
                    "Restore Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCalendarPrompt() {
        // Get current date as default
        LocalDate today = LocalDate.now();

        String yearStr = JOptionPane.showInputDialog("Enter Year (e.g., 2026):", today.getYear());
        if (yearStr == null) return; // User cancelled

        String monthStr = JOptionPane.showInputDialog("Enter Month (1-12):", today.getMonthValue());
        if (monthStr == null) return; // User cancelled

        try {
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);

            if (month >= 1 && month <= 12) {
                showCalendar(year, month);
            } else {
                JOptionPane.showMessageDialog(null, "Month must be between 1 and 12");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid year or month format");
        }
    }

    private void showWeekViewPrompt() {
        LocalDate today = LocalDate.now();

        String dateStr = JOptionPane.showInputDialog(
                "Enter a date (dd/mm/yyyy) to see that week:",
                today.getDayOfMonth() + "/" + today.getMonthValue() + "/" + today.getYear());
        if (dateStr == null) return; // User cancelled

        try {
            // Normalize date format
            dateStr = dateStr.replace("-", "/");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate selectedDate = LocalDate.parse(dateStr, formatter);
            showWeekView(selectedDate);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid date format. Please use d/m/yyyy");
        }
    }

    private void showWeekView(LocalDate date) {
        // Calculate week start (Sunday)
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() % 7);

        StringBuilder weekView = new StringBuilder();
        weekView.append("=== Week of ").append(weekStart).append(" ===").append("\n\n");

        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        // Loop through each day of the week
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = weekStart.plusDays(i);
            weekView.append(currentDay.format(dayFormat)).append(": ");

            // Find events for this day
            java.util.List<Event> dayEvents = new java.util.ArrayList<>();
            for (Event event : app.getEvents()) {
                LocalDateTime eventStart = event.getStart();
                if (eventStart.toLocalDate().equals(currentDay)) {
                    dayEvents.add(event);
                }
            }

            // Display events or "No events"
            if (dayEvents.isEmpty()) {
                weekView.append("No events");
            } else {
                // Sort events by time
                dayEvents.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));

                for (int j = 0; j < dayEvents.size(); j++) {
                    Event evt = dayEvents.get(j);
                    if (j > 0)
                        weekView.append("           "); // Indent additional events
                    weekView.append(evt.getTitle())
                            .append(" (")
                            .append(evt.getStart().format(timeFormat))
                            .append(")");
                    if (j < dayEvents.size() - 1)
                        weekView.append("\n");
                }
            }
            weekView.append("\n");
        }

        // Display in a dialog
        JTextArea textArea = new JTextArea(weekView.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(null, scrollPane, "Week View", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAddEventForm() {
        try {
            String title = JOptionPane.showInputDialog("Enter Event Title:");
            if (title == null) return; // User cancelled

            String description = JOptionPane.showInputDialog("Enter Description:");
            if (description == null) return; // User cancelled

            String dateStr = JOptionPane.showInputDialog("Enter Date (dd/mm/yyyy):");
            if (dateStr == null) return; // User cancelled

            String timeStr = JOptionPane.showInputDialog("Enter Start Time (HH:mm):");
            if (timeStr == null) return; // User cancelled

            String durationStr = JOptionPane.showInputDialog("Enter Duration (in hours):");
            if (durationStr == null) return; // User cancelled

            if (title != null && dateStr != null && timeStr != null) {
                // 1. Parse Start Time - normalize date format (replace dashes with slashes)
                dateStr = dateStr.replace("-", "/");
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                LocalDateTime start = LocalDateTime.parse(dateStr + " " + timeStr, inputFormatter);

                // 2. Calculate End Time based on duration
                double hours = Double.parseDouble(durationStr);
                LocalDateTime end = start.plusMinutes((long) (hours * 60));

                // 3. Generate sequential ID (1, 2, 3, ...)
                int id = generateNextId();

                // 4. Create Event object
                Event newEvent = new Event(id, title, description, start, end);

                // 4.1 Check for time conflicts
                Event conflict = app.hasConflict(start, end, -1);
                if (conflict != null) {
                    int proceed = JOptionPane.showConfirmDialog(null,
                            "WARNING: This event conflicts with:\n" +
                                    "Title: " + conflict.getTitle() + "\n" +
                                    "Time: "
                                    + conflict.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                                    " - " + conflict.getEnd().format(DateTimeFormatter.ofPattern("HH:mm")) + "\n\n" +
                                    "Do you want to add it anyway?",
                            "Event Conflict Detected",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (proceed != JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(null, "Event not added.");
                        return; // Cancel adding the event
                    }
                }

                // 4.5 Ask for reminder setting
                String[] reminderOptions = { "No reminder", "15 minutes before", "30 minutes before",
                        "1 hour before", "1 day before", "Custom" };
                String reminderChoice = (String) JOptionPane.showInputDialog(null,
                        "Set a reminder for this event:",
                        "Event Reminder",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        reminderOptions,
                        reminderOptions[2]); // Default to 30 minutes

                if (reminderChoice != null) {
                    int reminderMinutes = 0;
                    switch (reminderChoice) {
                        case "15 minutes before":
                            reminderMinutes = 15;
                            break;
                        case "30 minutes before":
                            reminderMinutes = 30;
                            break;
                        case "1 hour before":
                            reminderMinutes = 60;
                            break;
                        case "1 day before":
                            reminderMinutes = 1440; // 24 hours
                            break;
                        case "Custom":
                            String customStr = JOptionPane.showInputDialog("Enter minutes before event:");
                            if (customStr != null) {
                                try {
                                    reminderMinutes = Integer.parseInt(customStr);
                                } catch (NumberFormatException e) {
                                    reminderMinutes = 30; // Default
                                }
                            }
                            break;
                    }
                    newEvent.setReminderMinutes(reminderMinutes);
                }

                // 4.6 Ask for additional fields
                int addFields = JOptionPane.showConfirmDialog(null,
                        "Do you want to add additional details (Location, Category, Priority)?",
                        "Additional Fields",
                        JOptionPane.YES_NO_OPTION);

                if (addFields == JOptionPane.YES_OPTION) {
                    String location = JOptionPane.showInputDialog("Enter Location (e.g., Meeting Room A):");

                    String[] categoryOptions = { "Work", "Personal", "Sport", "Study", "Other" };
                    String category = (String) JOptionPane.showInputDialog(null,
                            "Select Category:",
                            "Event Category",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            categoryOptions,
                            categoryOptions[0]);

                    String[] priorityOptions = { "High", "Medium", "Low" };
                    String priority = (String) JOptionPane.showInputDialog(null,
                            "Select Priority:",
                            "Event Priority",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            priorityOptions,
                            priorityOptions[1]); // Default to Medium

                    // Create and save additional fields
                    AdditionalFields additionalFields = new AdditionalFields(
                            id,
                            location != null ? location : "",
                            category != null ? category : "",
                            priority != null ? priority : "");
                    app.addAdditionalFields(id, additionalFields);
                }

                // 5. Ask if user wants to make this a recurring event
                int recurring = JOptionPane.showConfirmDialog(null,
                        "Is this a recurring event?",
                        "Recurrence",
                        JOptionPane.YES_NO_OPTION);

                if (recurring == JOptionPane.YES_OPTION) {
                    addRecurrenceToEvent(newEvent);
                }

                // 6. If event has recurrence, generate all occurrences and save them
                if (newEvent.getRecurrence() != null && newEvent.getRecurrence().isRecurring()) {
                    java.util.List<Event> recurringEvents = RecurrenceLogic.generateRecurringEvents(newEvent);
                    // Assign sequential IDs to each recurring event occurrence
                    for (Event recurEvent : recurringEvents) {
                        recurEvent.setId(generateNextId()); // Assign proper ID
                        app.addEvent(recurEvent);
                    }
                    JOptionPane.showMessageDialog(null,
                            "Recurring event created! Generated " + recurringEvents.size() + " occurrences.");
                } else {
                    // 7. Save single event via App -> FileManager
                    app.addEvent(newEvent);
                    JOptionPane.showMessageDialog(null, "Event Saved to File!");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: Please check your input formats.\n" + e.getMessage());
        }
    }

    private void addRecurrenceToEvent(Event event) {
        try {
            // Ask for recurrence type
            String[] options = { "DAILY", "WEEKLY", "MONTHLY" };
            String frequencyStr = (String) JOptionPane.showInputDialog(null,
                    "Select Recurrence Type:",
                    "Recurrence Frequency",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (frequencyStr == null)
                return; // User cancelled

            // Ask for interval (e.g., every 2 days, every 1 week)
            String intervalStr = JOptionPane.showInputDialog(
                    "Repeat every how many " + frequencyStr.toLowerCase() + "? (e.g., 1, 2, 3...):");
            if (intervalStr == null) return; // User cancelled

            int interval = Integer.parseInt(intervalStr);

            // Ask for end date
            String endDateStr = JOptionPane.showInputDialog(
                    "Enter End Date for recurrence (dd/mm/yyyy):");
            if (endDateStr == null) return; // User cancelled

            String endTimeStr = JOptionPane.showInputDialog(
                    "Enter End Time (HH:mm):");
            if (endTimeStr == null) return; // User cancelled

            // Normalize date format
            endDateStr = endDateStr.replace("-", "/");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime endDate = LocalDateTime.parse(endDateStr + " " + endTimeStr, formatter);

            // Create Recurrence object
            Recurrence.Frequency freq = Recurrence.Frequency.valueOf(frequencyStr);
            Recurrence recurrence = new Recurrence(freq, interval, endDate);

            // Attach to event
            event.setRecurrence(recurrence);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error setting up recurrence: " + e.getMessage());
        }
    }

    private void showUpdateEventForm() {
        String oldTitle = JOptionPane.showInputDialog("Enter the title of the event to update:");
        if (oldTitle == null) return; // User cancelled

        // 1. Check if the event exists first
        Event existing = app.findTitle(oldTitle);

        if (existing != null) {
            // 2. Collect new information
            String newTitle = JOptionPane.showInputDialog("Enter new Event Title:", existing.getTitle());
            if (newTitle == null) return; // User cancelled

            String newDesc = JOptionPane.showInputDialog("Enter new Description:", existing.getDescription());
            if (newDesc == null) return; // User cancelled

            String newDate = JOptionPane.showInputDialog("Enter new Date (dd/mm/yyyy):");
            if (newDate == null) return; // User cancelled

            String newTime = JOptionPane.showInputDialog("Enter new Time (HH:mm):");
            if (newTime == null) return; // User cancelled

            try {
                // 3. Convert the String input into LocalDateTime - normalize date format
                newDate = newDate.replace("-", "/");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                LocalDateTime start = LocalDateTime.parse(newDate + " " + newTime, formatter);
                LocalDateTime end = start.plusHours(1); // Default to 1 hour duration

                // 3.5 Check for time conflicts (exclude the current event being updated)
                Event conflict = app.hasConflict(start, end, existing.getId());
                if (conflict != null) {
                    int proceed = JOptionPane.showConfirmDialog(null,
                            "WARNING: This updated time conflicts with:\n" +
                                    "Title: " + conflict.getTitle() + "\n" +
                                    "Time: "
                                    + conflict.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                                    " - " + conflict.getEnd().format(DateTimeFormatter.ofPattern("HH:mm")) + "\n\n" +
                                    "Do you want to update it anyway?",
                            "Event Conflict Detected",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (proceed != JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(null, "Event not updated.");
                        return; // Cancel the update
                    }
                }

                // 4. Create the update object (Reuse the same ID so the CSV file stays
                // consistent)
                Event updatedEvent = new Event(existing.getId(), newTitle, newDesc, start, end);

                // 5. Send to SchedulerApp
                app.updateEvent(oldTitle, updatedEvent);

                JOptionPane.showMessageDialog(null, "Event updated successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid date/time format. Please use dd/mm/yyyy and HH:mm");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Event '" + oldTitle + "' not found.");
        }
    }

    private void showDeleteEventForm() {
        String title = JOptionPane.showInputDialog("Enter the title of the event to delete:");

        if (title == null || title.trim().isEmpty()) {
            return; // User cancelled or entered nothing
        }

        // Find all events with this title
        java.util.List<Event> matchingEvents = new java.util.ArrayList<>();
        for (Event e : app.getEvents()) {
            if (e.getTitle().equalsIgnoreCase(title)) {
                matchingEvents.add(e);
            }
        }

        if (matchingEvents.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Event '" + title + "' not found.",
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // If multiple events exist with the same title, show them with dates and let user choose
        if (matchingEvents.size() > 1) {
            // Create array of event descriptions with dates
            DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String[] eventOptions = new String[matchingEvents.size()];

            for (int i = 0; i < matchingEvents.size(); i++) {
                Event e = matchingEvents.get(i);
                eventOptions[i] = e.getTitle() + " - " + e.getStart().format(displayFormat) +
                                  " to " + e.getEnd().format(displayFormat);
            }

            // Create a vertical list with scroll pane
            JList<String> eventList = new JList<>(eventOptions);
            eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(eventList);
            scrollPane.setPreferredSize(new Dimension(500, 200));

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Found " + matchingEvents.size() + " events with title '" + title + "'. Select which one to delete:"), BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);

            JButton deleteSelectedBtn = new JButton("Delete Selected");
            JButton deleteAllBtn = new JButton("Delete ALL " + matchingEvents.size() + " occurrences");
            JButton cancelBtn = new JButton("Cancel");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(deleteSelectedBtn);
            buttonPanel.add(deleteAllBtn);
            buttonPanel.add(cancelBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            JDialog dialog = new JDialog((JFrame) null, "Multiple Events Found", true);
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(null);

            final int[] choice = {-1};

            deleteSelectedBtn.addActionListener(e -> {
                int selected = eventList.getSelectedIndex();
                if (selected >= 0) {
                    choice[0] = selected;
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Please select an event to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });

            deleteAllBtn.addActionListener(e -> {
                choice[0] = matchingEvents.size();
                dialog.dispose();
            });

            cancelBtn.addActionListener(e -> {
                choice[0] = -1;
                dialog.dispose();
            });

            dialog.setVisible(true);

            if (choice[0] >= 0 && choice[0] < matchingEvents.size()) {
                // Delete the specific event selected
                Event toDelete = matchingEvents.get(choice[0]);
                int confirm = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to delete this event?\n" +
                        toDelete.getTitle() + "\n" +
                        toDelete.getStart().format(displayFormat) + " to " +
                        toDelete.getEnd().format(displayFormat),
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    app.getEvents().remove(toDelete);
                    FileManager.saveEvents(app.getEvents());
                    JOptionPane.showMessageDialog(null,
                            "Event deleted successfully!\n" + (matchingEvents.size() - 1) + " occurrences remain.",
                            "Delete Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else if (choice[0] == matchingEvents.size()) {
                // Delete all occurrences
                int confirm = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to delete ALL " + matchingEvents.size() + " occurrences of '" + title + "'?",
                        "Confirm Delete All",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    int deleted = app.deleteAllEventsByTitle(title);
                    JOptionPane.showMessageDialog(null,
                            "Deleted all " + deleted + " occurrences of '" + title + "'.",
                            "Delete Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            // choice == matchingEvents.size() + 1 or -1 (closed dialog) = Cancel, do nothing
        } else {
            // Only one event with this title
            Event toDelete = matchingEvents.get(0);
            DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this event?\n" +
                    toDelete.getTitle() + "\n" +
                    toDelete.getStart().format(displayFormat) + " to " +
                    toDelete.getEnd().format(displayFormat),
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                app.deleteEvent(title);
                JOptionPane.showMessageDialog(null,
                        "Event '" + title + "' has been deleted successfully!",
                        "Delete Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void showEvents() {
        StringBuilder sb = new StringBuilder("Scheduled Events:\n");
        // Formatter to make the LocalDateTime look nice in the list
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Event event : app.getEvents()) {
            String formattedDate = event.getStart().format(displayFormat);
            sb.append("- ")
                    .append(event.getTitle())
                    .append(" [")
                    .append(formattedDate)
                    .append("]\n");
        }

        if (app.getEvents().isEmpty()) {
            sb.append("No events found.");
        }

        JOptionPane.showMessageDialog(null, sb.toString());
    }

    public void showCalendar(int year, int month) {
        JFrame frame = new JFrame("Calendar - " + month + "/" + year);
        frame.setSize(650, 550);
        frame.setLayout(new BorderLayout());

        YearMonth ym = YearMonth.of(year, month);
        LocalDate firstDay = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();

        JPanel calendarPanel = new JPanel(new GridLayout(0, 7));

        // Day headers
        String[] days = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            calendarPanel.add(lbl);
        }

        // Empty cells before the first day of the month
        int startDay = firstDay.getDayOfWeek().getValue(); // Mon=1 ... Sun=7
        int offset = (startDay % 7); // Shift so Sunday = 0
        for (int i = 0; i < offset; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // Collect events for the month to show in notes
        StringBuilder eventNotes = new StringBuilder();
        eventNotes.append("Events this month:\n");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        java.util.Map<Integer, java.util.List<Event>> eventsByDay = new java.util.HashMap<>();

        // Group events by day
        for (Event e : app.getEvents()) {
            LocalDateTime start = e.getStart();
            if (start.getYear() == year && start.getMonthValue() == month) {
                int day = start.getDayOfMonth();
                eventsByDay.computeIfAbsent(day, k -> new java.util.ArrayList<>()).add(e);
            }
        }

        // Fill in the days of the month
        for (int day = 1; day <= daysInMonth; day++) {
            JLabel lbl = new JLabel(String.valueOf(day), SwingConstants.CENTER);

            // Highlight if an event exists on this specific date
            if (eventsByDay.containsKey(day)) {
                lbl.setOpaque(true);
                lbl.setBackground(Color.YELLOW);
                lbl.setText(day + " *"); // Mark with asterisk

                // Add to notes
                for (Event e : eventsByDay.get(day)) {
                    eventNotes.append("* ").append(day).append(": ")
                            .append(e.getTitle())
                            .append(" (").append(e.getStart().format(timeFormat)).append(")\n");
                }
            }
            calendarPanel.add(lbl);
        }

        // Add calendar grid to frame
        frame.add(calendarPanel, BorderLayout.CENTER);

        // Add event notes at the bottom
        if (eventsByDay.isEmpty()) {
            eventNotes.append("No events scheduled this month.");
        }

        JTextArea notesArea = new JTextArea(eventNotes.toString());
        notesArea.setEditable(false);
        notesArea.setFont(new Font("Arial", Font.PLAIN, 11));
        notesArea.setBackground(new Color(240, 240, 240));
        notesArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(650, 120));
        frame.add(notesScroll, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        CalendarView calendarView = new CalendarView();

        // Show upcoming events notification on launch
        calendarView.showUpcomingEventsNotification();

        // Then show main menu
        calendarView.showMainMenu();
    }

    // ================ SEARCH FUNCTIONALITY ================

    private void showSearchMenu() {
        String[] options = { "Search by Date", "Search by Date Range", "Search by Keyword",
                "Search by Additional Fields" };
        String choice = (String) JOptionPane.showInputDialog(
                null,
                "Select search type:",
                "Search Events",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == null)
            return; // User cancelled

        switch (choice) {
            case "Search by Date":
                searchByDate();
                break;
            case "Search by Date Range":
                searchByDateRange();
                break;
            case "Search by Keyword":
                searchByKeyword();
                break;
            case "Search by Additional Fields":
                searchByAdditionalFields();
                break;
        }
    }

    private void searchByAdditionalFields() {
        String keyword = JOptionPane.showInputDialog(
                "Enter keyword to search in additional fields\n(Location, Category, or Priority):");

        if (keyword == null || keyword.trim().isEmpty())
            return;

        java.util.List<Event> results = SearchUtils.searchByAdditionalFields(
                app.getEvents(),
                app.getAllAdditionalFields(),
                keyword);
        displaySearchResults(results, "Events matching '" + keyword + "' in additional fields");
    }

    private void searchByDate() {
        try {
            String dateStr = JOptionPane.showInputDialog(
                    "Enter date to search (d/m/yyyy):",
                    LocalDate.now().getDayOfMonth() + "/" + LocalDate.now().getMonthValue() + "/"
                            + LocalDate.now().getYear());

            if (dateStr == null)
                return;

            // Normalize date format
            dateStr = dateStr.replace("-", "/");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate searchDate = LocalDate.parse(dateStr, formatter);

            java.util.List<Event> results = SearchUtils.searchByDate(app.getEvents(), searchDate);
            displaySearchResults(results, "Events on " + searchDate);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid date format. Please use d/m/yyyy");
        }
    }

    private void searchByDateRange() {
        try {
            String startStr = JOptionPane.showInputDialog("Enter start date (d/m/yyyy):");
            if (startStr == null)
                return;

            String endStr = JOptionPane.showInputDialog("Enter end date (d/m/yyyy):");
            if (endStr == null)
                return;

            // Normalize date formats
            startStr = startStr.replace("-", "/");
            endStr = endStr.replace("-", "/");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate startDate = LocalDate.parse(startStr, formatter);
            LocalDate endDate = LocalDate.parse(endStr, formatter);

            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(null, "End date must be after start date!");
                return;
            }

            java.util.List<Event> results = SearchUtils.searchByDateRange(app.getEvents(), startDate, endDate);
            displaySearchResults(results, "Events from " + startDate + " to " + endDate);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid date format. Please use d/m/yyyy");
        }
    }

    private void searchByKeyword() {
        String keyword = JOptionPane.showInputDialog("Enter keyword to search (title/description):");

        if (keyword == null || keyword.trim().isEmpty())
            return;

        java.util.List<Event> results = SearchUtils.searchByKeyword(app.getEvents(), keyword);
        displaySearchResults(results, "Events matching '" + keyword + "'");
    }

    private void displaySearchResults(java.util.List<Event> results, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append("=".repeat(50)).append("\n\n");

        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        if (results.isEmpty()) {
            sb.append("No events found.");
        } else {
            sb.append("Found ").append(results.size()).append(" event(s):\n\n");

            for (Event event : results) {
                sb.append("• ").append(event.getTitle()).append("\n");
                sb.append("  Date: ").append(event.getStart().format(displayFormat)).append("\n");
                sb.append("  Description: ").append(event.getDescription()).append("\n\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(null, scrollPane, "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }

    // ================ NOTIFICATION FUNCTIONALITY ================

    /**
     * Show upcoming events notification on program launch
     */
    private void showUpcomingEventsNotification() {
        // Check for events in the next 24 hours
        java.util.List<Event> upcoming = app.getUpcomingEvents(1440); // 24 hours = 1440 minutes

        if (upcoming.isEmpty()) {
            return; // No upcoming events, don't show notification
        }

        StringBuilder notification = new StringBuilder();
        notification.append("\u23F0 UPCOMING EVENTS REMINDER\n");
        notification.append("=".repeat(50)).append("\n\n");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Event event : upcoming) {
            long minutesUntil = java.time.Duration.between(now, event.getStart()).toMinutes();

            // Format the duration
            String duration;
            if (minutesUntil < 60) {
                duration = minutesUntil + " minutes";
            } else if (minutesUntil < 1440) {
                long hours = minutesUntil / 60;
                long mins = minutesUntil % 60;
                duration = hours + " hour" + (hours > 1 ? "s" : "");
                if (mins > 0)
                    duration += " " + mins + " min";
            } else {
                long days = minutesUntil / 1440;
                long hours = (minutesUntil % 1440) / 60;
                duration = days + " day" + (days > 1 ? "s" : "");
                if (hours > 0)
                    duration += " " + hours + " hour" + (hours > 1 ? "s" : "");
            }

            notification.append("\u2022 ").append(event.getTitle()).append("\n");
            notification.append("  Coming soon in: ").append(duration).append("\n");
            notification.append("  Date: ").append(event.getStart().format(timeFormat)).append("\n");

            if (event.getReminderMinutes() > 0) {
                notification.append("  \u23f0 Reminder set: ").append(event.getReminderMinutes())
                        .append(" min before\n");
            }
            notification.append("\n");
        }

        // Show in a dialog
        JTextArea textArea = new JTextArea(notification.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        textArea.setBackground(new Color(255, 255, 200)); // Light yellow background

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        JOptionPane.showMessageDialog(null, scrollPane,
                "You have " + upcoming.size() + " upcoming event(s)!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAnalytics() {
        java.util.List<Event> events = app.getEvents();
        java.util.Map<Integer, AdditionalFields> additionalFields = app.getAllAdditionalFields();

        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No events found. Add some events to see analytics!",
                    "Analytics",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Build the statistics report
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════\n");
        report.append("        CALENDAR ANALYTICS & INSIGHTS       \n");
        report.append("═══════════════════════════════════════════\n\n");

        // Basic Stats
        report.append("📊 BASIC STATISTICS\n");
        report.append("─────────────────────────────────────────\n");
        report.append(String.format("Total Events: %d\n", events.size()));

        // Busiest Day
        java.time.DayOfWeek busyDay = analytics.busiestDayOfWeek(events);
        report.append(String.format("Busiest Day of Week: %s\n",
                busyDay != null ? busyDay : "N/A"));

        // Busiest Hour
        int busyHour = analytics.busiestHour(events);
        report.append(String.format("Busiest Hour: %s\n",
                busyHour >= 0 ? String.format("%02d:00", busyHour) : "N/A"));

        // Busiest Month
        java.time.Month busyMonth = analytics.busiestMonth(events);
        report.append(String.format("Busiest Month: %s\n",
                busyMonth != null ? busyMonth : "N/A"));

        // Average events per week
        report.append(String.format("Average Events per Week: %.1f\n\n",
                analytics.averageEventsPerWeek(events)));

        // Duration Stats
        report.append("⏱️ DURATION ANALYSIS\n");
        report.append("─────────────────────────────────────────\n");
        report.append(String.format("Average Event Duration: %.1f minutes\n",
                analytics.averageEventDuration(events)));

        Event longest = analytics.longestEvent(events);
        if (longest != null) {
            long mins = java.time.Duration.between(longest.getStart(), longest.getEnd()).toMinutes();
            report.append(String.format("Longest Event: \"%s\" (%d min)\n\n",
                    longest.getTitle(), mins));
        } else {
            report.append("Longest Event: N/A\n\n");
        }

        // Recurring vs Single
        report.append("🔄 EVENT TYPES\n");
        report.append("─────────────────────────────────────────\n");
        java.util.Map<String, Integer> recCounts = analytics.countRecurringVsSingle(events);
        report.append(String.format("Recurring Events: %d\n", recCounts.get("recurring")));
        report.append(String.format("Single Events: %d\n\n", recCounts.get("single")));

        // Upcoming vs Past
        report.append("📅 TIMELINE\n");
        report.append("─────────────────────────────────────────\n");
        java.util.Map<String, Integer> timeline = analytics.upcomingVsPastEvents(events);
        report.append(String.format("Upcoming Events: %d\n", timeline.get("upcoming")));
        report.append(String.format("Past Events: %d\n\n", timeline.get("past")));

        // Time of Day Distribution
        report.append("🌅 TIME OF DAY DISTRIBUTION\n");
        report.append("─────────────────────────────────────────\n");
        java.util.Map<String, Integer> timeOfDay = analytics.eventsByTimeOfDay(events);
        for (java.util.Map.Entry<String, Integer> entry : timeOfDay.entrySet()) {
            report.append(String.format("%-20s: %d events\n", entry.getKey(), entry.getValue()));
        }
        report.append("\n");

        // Category Breakdown
        report.append("📂 CATEGORY BREAKDOWN\n");
        report.append("─────────────────────────────────────────\n");
        java.util.Map<String, Integer> categories = analytics.eventsByCategory(events, additionalFields);
        if (categories.isEmpty()) {
            report.append("No categories assigned\n\n");
        } else {
            for (java.util.Map.Entry<String, Integer> entry : categories.entrySet()) {
                report.append(String.format("%-20s: %d events\n", entry.getKey(), entry.getValue()));
            }
            report.append("\n");
        }

        // Priority Distribution
        report.append("⚡ PRIORITY DISTRIBUTION\n");
        report.append("─────────────────────────────────────────\n");
        java.util.Map<String, Integer> priorities = analytics.eventsByPriority(events, additionalFields);
        if (priorities.isEmpty()) {
            report.append("No priorities assigned\n");
        } else {
            for (java.util.Map.Entry<String, Integer> entry : priorities.entrySet()) {
                report.append(String.format("%-20s: %d events\n", entry.getKey(), entry.getValue()));
            }
        }

        // Display in scrollable text area
        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 600));

        JOptionPane.showMessageDialog(null, scrollPane,
                "Calendar Analytics & Statistics",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

