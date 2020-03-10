/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;

public class UIScrollPane extends UserInterface<UIScrollPane, ScrollPane> {

    /**
     * @param ui
     * @param view
     */
    public UIScrollPane(View view) {
        super(new ScrollPane(), view);
    }

    /**
     * Set scroll bar policy.
     * 
     * @param horizontal
     * @param vertical
     * @return
     */
    public UIScrollPane policy(ScrollBarPolicy horizontal, ScrollBarPolicy vertical) {
        if (horizontal != null) {
            ui.setHbarPolicy(horizontal);
        }
        if (vertical != null) {
            ui.setVbarPolicy(vertical);
        }
        return this;
    }
}