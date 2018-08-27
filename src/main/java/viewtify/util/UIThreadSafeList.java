/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.util;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import viewtify.Viewtify;

/**
 * @version 2018/08/23 20:15:33
 */
public class UIThreadSafeList<E> extends DelegatingObservableList<E> {

    /**
     * Create UI thread-safe {@link ObservableList}.
     * 
     * @param delegate
     */
    public UIThreadSafeList(ObservableList<E> delegate) {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends E> change) {
        Viewtify.inUI(() -> fireChange(change));
    }
}