/*
 * Copyright (C) 2020 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify.ui;

import static java.util.concurrent.TimeUnit.*;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.GraphicDecoration;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.ui.helper.DisableHelper;
import viewtify.ui.helper.PropertyAccessHelper;
import viewtify.ui.helper.ReferenceHolder;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.TooltipHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;
import viewtify.ui.helper.ValueHelper;
import viewtify.ui.helper.Verifier;
import viewtify.ui.helper.VerifyHelper;
import viewtify.util.Icon;

public class UserInterface<Self extends UserInterface<Self, W>, W extends Node> extends ReferenceHolder
        implements UserActionHelper<Self>, StyleHelper<Self, W>, DisableHelper<Self>, TooltipHelper<Self, W>, UserInterfaceProvider<W>,
        PropertyAccessHelper, VerifyHelper<Self> {

    /** User configuration for UI. */
    private static final Preference preference = I.make(Preference.class).restore();

    /** The actual view. */
    public final W ui;

    /** The associated view. */
    protected final View view;

    /** The validation system. */
    private Verifier verifier;

    /**
     * @param ui
     */
    public UserInterface(W ui, View view) {
        this.ui = ui;
        this.view = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public W ui() {
        return ui;
    }

    /**
     * Gets the A to which this UI currently belongs.
     * 
     * @return
     */
    public final Optional<Stage> stage() {
        return Optional.ofNullable((Stage) ui.getScene().getWindow());
    }

    /**
     * Register keyborad shortcut.
     * 
     * @param key
     * @param action
     * @return
     */
    public Self keybind(String key, Runnable action) {
        KeyCombination stroke = KeyCodeCombination.keyCombination(key);
        when(User.KeyPress).take(stroke::match).to(action::run);
        return (Self) this;
    }

    /**
     * Select parent {@link Node}.
     * 
     * @return
     */
    public UserInterface parent() {
        return new UserInterface(ui.getParent(), view);
    }

    /**
     * Specifies whether this {@code Node} and any subnodes should be rendered as part of the scene
     * graph. A node may be visible and yet not be shown in the rendered scene if, for instance, it
     * is off the screen or obscured by another Node. Invisible nodes never receive mouse events or
     * keyboard focus and never maintain keyboard focus when they become invisible.
     *
     * @defaultValue true
     */
    public Self visible(boolean visible) {
        ui.setVisible(visible);
        return (Self) this;
    }

    /**
     * Mark as valid interface.
     * 
     * @return
     */
    public final Self decorateBy(Icon icon) {
        decorateBy(icon, null);

        return (Self) this;
    }

    /**
     * Decorate this ui by the specified icon.
     * 
     * @param icon
     * @param message
     */
    private final void decorateBy(Icon icon, String message) {
        I.signal(Decorator.getDecorations(ui))
                .take(GraphicDecoration.class::isInstance)
                .take(1)
                .on(Viewtify.UIThread)
                .to(deco -> Decorator.removeDecoration(ui, deco), e -> {

                }, () -> {
                    Label label = new Label();
                    label.setGraphic(icon.image());
                    label.setAlignment(Pos.CENTER);

                    if (message != null && !message.isEmpty()) {
                        Tooltip tooltip = new Tooltip(message);
                        tooltip.setAutoFix(true);
                        tooltip.setShowDelay(Duration.ZERO);
                        tooltip.setShowDuration(Duration.INDEFINITE);
                        StyleHelper.of(tooltip).style(Styles.ValidationToolTip);
                        label.setTooltip(tooltip);
                    }

                    Decorator.addDecoration(ui, new GraphicDecoration(label, Pos.CENTER_LEFT));
                });
    }

    /**
     * Undecorate all icons.
     */
    private final void undecorate() {
        I.signal(Decorator.getDecorations(ui))
                .take(GraphicDecoration.class::isInstance)
                .take(1)
                .on(Viewtify.UIThread)
                .to(deco -> Decorator.removeDecoration(ui, deco));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized Verifier verifier() {
        if (verifier == null) {
            verifier = new Verifier();
            if (this instanceof ValueHelper) verifier.verifyWhen(((ValueHelper) this).isChanged());
            verifier.message.observe().to(message -> {
                if (message == null) {
                    undecorate();
                } else {
                    decorateBy(Icon.Error, message);
                }
            });
        }
        return verifier;
    }

    /**
     * Restore UI related settings.
     * 
     * @param property
     * @param defaultValue The default property value.
     */
    protected final <T> T restore(Property<T> property, T defaultValue) {
        return restore(property, property::setValue, defaultValue);
    }

    /**
     * Restore UI related settings.
     * 
     * @param getterAndObserver A property value getter and observer.
     * @param setter A property value setter.
     * @param defaultValue The default property value.
     */
    protected final <T> T restore(ReadOnlyProperty<T> getterAndObserver, Consumer<T> setter, T defaultValue) {
        T value = Objects.requireNonNull(defaultValue);
        String id = view.id() + " 🔄 " + ui.getId() + " 🔄 " + getterAndObserver.getName();

        // restore
        if (ui.getId() != null) {
            String stored = preference.get(id);

            if (stored != null) {
                try {
                    value = (T) I.transform(stored, defaultValue.getClass());
                } catch (Throwable e) {
                    // ignore
                    e.printStackTrace();
                }
            }
        }
        setter.accept(value);

        // prepare for storing
        Viewtify.observe(getterAndObserver).debounce(1000, MILLISECONDS).to(change -> {
            preference.put(id, I.transform(change, String.class));
            preference.store();
        });
        return value;
    }

    /**
     * 
     */
    @SuppressWarnings("serial")
    @Managed(value = Singleton.class)
    private static class Preference extends ConcurrentSkipListMap<String, String> implements Storable<Preference> {
    }

    /**
     * 
     */
    private static class Styles implements StyleDSL {

        static Style ValidationToolTip = () -> {
            font.size(12, px).color("-fx-light-text-color");
            background.color($.rgba(60, 60, 60, 0.8));
            padding.vertical(8, px).horizontal(12, px);
        };
    }
}