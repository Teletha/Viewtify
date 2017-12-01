/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui;

import java.util.function.Consumer;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

/**
 * @version 2017/11/15 9:54:15
 */
public class UIMenuItem {

    /** The actual ui. */
    public final MenuItem ui;

    /**
     * Enchanced view.
     * 
     * @param ui
     */
    UIMenuItem(MenuItem ui) {
        this.ui = ui;
    }

    /**
     * Set label text.
     * 
     * @param label
     * @return
     */
    public UIMenuItem label(String label) {
        ui.setText(label);
        return this;
    }

    /**
     * Helper to listen user action event.
     * 
     * @param actionType
     * @param listener
     * @return
     */
    public <T extends Event> UIMenuItem whenUserClick(Runnable listener) {
        return whenUserClick(e -> listener.run());
    }

    /**
     * Helper to listen user action event.
     * 
     * @param actionType
     * @param listener
     * @return
     */
    public <T extends Event> UIMenuItem whenUserClick(EventHandler<ActionEvent> listener) {
        ui.addEventHandler(ActionEvent.ACTION, listener);
        return this;
    }

    /**
     * Helper to listen user action event.
     * 
     * @param actionType
     * @param listener
     * @return
     */
    public <T extends Event, A> UIMenuItem whenUserClick(Consumer<A> listener, A context) {
        return whenUserClick(e -> listener.accept(context));
    }

    /**
     * Validation helper.
     */
    public UIMenuItem disableWhen(ObservableValue<? extends Boolean> condition) {
        if (condition != null) {
            ui.disableProperty().bind(condition);
        }
        return this;
    }
}