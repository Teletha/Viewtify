/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui;

import java.util.function.Function;

import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.text.Text;

import viewtify.View;
import viewtify.Viewtify;

/**
 * @version 2017/11/15 9:54:15
 */
public class UITreeTableView<T> extends AbstractTableView<UITreeTableView, TreeTableView<T>, T> {

    /** The root item. */
    public final UITreeItem<T> root;

    /**
     * Enchanced view.
     * 
     * @param ui
     */
    private UITreeTableView(TreeTableView<T> ui, View view) {
        super(ui, view, Viewtify.calculate(ui.getSelectionModel().getSelectedItems()).map(TreeItem<T>::getValue));

        TreeItem item = new TreeItem();
        ui.setRoot(item);
        ui.setShowRoot(false);

        root = new UITreeItem(ui, item);
    }

    public UITreeTableView<T> placeholder(String text) {
        return placeholder(new Text(text));
    }

    public UITreeTableView<T> placeholder(Node node) {
        ui.setPlaceholder(node);
        return this;
    }

    /**
     * <p>
     * Specifies the selection mode to use in this selection model. The selection mode specifies how
     * many items in the underlying data model can be selected at any one time.
     * <p>
     */
    public UITreeTableView<T> selectMultipleRows() {
        ui.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return this;
    }

    /**
     * <p>
     * Specifies the selection mode to use in this selection model. The selection mode specifies how
     * many items in the underlying data model can be selected at any one time.
     * <p>
     */
    public UITreeTableView<T> selectSingleRow() {
        ui.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        return this;
    }

    /**
     * Specifies ROW renderer.
     */
    public UITreeTableView<T> render(Function<UITreeTableView<T>, TreeTableRow<T>> renderer) {
        ui.setRowFactory(table -> renderer.apply(this));

        return this;
    }
}
