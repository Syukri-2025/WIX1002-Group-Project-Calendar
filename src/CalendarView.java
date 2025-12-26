import javax.swing.*;

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
        String title = JOptionPane.showInputDialog("Enter Event Title:");
        String date = JOptionPane.showInputDialog("Enter Date (dd/mm/yyyy):");
        app.addEvent(new Event(title, date));
    }

    private void showUpdateEventForm() {
        String oldTitle = JOptionPane.showInputDialog("Enter the title of the event to update:");
        String newTitle = JOptionPane.showInputDialog("Enter new Event Title:");
        String newDate = JOptionPane.showInputDialog("Enter new Date (dd/mm/yyyy):");
        app.updateEvent(oldTitle, new Event(newTitle, newDate));
    }

    private void showDeleteEventForm() {
        String title = JOptionPane.showInputDialog("Enter the title of the event to delete:");
        app.deleteEvent(title);
    }

    private void showEvents() {
        StringBuilder sb = new StringBuilder("Events:\n");
        for (Event e : app.getEvents()) {
            sb.append("- ").append(e).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    public void showCalendar(int year, int month) { 
        JFrame frame = new JFrame("Calendar " + month + "/" + year);
        frame.setSize(600, 400); 
        YearMonth ym = YearMonth.of(year, month);
        LocalDate firstDay = ym.atDay(1); 
        int daysInMonth = ym.lengthOfMonth(); 
        JPanel panel = new JPanel(new GridLayout(0, 7)); // 7 columns 
        // Day headers 
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}; 
            for (String d : days) { 
                JLabel lbl = new JLabel(d, SwingConstants.CENTER); 
                lbl.setFont(new Font("Arial", Font.BOLD, 14)); 
                panel.add(lbl); 
            
            } 
            
            // Empty cells before first day 
            
            int startDay = firstDay.getDayOfWeek().getValue(); // Monday=1 ... Sunday=7 
            int offset = (startDay % 7); // shift so Sunday=0 
                    for (int i = 0; i < offset; i++) { 
                        panel.add(new JLabel("")); 
                    
                    } 
                    
                    // Days of month 
                for (int day = 1; day <= daysInMonth; day++) { 
                    String dayStr = String.valueOf(day); 
                    JLabel lbl = new JLabel(dayStr, SwingConstants.CENTER); 

                    // Highlight if event exists on this day 
                
                            for (Event e : app.getEvents()) { 
                                if (e.getDate().startsWith(String.format("%02d/%02d/%d", day, month, year))) { 
                                    lbl.setOpaque(true); 
                                    lbl.setBackground(Color.YELLOW); 
                                    lbl.setText(dayStr + " *"); // mark with * 
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
