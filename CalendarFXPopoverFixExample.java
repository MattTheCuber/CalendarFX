/*
 * Example application demonstrating the CalendarFX Popover Fix
 */
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.MonthView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CalendarFXPopoverFixExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a standard CalendarFX setup
        Calendar calendar = new Calendar("My Calendar");
        CalendarSource calendarSource = new CalendarSource("My Calendars");
        calendarSource.getCalendars().add(calendar);

        CalendarView calendarView = new CalendarView();
        calendarView.getCalendarSources().add(calendarSource);
        
        // Get the MonthView and apply the fix
        MonthView monthView = calendarView.getMonthPage().getDetailedMonthView();
        
        // Apply the popover fix - this is the key line!
        CalendarFXPopoverFix.apply(monthView);
        
        // Add a sample entry to test with
        Entry<String> entry = new Entry<>("Test Entry");
        entry.setInterval(java.time.LocalDate.now(), java.time.LocalTime.of(10, 0), 
                         java.time.LocalDate.now(), java.time.LocalTime.of(11, 0));
        calendar.addEntry(entry);

        Scene scene = new Scene(calendarView, 1200, 800);
        primaryStage.setTitle("CalendarFX with Popover Fix");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}