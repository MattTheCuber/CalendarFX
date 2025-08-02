/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.view.popover;

import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.Messages;
import com.calendarfx.view.TimeField;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.util.Objects;

import java.util.Objects;

public class EntryPopOverContentPane extends PopOverContentPane {

    private final Entry<?> entry;
    private final DateControl dateControl;
    private final PopOver popOver;

    private final InvalidationListener hideListener = it -> {
        if (getEntry().getCalendar() == null) {
            getPopOver().hide(Duration.ZERO);
        }
    };

    private final WeakInvalidationListener weakHideListener = new WeakInvalidationListener(hideListener);

    private final InvalidationListener fullDayListener = obs -> {
        if (getEntry().isFullDay() && !getPopOver().isDetached() && getDateControl() instanceof DayViewBase) {
            getPopOver().setDetached(true);
        }
    };

    private final WeakInvalidationListener weakFullDayListener = new WeakInvalidationListener(fullDayListener);

    public EntryPopOverContentPane(PopOver popOver, DateControl dateControl, Entry<?> entry) {
        getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

        this.popOver = popOver;
        this.dateControl = dateControl;
        this.entry = Objects.requireNonNull(entry);

        EntryDetailsView details = new EntryDetailsView(entry, dateControl);
        PopOverTitledPane detailsPane = new PopOverTitledPane(Messages.getString("EntryPopOverContentPane.DETAILS"), details);

        EntryHeaderView header = new EntryHeaderView(entry, dateControl.getCalendars());
        setHeader(header);

        if (Boolean.getBoolean("calendarfx.developer")) {
            EntryPropertiesView properties = new EntryPropertiesView(entry);
            PopOverTitledPane propertiesPane = new PopOverTitledPane("Properties", properties);
            propertiesPane.getStyleClass().add("no-padding");
            getPanes().addAll(detailsPane, propertiesPane);
        } else {
            getPanes().addAll(detailsPane);
        }

        setExpandedPane(detailsPane);

        // Add focus tracking for time fields to prevent premature popover closing
        addTimeFieldFocusTracking(details);

        entry.fullDayProperty().addListener(weakFullDayListener);
        popOver.setOnHidden(evt -> entry.fullDayProperty().removeListener(weakFullDayListener));

        entry.calendarProperty().addListener(weakHideListener);
    }

    public final PopOver getPopOver() {
        return popOver;
    }

    public final DateControl getDateControl() {
        return dateControl;
    }

    public final Entry<?> getEntry() {
        return entry;
    }

    /**
     * Adds focus tracking to time fields to prevent the popover from closing
     * while the user is actively editing time values.
     */
    private void addTimeFieldFocusTracking(EntryDetailsView details) {
        setupTimeFieldFocusTracking(details.getStartTimeField());
        setupTimeFieldFocusTracking(details.getEndTimeField());
    }

    private void setupTimeFieldFocusTracking(TimeField timeField) {
        timeField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Time field gained focus - temporarily disable auto-hide
                popOver.setAutoHide(false);
            } else {
                // Time field lost focus - re-enable auto-hide after ensuring no other time field has focus
                javafx.application.Platform.runLater(() -> {
                    // Check if any time field still has focus before re-enabling auto-hide
                    boolean anyTimeFieldFocused = false;
                    
                    // Look up the EntryDetailsView from the expanded pane content
                    if (getExpandedPane() != null && getExpandedPane().getContent() instanceof EntryDetailsView) {
                        EntryDetailsView detailsView = (EntryDetailsView) getExpandedPane().getContent();
                        anyTimeFieldFocused = detailsView.getStartTimeField().isFocused() || 
                                              detailsView.getEndTimeField().isFocused();
                    }
                    
                    if (!anyTimeFieldFocused) {
                        popOver.setAutoHide(true);
                    }
                });
            }
        });
    }
}
