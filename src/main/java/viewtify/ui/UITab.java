/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javafx.scene.Node;
import javafx.scene.control.Tab;

import kiss.I;
import kiss.WiseFunction;
import viewtify.ui.helper.ContextMenuHelper;
import viewtify.ui.helper.LabelHelper;
import viewtify.ui.helper.StyleHelper;

public class UITab extends Tab implements StyleHelper<UITab, Tab>, LabelHelper<UITab>, ContextMenuHelper<UITab> {

    /** Cache to find tab node. */
    private static final WiseFunction<Node, Object> findTab;

    static {
        try {
            Method m = Class.forName("javafx.scene.control.skin.TabPaneSkin$TabHeaderSkin").getMethod("getTab");
            m.setAccessible(true);

            findTab = m::invoke;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The parent view. */
    private final View parent;

    /** The actual contents builder. */
    private Function<UITab, View> viewBuilder;

    /** Tab state. */
    private final AtomicBoolean loaded = new AtomicBoolean();

    /** The cached reference for the styleable node . */
    private WeakReference<Node> styleable;

    public UITab() {
        this(null);
    }

    /**
     * 
     */
    protected UITab(View parent) {
        this.parent = parent;

        selectedProperty().addListener(change -> load());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tab ui() {
        return this;
    }

    /**
     * Set contents.
     * 
     * @param contents
     * @return
     */
    public final UITab contents(Class<? extends View> contents) {
        return contents(I.make(contents));
    }

    /**
     * Set contents.
     * 
     * @param contents
     * @return
     */
    public final UITab contents(View contents) {
        return contents(tab -> contents);
    }

    /**
     * Set contents.
     * 
     * @param contents
     * @return
     */
    public final UITab contents(Function<UITab, View> contents) {
        if (isLoaded()) {
            setContent(contents.apply(this).ui());
        } else {
            viewBuilder = contents;
        }
        return this;
    }

    /**
     * Test if this tab has been already loaded.
     * 
     * @return
     */
    public final boolean isLoaded() {
        return loaded.get();
    }

    /**
     * Load tab contents explicitly.
     */
    public final void load() {
        if (viewBuilder != null && loaded.getAndSet(true) == false) {
            View view = viewBuilder.apply(this);
            view.initializeLazy(parent);
            setContent(view.ui());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Node getStyleableNode() {
        if (styleable == null) {
            for (Node node : getTabPane().lookupAll(".tab")) {
                if (findTab.apply(node) == this) {
                    styleable = new WeakReference(node);
                    break;
                }
            }

            if (styleable == null) {
                return null;
            }
        }
        return styleable.get();
    }
}
