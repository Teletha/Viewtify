/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui.dock;

import java.util.LinkedHashSet;
import java.util.Set;

import javafx.scene.Parent;
import javafx.scene.control.TabPane;

/**
 * Describes a logical view area which displays the views within a tab pane.
 */
class TabArea extends ViewArea {

    /** The actual root pane. */
    private final TabPane tabPane = new TabPane();

    /** A list with all contained views. */
    private final Set<ViewStatus> views = new LinkedHashSet<>();

    /**
     * Create a new tab area.
     */
    TabArea() {
        registerDragEvents();
    }

    /**
     * Register the event handler for drag&drop of views.
     */
    private void registerDragEvents() {
        tabPane.setOnDragDetected(event -> {
            DNDManager.onDragDetected(event);
        });
        tabPane.setOnDragDone(event -> {
            DNDManager.onDragDone(event);
        });
        super.registerDragEvents(tabPane);
    }

    /**
     * Remove a view from this area. If this area is empty it will also be removed.
     *
     * @param view The view to remove
     */
    void remove(ViewStatus view) {
        remove(view, true);
    }

    /**
     * Remove a view from this area. If checkEmpty is true it checks if this area is empty and
     * remove this area.
     *
     * @param view The view to remove.
     * @param checkEmpty Should this area be removed if it is empty?
     */
    void remove(ViewStatus view, boolean checkEmpty) {
        if (!views.contains(view)) {
            return;
        }
        views.remove(view);
        view.setArea(null);
        tabPane.getTabs().remove(view.tab);
        if (checkEmpty) {
            handleEmpty();
        }
    }

    /**
     * Check if this area is empty, so remove it.
     */
    void handleEmpty() {
        if (views.isEmpty()) {
            getParent().remove(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Parent getNode() {
        return tabPane;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void add(ViewStatus view, ViewPosition position) {
        if (position != ViewPosition.CENTER) {
            super.add(view, position);
            return;
        }
        views.add(view);
        view.setArea(this);
        tabPane.getTabs().add(view.tab);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canDropToCenter() {
        return true;
    }
}