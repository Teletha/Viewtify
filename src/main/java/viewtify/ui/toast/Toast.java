/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui.toast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.WritableDoubleValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

import kiss.Disposable;
import kiss.I;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.model.Model;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;
import viewtify.util.Corner;
import viewtify.util.FXUtils;

public class Toast {

    /** The margin for each notifications. */
    private static final int MARGIN = 12;

    /** The base transparent window. */
    private static final LinkedList<Notification> notifications = new LinkedList();

    /** The setting. */
    public static final Setting setting = I.make(Setting.class);

    /**
     * Show the specified node.
     * 
     * @param node
     */
    public static void show(String message) {
        Label label = new Label(message);
        label.setWrapText(true);

        show(label);
    }

    /**
     * Show the specified node.
     * 
     * @param node
     */
    public static void show(Node node) {
        add(new Notification(node));
    }

    /**
     * Add notification.
     * 
     * @param notification
     */
    private static void add(Notification notification) {
        notifications.add(notification);
        if (setting.max.v < notifications.size()) {
            while (setting.max.v < notifications.size()) {
                remove(notifications.peekFirst());
            }
        } else {
            layoutNotifications();
        }
    }

    /**
     * Remove notification.
     * 
     * @param notification
     */
    private static void remove(Notification notification) {
        // model management
        if (notifications.remove(notification)) {
            if (notification.disposer != null) {
                notification.disposer.dispose();
            }

            // UI effect
            FXUtils.animate(setting.animation.v, notification.popup.opacityProperty(), 0, () -> {
                notification.popup.hide();
                notification.popup.getContent().clear();
            });

            // draw UI
            layoutNotifications();
        }
    }

    /**
     * Layout all notifications.
     */
    private static void layoutNotifications() {
        Window w = Window.getWindows().get(0);
        // correct 10 pixel for maximized window
        Screen screen = Screen.getScreensForRectangle(w.getX() + 10, w.getY(), w.getWidth(), w.getHeight()).get(0);
        Rectangle2D rect = screen.getBounds();
        boolean isTopSide = setting.area.v.isTopSide();
        double x = setting.area.v.isLeftSide() ? rect.getMinX() + MARGIN : rect.getMaxX() - setting.width.v - MARGIN;
        double y = isTopSide ? 30 : rect.getMaxY() - 30;

        Iterator<Notification> iterator = isTopSide ? notifications.descendingIterator() : notifications.iterator();
        while (iterator.hasNext()) {
            Toast.Notification notify = iterator.next();

            if (notify.popup.isShowing()) {
                if (!isTopSide) y -= notify.popup.getHeight() + MARGIN;
                notify.popup.setX(x);
                FXUtils.animate(setting.animation.v, notify.y, y);
            } else {
                notify.popup.setOpacity(0);
                notify.popup.show(w);
                if (!isTopSide) y -= notify.popup.getHeight() + MARGIN;
                notify.popup.setX(x);
                notify.popup.setY(y);

                FXUtils.animate(setting.animation.v, notify.popup.opacityProperty(), 1);
            }

            if (isTopSide) y += notify.popup.getHeight() + MARGIN;
        }
    }

    /**
     * 
     */
    public static class Setting extends Model<Setting> {

        /** The maximum size of notifications. */
        public final Preference<Integer> max = initialize(7).require(n -> Math.max(1, n));

        /** The animation time. */
        public final Preference<Duration> animation = initialize(Duration.millis(333))
                .require(n -> Duration.ONE.lessThanOrEqualTo(n) ? n : Duration.ONE);

        /** The automatic hiding time. */
        public final Preference<Duration> autoHide = initialize(Duration.seconds(60))
                .require(n -> Duration.ONE.lessThanOrEqualTo(n) ? n : Duration.ZERO);

        /** The notification area. */
        public final Preference<Corner> area = initialize(Corner.TopRight);

        /** The opacity of notification area. */
        public final Preference<Double> opacity = initialize(0.85).require(n -> Math.max(0, Math.min(1, n)));

        /** The width of notification area. */
        public final Preference<Integer> width = initialize(250).require(n -> Math.max(10, n));

        /**
         * Configure the animation time. (default : 333)
         * 
         * @param duration A positive number (mills).
         * @return Chainable API.
         */
        public Setting animation(int duration) {
            return animation(Duration.millis(duration));
        }

        /**
         * Configure the animation time. (default : 333)
         * 
         * @param duration A positive number.
         * @return Chainable API.
         */
        public Setting animation(Duration duration) {
            animation.set(duration);
            return this;
        }

        /**
         * Configure the notification area. (default : TopRight)
         * 
         * @param corner A notification area.
         * @return Chainable API.
         */
        public Setting area(Corner corner) {
            area.set(corner);
            return this;
        }

        /**
         * Configure the auto hide time. (default : 60)
         * 
         * @param duration A positive number (sec), 0 or negative number disable auto-hiding.
         * @return Chainable API.
         */
        public Setting autoHide(int duration) {
            return autoHide(Duration.seconds(duration));
        }

        /**
         * Configure the auto hide time. (default : 60)
         * 
         * @param duration A positive number (sec), 0 or negative number disable auto-hiding.
         * @return Chainable API.
         */
        public Setting autoHide(Duration duration) {
            autoHide.set(duration);
            return this;
        }

        /**
         * Configure the maximum viewable size of notifications. (default : 7)
         * 
         * @param size A positive number.
         * @return Chainable API.
         */
        public Setting max(int size) {
            max.set(size);
            return this;
        }

        /**
         * Configure the opacity of notification area. (default : 0.85)
         * 
         * @param value A positive number.
         * @return Chainable API.
         */
        public Setting opacity(double value) {
            opacity.set(value);
            return this;
        }

        /**
         * Configure the width of notification area. (default : 250)
         * 
         * @param value A positive number.
         * @return Chainable API.
         */
        public Setting width(int value) {
            width.set(value);
            return this;
        }
    }

    /**
     * 
     */
    private static class Notification {

        /** The base transparent window. */
        private final Popup popup = new Popup();

        /** Expose location y property. */
        private final WritableDoubleValue y = new WritableDoubleValue() {

            @Override
            public Number getValue() {
                return get();
            }

            @Override
            public double get() {
                return popup.getY();
            }

            @Override
            public void setValue(Number value) {
                set(value.doubleValue());
            }

            @Override
            public void set(double value) {
                popup.setY(value);
            }
        };

        private Disposable disposer;

        /**
         * @param node
         */
        private Notification(Node node) {
            VBox box = new VBox(node);
            StyleHelper.of(box).style(Styles.popup);
            box.setMaxWidth(setting.width.v);
            // box.setMinWidth(setting.width.v);
            box.setOpacity(setting.opacity.v);

            popup.setX(0);
            popup.getContent().add(box);
            UserActionHelper.of(popup).when(User.MouseClick).to(() -> remove(this));
            if (0 < setting.autoHide.v.toMillis()) {
                disposer = I.schedule((long) setting.autoHide.v.toMillis(), TimeUnit.MILLISECONDS).first().to(() -> remove(this));
            }
        }

    }

    /**
     * Notification style.
     */
    private static interface Styles extends StyleDSL {

        Style popup = () -> {
            padding.vertical(MARGIN, px).horizontal(MARGIN, px);
            background.color("-fx-background");
            border.radius(7, px).color("-fx-mid-text-color");
        };
    }
}
