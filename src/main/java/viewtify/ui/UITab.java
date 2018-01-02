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

import java.util.Objects;

import javafx.scene.control.Tab;

import viewtify.ui.helper.StyleHelper;

/**
 * @version 2017/12/27 15:55:39
 */
public class UITab implements StyleHelper<UITab, Tab> {

    /** The actual ui. */
    public final Tab ui;

    /**
     * @param tab
     */
    public UITab(Tab tab) {
        this.ui = tab;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tab ui() {
        return ui;
    }

    /**
     * Get text.
     * 
     * @param text
     */
    public String text() {
        return ui.getText();
    }

    /**
     * Set text.
     * 
     * @param text
     */
    public UITab text(Object text) {
        ui.setText(Objects.toString(text));
        return this;
    }
}