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

/**
 * @version 2018/08/29 12:11:02
 */
public interface UserInterfaceProvider<N> {

    /**
     * Provide the actual javafx UI.
     * 
     * @return
     */
    N ui();
}