/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui.helper;

import java.util.Objects;

import javafx.scene.control.Labeled;

/**
 * @version 2018/01/23 14:00:24
 */
public interface LabelHelper<Self extends LabelHelper, W extends Labeled> extends StyleHelper<Self, W> {

    /**
     * Get text.
     * 
     * @param text
     */
    default String text() {
        return ui().getText();
    }

    /**
     * Set text.
     * 
     * @param text
     */
    default Self text(Object text) {
        ui().setText(Objects.toString(text));
        return (Self) this;
    }
}