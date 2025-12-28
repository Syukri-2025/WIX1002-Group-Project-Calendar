import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class CalendarView {
    private SchedulerApp app = new SchedulerApp();

    public void showMainMenu() {
        JFrame frame = new JFrame("Scheduler");
        JButton addButton = new JButton("Add Event");
        JButton updateButton = new JButton("Update Event");
        JButton deleteButton = new JButton("Delete Event");
        JButton viewButton = new JButton("View Events");

        addButton.addActionListener(adding -> showAddEventForm());
        updateButton.addActionListener(adding -> showUpdateEventForm());
        deleteButton.addActionListener(adding -> showDeleteEventForm());
        viewButton.addActionListener(adding -> showEvents());

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(viewButton);

        frame.add(panel);
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void showAddEventForm() {
    try {
        String title = JOptionPane.showInputDialog("Enter Event Title:");
        String description = JOptionPane.showInputDialog("Enter Description:");
        String dateStr = JOptionPane.showInputDialog("Enter Date (dd/mm/yyyy):");
        String timeStr = JOptionPane.showInputDialog("Enter Start Time (HH:mm):");
        String durationStr = JOptionPane.showInputDialog("Enter Duration (in hours):");

        if (title != null && dateStr != null && timeStr != null) {
            // 1. Parse Start Time
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime start = LocalDateTime.parse(dateStr + " " + timeStr, inputFormatter);
            
            // 2. Calculate End Time based on duration
            double hours = Double.parseDouble(durationStr);
            LocalDateTime end = start.plusMinutes((long)(hours * 60));

            // 3. Create ID (Using timestamp or list size as a simple ID)
            int id = (int)(System.currentTimeMillis() % 10000);

            // 4. Create Event object
            Event newEvent = new Event(id, title, description, start, end);
            
            // 5. Save via App -> FileManager
            app.addEvent(newEvent); 
            
            JOptionPane.showMessageDialog(null, "Event Saved to File!");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: Please check your input formats.");
    }
}
  private void showUpdateEventForm() {
    String oldTitle = JOptionPane.showInputDialog("Enter the title of the event to update:");
    
    // 1. Check if the event exists first
    Event existing = app.findTitle(oldTitle);
    
    if (existing != null) {
        // 2. Collect new information
        String newTitle = JOptionPane.showInputDialog("Enter new Event Title:", existing.getTitle());
        String newDesc = JOptionPane.showInputDialog("Enter new Description:", existing.getDescription());
        String newDate = JOptionPane.showInputDialog("Enter new Date (dd/mm/yyyy):");
        String newTime = JOptionPane.showInputDialog("Enter new Time (HH:mm):");

        try {
            // 3. Convert the String input into LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime start = LocalDateTime.parse(newDate + " " + newTime, formatter);
            LocalDateTime end = start.plusHours(1); // Default to 1 hour duration

            // 4. Create the update object (Reuse the same ID so the CSV file stays consistent)
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
        app.deleteEvent(title);
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
    frame.setSize(600, 400); 
    
    YearMonth ym = YearMonth.of(year, month);
    LocalDate firstDay = ym.atDay(1); 
    int daysInMonth = ym.lengthOfMonth(); 
    
    JPanel panel = new JPanel(new GridLayout(0, 7)); 

    // Day headers 
    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}; 
    for (String d : days) { 
        JLabel lbl = new JLabel(d, SwingConstants.CENTER); 
        lbl.setFont(new Font("Arial", Font.BOLD, 14)); 
        panel.add(lbl); 
    } 

    // Empty cells before the first day of the month
    int startDay = firstDay.getDayOfWeek().getValue(); // Mon=1 ... Sun=7 
    int offset = (startDay % 7); // Shift so Sunday = 0
    for (int i = 0; i < offset; i++) { 
        panel.add(new JLabel("")); 
    } 

    // Fill in the days of the month
    for (int day = 1; day <= daysInMonth; day++) { 
        JLabel lbl = new JLabel(String.valueOf(day), SwingConstants.CENTER); 
        
        // Highlight if an event exists on this specific date
        for (Event e : app.getEvents()) {
            LocalDateTime start = e.getStart();
            
            // Compare year, month, and day
            if (start.getYear() == year && 
                start.getMonthValue() == month && 
                start.getDayOfMonth() == day) {
                
                lbl.setOpaque(true);
                lbl.setBackground(Color.YELLOW); 
                lbl.setText(day + " *"); // Mark with asterisk
            }
        }
        panel.add(lbl);
    } 
    
    frame.add(panel); 
    frame.setVisible(true); 
}
        
    public static void main(String[] args) {

        new CalendarView().showMainMenu();

    }
}
