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

import java.awt.Label;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.controlsfx.control.HiddenSidesPane;

import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kiss.I;
import kiss.Signal;
import kiss.Tree;
import kiss.Variable;
import stylist.Style;
import viewtify.Viewtify;
import viewtify.style.FormStyles;
import viewtify.ui.helper.StyleHelper;
import viewtify.util.TextNotation;

/**
 * Domain specific language for user interface declaration.
 */
public class ViewDSL extends Tree<UserInterfaceProvider, ViewDSL.UINode> implements UserInterfaceProvider<Node> {

    /** The horizontal box. */
    protected static final UserInterfaceProvider hbox = new Box(HBox.class);

    /** The vertical box. */
    protected static final UserInterfaceProvider vbox = new Box(VBox.class);

    /** The stack box. */
    protected static final UserInterfaceProvider sbox = new Box(StackPane.class);

    /**
     * 
     */
    protected ViewDSL() {
        super(ViewDSL.UINode::new, null, (follower, current) -> {
            if (follower instanceof Style) {
                StyleHelper.of(current.node).style((Style) follower);
            } else {
                follower.accept(current);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Node ui() {
        return (Node) root.get(0).node;
    }

    /**
     * Convert {@link Node} to {@link UserInterfaceProvider}.
     * 
     * @param node A node to convert.
     * @return
     */
    protected final UserInterfaceProvider ui(Node node) {
        return () -> node;
    }

    /**
     * Declare the dynamic child {@link UserInterfaceProvider}.
     * 
     * @param provider A child UI provider.
     * @param containerAttributes Attributes for container UI.
     */
    protected final void $(Variable<? extends UserInterfaceProvider> provider, Consumer<UINode>... containerAttributes) {
        $(() -> new Pane(), provider.observing().skipNull().map(I::list), containerAttributes);
    }

    /**
     * Declare the dynamic children {@link UserInterfaceProvider}s.
     * 
     * @param container A container UI provider.
     * @param providers A children UI provider.
     * @param containerAttributes Attributes for container UI.
     */
    protected final void $(UserInterfaceProvider<Pane> container, ObservableList<? extends UserInterfaceProvider<? extends Node>> providers, Consumer<UINode>... containerAttributes) {
        $(container, Viewtify.observing(providers), containerAttributes);
    }

    /**
     * Declare the dynamic children {@link UserInterfaceProvider}s.
     * 
     * @param container A container UI provider.
     * @param providers A children UI provider.
     * @param containerAttributes Attributes for container UI.
     */
    private void $(UserInterfaceProvider<Pane> container, Signal<? extends List<? extends UserInterfaceProvider<? extends Node>>> providers, Consumer<UINode>... containerAttributes) {
        Pane pane = container.ui();
        providers.to(list -> {
            pane.getChildren().setAll(I.signal(list).map(UserInterfaceProvider::ui).toList());
        });

        $(() -> pane, containerAttributes);
    }

    /**
     * Declare the {@link Label}.
     * 
     * @param text A label text.
     * @param followers
     */
    protected final void label(Object text, Consumer<UINode>... followers) {
        $(() -> TextNotation.parse(String.valueOf(text)), followers);
    }

    /**
     * Declare the {@link Label}.
     * 
     * @param text A label text.
     * @param followers
     */
    protected final void label(Variable text, Consumer<UINode>... followers) {
        $(() -> TextNotation.parse(text), followers);
    }

    /**
     * Declare Form UI simply.
     * 
     * @param label A form label.
     * @param userInterfaces A list of form controls.
     */
    protected final void form(String label, UserInterface... userInterfaces) {
        form(() -> TextNotation.parse(label), null, userInterfaces);
    }

    /**
     * Declare Form UI simply.
     * 
     * @param label A form label.
     * @param style Additional style for controls.
     * @param userInterfaces A list of form controls.
     */
    protected final void form(String label, Style style, UserInterface... userInterfaces) {
        form(() -> TextNotation.parse(label), style, userInterfaces);
    }

    /**
     * Declare Form UI simply.
     * 
     * @param label A form label.
     * @param userInterfaces A list of form controls.
     */
    protected final void form(Variable label, UserInterface... userInterfaces) {
        form(label, null, userInterfaces);
    }

    /**
     * Declare Form UI simply.
     * 
     * @param label A form label.
     * @param style Additional style for controls.
     * @param userInterfaces A list of form controls.
     */
    protected final void form(Variable label, Style style, UserInterface... userInterfaces) {
        form(() -> TextNotation.parse(label), style, userInterfaces);
    }

    /**
     * Declare Form UI simply.
     * 
     * @param style Additional style for controls.
     * @param userInterfaces A list of form controls.
     */
    protected final void form(Style style, UserInterface... userInterfaces) {
        form((UserInterfaceProvider) null, style, userInterfaces);
    }

    /**
     * Declare Form UI simply.
     * 
     * @param label A form label.
     * @param style Additional style for controls.
     * @param userInterfaces A list of form controls.
     */
    private void form(UserInterfaceProvider label, Style style, UserInterface... userInterfaces) {
        $(hbox, FormStyles.FormRow, () -> {
            if (label != null) $(label, FormStyles.FormLabel);
            for (UserInterface userInterface : userInterfaces) {
                $(userInterface, style == null ? new Style[] {FormStyles.FormInput} : new Style[] {FormStyles.FormInput, style});
            }
        });
    }

    /**
     * UI tree structure node for javafx's {@link Node}.
     */
    static class UINode implements Consumer<UINode> {

        /**
         * The actual javafx {@link Node} like object (using {@link Styleable} because
         * {@link TableColumn} and {@link MenuItem} are not {@link Node}).
         */
        private Styleable node;

        /**
         * Internal tree node.
         * 
         * @param provider Actual UI provider.
         * @param id Unused.
         * @param context Unused.
         */
        private UINode(UserInterfaceProvider provider, int id, Object context) {
            this.node = provider.ui();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(UINode context) {
            if (context.node instanceof TableView) {
                ((TableView) context.node).getColumns().add(node);
            } else if (context.node instanceof TreeTableView) {
                ((TreeTableView) context.node).getColumns().add(node);
            } else if (context.node instanceof ScrollPane) {
                ((ScrollPane) context.node).setContent((Node) node);
            } else if (context.node instanceof SplitPane) {
                ((SplitPane) context.node).getItems().add((Node) node);
            } else if (context.node instanceof HiddenSidesPane) {
                ((HiddenSidesPane) context.node).setContent((Node) node);
            } else if (context.node instanceof ToolBar) {
                ((ToolBar) context.node).getItems().add((Node) node);
            } else if (context.node instanceof Group) {
                ((Group) context.node).getChildren().add((Node) node);
            } else if (context.node instanceof Pane) {
                ((Pane) context.node).getChildren().add((Node) node);
            }
        }
    }

    /**
     * General box container.
     */
    private static final class Box<P extends Pane> implements UserInterfaceProvider<P> {

        /** The box type. */
        private final Class<P> type;

        /**
         * The typed box.
         * 
         * @param type A box type.
         */
        private Box(Class<P> type) {
            this.type = Objects.requireNonNull(type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public P ui() {
            return I.make(type);
        }
    }
}