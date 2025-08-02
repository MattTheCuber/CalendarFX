/*
 * CalendarFX MonthView Popover Time Field Fix - Reflection-based Patch
 * 
 * This patch provides a workaround for the issue where the entry modification 
 * popover closes prematurely when users attempt to edit time fields.
 * 
 * Usage:
 *   MonthView monthView = new MonthView();
 *   CalendarFXPopoverFix.apply(monthView);
 */
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.TimeField;
import com.calendarfx.view.popover.EntryDetailsView;
import com.calendarfx.view.popover.EntryPopOverContentPane;
import javafx.application.Platform;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;

import java.lang.reflect.Field;

public class CalendarFXPopoverFix {
    
    /**
     * Applies the popover fix to the given MonthView.
     * 
     * @param monthView the MonthView to apply the fix to
     */
    public static void apply(MonthView monthView) {
        monthView.setEntryDetailsPopOverContentCallback(param -> 
            new FixedEntryPopOverContentPane(param.getPopOver(), param.getDateControl(), param.getEntry())
        );
    }
    
    private static class FixedEntryPopOverContentPane extends EntryPopOverContentPane {
        private final PopOver popOver;
        
        public FixedEntryPopOverContentPane(PopOver popOver, DateControl dateControl, Entry<?> entry) {
            super(popOver, dateControl, entry);
            this.popOver = popOver;
            Platform.runLater(this::applyTimeFieldFix);
        }
        
        private void applyTimeFieldFix() {
            if (getExpandedPane() != null && getExpandedPane().getContent() instanceof EntryDetailsView) {
                EntryDetailsView details = (EntryDetailsView) getExpandedPane().getContent();
                try {
                    TimeField startTimeField = getTimeField(details, "startTimeField");
                    TimeField endTimeField = getTimeField(details, "endTimeField");
                    
                    if (startTimeField != null && endTimeField != null) {
                        addFocusTracking(startTimeField, startTimeField, endTimeField);
                        addFocusTracking(endTimeField, startTimeField, endTimeField);
                    }
                } catch (Exception e) {
                    System.err.println("CalendarFX Popover Fix: Could not apply time field fix - " + e.getMessage());
                }
            }
        }
        
        private TimeField getTimeField(EntryDetailsView detailsView, String fieldName) {
            try {
                Field field = EntryDetailsView.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (TimeField) field.get(detailsView);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("CalendarFX Popover Fix: Could not access field " + fieldName);
                return null;
            }
        }
        
        private void addFocusTracking(TimeField timeField, TimeField startTimeField, TimeField endTimeField) {
            timeField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    popOver.setAutoHide(false);
                } else {
                    Platform.runLater(() -> {
                        boolean anyTimeFieldFocused = startTimeField.isFocused() || endTimeField.isFocused();
                        if (!anyTimeFieldFocused) {
                            popOver.setAutoHide(true);
                        }
                    });
                }
            });
        }
    }
}