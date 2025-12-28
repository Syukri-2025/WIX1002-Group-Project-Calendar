import java.util.ArrayList;


public class SchedulerApp {

    //--> using the arraylist so that program can be 
    //detected easily 

    private ArrayList<Event> events = new ArrayList<>();

     public SchedulerApp() {
    // This pulls the data from the CSV file into your list when the app starts
    this.events = new ArrayList<>(FileManager.loadEvents());
}
    //--> addEvent method
   public void addEvent(Event adding){
    events.add(adding);
    FileManager.saveEvents(this.events); // Save change to file!
}
    public Event findTitle(String title){
        
        for(Event adding : events ){
            if(adding.getTitle().equalsIgnoreCase(title))
                return adding;
        }
        return null; //---error 

    }
public void updateEvent(String oldTitle, Event update) {
    Event existing = findTitle(oldTitle);
    if (existing != null) {
        // Update the fields in the object
        existing.setTitle(update.getTitle());
        existing.setStart(update.getStart()); // Use the LocalDateTime setter
        existing.setDescription(update.getDescription());
        existing.setEnd(update.getEnd());

        // Step B: IMPORTANT - Sync changes to your CSV file
        // After updating the list, you must tell the file manager to rewrite the file
        FileManager.saveEvents(this.events); 
    } else {
        System.out.println("Event not found: " + oldTitle);
    }
}

    public void deleteEvent(String title){
    Event existing = findTitle(title);
    if(existing != null){
        events.remove(existing);
        FileManager.saveEvents(this.events); // Save change to file!
    }
}

public ArrayList<Event> getEvents(){
    return events;

}



    



   // public static void main(String[] args) {
    //     Scanner scanner = new Scanner(System.in) ;
    //    int option = 0;

    //    while (true) {

    //     System.out.println(" Choose an option: ");
    //     System.out.println(" 1. View ");
    //     System.out.println(" 2. Add  ");
    //     System.out.println(" 3. Exit  ");
    //     System.out.println(" Enter your option:  ");

    //     if(option==1){
    //         //called GUI under calendarView 

    //     } else if(option==2){
    //         System.out.println(" Event Title: ");
    //         String Title = scanner.nextLine();
    //         System.out.println(" Date: ");
    //         int Date = scanner.nextInt();
    //         //called Event file
            


    //     } else if(option == 3){
    //         System.out.println(" Existing window: ");
    //         break;

    //     }else{
    //         System.out.println("Invalid option. Please try again");
    //     }
        







    //    } scanner.close();
  //  }
}