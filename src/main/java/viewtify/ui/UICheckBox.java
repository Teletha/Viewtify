/*
 * Copyright (C) 2018 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify.ui;

import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableBooleanValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.control.CheckBox;

import viewtify.ui.helper.PreferenceHelper;

/**
 * @version 2018/09/09 12:02:22
 */
public class UICheckBox extends UserInterface<UICheckBox, CheckBox> implements PreferenceHelper<UICheckBox, Boolean> {

    /**
     * Enchanced view.
     * 
     * @param view A {@link View} to which the widget belongs.
     */
    private UICheckBox(View view) {
        super(new Internal(), view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property<Boolean> model() {
        return ui.selectedProperty();
    }

    /**
     * Return true when this checkbox is selected.
     * 
     * @return
     */
    public ObservableBooleanValue isSelected() {
        return ui.selectedProperty();
    }

    /**
     * Return true when this checkbox is NOT selected.
     * 
     * @return
     */
    public ObservableBooleanValue isNotSelected() {
        return ui.selectedProperty().not();
    }

    /**
     * @version 2018/09/09 23:26:36
     */
    private static class Internal extends CheckBox {

        /**
         * {@inheritDoc}
         */
        @Override
        public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
            return EnhancedCSSProperty.metadata(super.getControlCssMetaData());
        }
    }
}
