/*
 * Copyright (C) 2019 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify.ui;

import javafx.css.Styleable;

/**
 * @version 2018/09/09 18:10:32
 */
public interface UserInterfaceProvider<UI extends Styleable> {

    /**
     * Provide the actual user interface.
     * 
     * @return A user interface.
     */
    UI ui();
}
