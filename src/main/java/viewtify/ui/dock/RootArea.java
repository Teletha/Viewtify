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

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * A RootArea is a special {@link ViewArea} which has no parent and is directly used as root.
 */
final class RootArea extends ViewArea {

    /** The actual root pane. */
    private final Pane box;

    /** Close the stage containing this area when removing the child. */
    final boolean closeStage;

    /**
     * Create a new root area.
     *
     * @param dndManager The drag&drop manager
     * @param closeStage Close the stage containing this area when the last view was removed?
     */
    RootArea(DNDManager dndManager, boolean closeStage) {
        this(new VBox(), dndManager, closeStage);
    }

    /**
     * Create a new root area.
     *
     * @param box Use this pane to draw all the content.
     * @param dndManager The drag&drop manager
     * @param closeStage Close the stage containing this area when the last view was removed?
     */
    RootArea(Pane box, DNDManager dndManager, boolean closeStage) {
        super(dndManager);

        this.closeStage = closeStage;
        this.box = box;
        ViewArea editorArea = new TabArea(this, dndManager);
        editorArea.setEditor(true);
        this.box.getChildren().add(editorArea.getNode());
        setFirstChild(editorArea);
        HBox.setHgrow(box, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected Parent getNode() {
        return box;
    }

    /**
     * Set {@param child} as first child of this view area.
     * <p/>
     * It will also update the javafx scene graph and the childs parent value.
     *
     * @param child The new child.
     */
    @Override
    protected void setFirstChild(ViewArea child) {
        super.setFirstChild(child);
        box.getChildren().set(0, child.getNode());
        HBox.setHgrow(child.getNode(), Priority.ALWAYS);
        VBox.setVgrow(child.getNode(), Priority.ALWAYS);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void setSecondChild(ViewArea child) {
        throw new UnsupportedOperationException("Root Areas can not contain more than one ");
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void setParent(ViewArea parent) {
        throw new UnsupportedOperationException("Root Areas can not have any parent area");
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void split(ViewArea first, ViewArea second, Orientation orientation) {
        throw new UnsupportedOperationException("Root Areas can not be split");
    }

    /**
     * Add the view to this area at position.
     * <p/>
     * If position is {@link Position#CENTER} it will be added to that child that is defined as
     * editor area. Otherwise this area is split and the view will be positioned according the
     * position parameter.
     *
     * @param view The view to add.
     * @param position Add the view at this position.
     */
    @Override
    protected void add(ViewStatus view, Position position) {
        getFirstChild().add(view, position);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void remove(ViewArea area) {
        if (!closeStage) {
            throw new UnsupportedOperationException("Root Areas must have exactly one child");
        }
        ((Stage) box.getScene().getWindow()).close();
    }
}