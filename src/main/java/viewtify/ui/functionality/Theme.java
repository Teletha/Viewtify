/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui.functionality;

import javafx.collections.ObservableList;
import javafx.css.Styleable;

import kiss.Variable;

/**
 * @version 2017/12/02 18:19:15
 */
public interface Theme<Self extends Theme, S extends Styleable> extends UserInterface<S> {

    /**
     * Apply single state class by the specified enum.
     * 
     * @param node
     * @param state
     */
    default <E extends Enum<E>> Self style(E state) {
        if (state != null) {
            ObservableList<String> classes = ui().getStyleClass();

            for (Enum value : state.getClass().getEnumConstants()) {
                String name = value.name();

                if (state == value) {
                    if (!classes.contains(name)) {
                        classes.add(name);
                    }
                } else {
                    classes.remove(name);
                }
            }
        }
        return (Self) this;
    }

    /**
     * Apply single state class by the specified enum.
     * 
     * @param node
     * @param state
     */
    default <E extends Enum<E>> Self style(Variable<E> state) {
        return style(state.get());
    }

    /**
     * Clear all style for the specified enum type.
     * 
     * @param class1
     */
    default <E extends Enum<E>> Self unstyle(Class<E> style) {
        if (style != null) {
            ObservableList<String> classes = ui().getStyleClass();

            for (Enum value : style.getEnumConstants()) {
                classes.remove(value.name());
            }
        }
        return (Self) this;
    }
}