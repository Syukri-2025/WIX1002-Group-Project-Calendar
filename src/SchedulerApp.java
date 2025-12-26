import java.util.ArrayList;

public class SchedulerApp {

    //--> using the arraylist so that program can be 
    //detected easily 

    private ArrayList<Event> events = new ArrayList<>();

    //--> addEvent method
    public void addEvent(Event adding){

        events.add(adding); //kind of add the title and date the Event file has
    }

    public findTitle(String title){
        
        for(Event adding : events ){
            if(adding.getTitle().equalsIgnoreCase(title))
                return adding;
        }
        return null; //---error 

    }

    public void updateEvent(String oldTitle, Event update){
        Event existing = findTitle(oldTitle);
            if(existing =! null){
                existing.setTitle(update.getTitle());
                existing.setDate(update.getDate());

            }

    }

    public void deleteEvent(String title){
        Event existing = findTitle(title);

        if(existing =! null){
            events.remove(existing);

        }

    }

public ArrayList<Event> getEvent(){
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