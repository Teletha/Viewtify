/*
 * Copyright (C) 2018 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify;

import java.util.Optional;
import java.util.function.Consumer;

import javafx.css.Styleable;
import javafx.scene.paint.Color;

import stylist.CSSValue;
import stylist.StyleRule;
import viewtify.UI.UINode;
import viewtify.ui.helper.StyleHelper;

/**
 * Declared interface style.
 * 
 * @version 2018/09/05 14:03:55
 */
public interface Style extends stylist.Style, Consumer<UINode> {

    /**
     * {@inheritDoc}
     */
    @Override
    default void accept(UINode parent) {
        if (parent.node instanceof Styleable) {
            StyleHelper.of((Styleable) parent.node).style(this);
        }
    }

    /**
     * Compute stroke color as JavaFX {@link Color}.
     * 
     * @return
     */
    default Color stroke() {
        StyleRule rule = StyleRule.create(this);
        Optional<CSSValue> color = rule.properties.get("stroke");

        if (color.isPresent()) {
            CSSValue value = color.get();

            if (value instanceof stylist.value.Color) {
                return Color.web(((stylist.value.Color) value).toRGB());
            } else {
                return Color.web(color.toString());
            }
        } else {
            return Color.TRANSPARENT;
        }
    }
}
