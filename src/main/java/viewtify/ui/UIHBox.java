/*
 * Copyright (C) 2021 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify.ui;

import javafx.scene.layout.HBox;
import viewtify.ui.helper.ContextMenuHelper;

public class UIHBox extends UserInterface<UIHBox, HBox> implements ContextMenuHelper<UIHBox> {

    /**
     * @param view
     */
    public UIHBox(View view) {
        super(new HBox(), view);
    }
}