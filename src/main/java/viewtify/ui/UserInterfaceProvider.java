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

import java.time.LocalDate;
import java.util.Objects;

import javafx.css.Styleable;
import javafx.scene.Node;

import kiss.Variable;
import viewtify.ui.helper.ValueHelper;

public interface UserInterfaceProvider<UI extends Styleable> {

    /**
     * Provide the actual user interface.
     * 
     * @return A user interface.
     */
    UI ui();

    /**
     * Create the new user input UI for the specified type.
     * 
     * @param <V> Value type
     * @param <UI> UI type
     * @param type A value type.
     * @param property A value model.
     * @return A created UI.
     */
    static <V, UI extends UserInterface<UI, ? extends Node> & ValueHelper<UI, V>> UI inputFor(Class<V> type, Variable<V> property) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(property);

        if (type == String.class) {
            return (UI) new UIText<V>(null, String.class).clearable().sync(property);
        } else if (Enum.class.isAssignableFrom(type)) {
            return (UI) new UIComboBox<V>(null).items(type.getEnumConstants()).nullable().sync(property);
        } else if (Number.class.isAssignableFrom(type)) {
            return (UI) new UIText<V>(null, type).clearable().acceptDecimalInput().sync(property);
        } else if (type == LocalDate.class) {
            return (UI) new UIDatePicker(null).sync((Variable<LocalDate>) property);
        } else if (Comparable.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Unsupported type [" + type + "]");
        } else {
            throw new IllegalArgumentException("Unsupported type [" + type + "]");
        }
    }
}