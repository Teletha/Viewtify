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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import kiss.I;
import kiss.Variable;
import kiss.WiseFunction;
import viewtify.Viewtify;
import viewtify.ui.helper.LabelHelper;

/**
 * @version 2018/09/09 18:04:36
 */
public class UITableColumn<RowValue, ColumnValue>
        extends UITableColumnBase<TableColumn<RowValue, ColumnValue>, UITableColumn<RowValue, ColumnValue>, RowValue, ColumnValue> {

    /** The value provider utility. */
    private TypeMappingProvider mappingProvider;

    /**
     * Enchanced view.
     * 
     * @param view A {@link View} to which the widget belongs.
     */
    private UITableColumn(View view) {
        super(new Internal());
    }

    /**
     * Add value provider.
     * 
     * @param provider
     * @return
     */
    public <P extends Function<RowValue, ObservableValue<ColumnValue>>> UITableColumn<RowValue, ColumnValue> model(Class<P> provider) {
        return modelByProperty(I.make(provider));
    }

    /**
     * Add value provider.
     * 
     * @param provider
     * @return
     */
    public <Type extends RowValue> UITableColumn<RowValue, ColumnValue> model(Class<Type> type, WiseFunction<Type, ColumnValue> provider) {
        return modelByProperty(type, row -> new SimpleObjectProperty(provider.apply(row)));
    }

    /**
     * Add value provider.
     * 
     * @param provider
     * @return
     */
    public UITableColumn<RowValue, ColumnValue> model(WiseFunction<RowValue, ColumnValue> provider) {
        return modelByProperty(row -> new SimpleObjectProperty(provider.apply(row)));
    }

    /**
     * Add value provider.
     * 
     * @param provider
     * @return
     */
    public UITableColumn<RowValue, ColumnValue> modelByProperty(Function<RowValue, ObservableValue<ColumnValue>> provider) {
        ui.setCellValueFactory(data -> provider.apply(data.getValue()));
        return this;
    }

    /**
     * Add value provider.
     * 
     * @param provider
     * @return
     */
    public <Type extends RowValue> UITableColumn<RowValue, ColumnValue> modelByProperty(Class<Type> type, Function<Type, ObservableValue<ColumnValue>> provider) {
        if (mappingProvider == null) {
            modelByProperty(mappingProvider = new TypeMappingProvider());
        }
        mappingProvider.mapper.put(type, provider);

        return this;
    }

    /**
     * Add value provider.
     * 
     * @param provider
     * @return
     */
    public UITableColumn<RowValue, ColumnValue> modelByVar(WiseFunction<RowValue, Variable<ColumnValue>> provider) {
        return modelByProperty(row -> Viewtify.calculate(provider.apply(row)));
    }

    /**
     * Add value provider.
     * 
     * @param provider
     * @return
     */
    public <Type extends RowValue> UITableColumn<RowValue, ColumnValue> modelByVar(Class<Type> type, WiseFunction<Type, Variable<ColumnValue>> provider) {
        return modelByProperty(type, row -> Viewtify.calculate(provider.apply(row)));
    }

    /**
     * @version 2017/12/02 16:23:03
     */
    private class TypeMappingProvider<T> implements Function<T, ObservableValue<ColumnValue>> {

        private final Map<Class<T>, Function<T, ObservableValue<ColumnValue>>> mapper = new HashMap();

        /**
         * {@inheritDoc}
         */
        @Override
        public ObservableValue<ColumnValue> apply(T value) {
            return mapper.get(value.getClass()).apply(value);
        }
    }

    /**
     * Set cell renderer.
     * 
     * @param renderer
     * @return
     */
    public UITableColumn<RowValue, ColumnValue> render(Function<TableColumn<RowValue, ColumnValue>, TableCell<RowValue, ColumnValue>> renderer) {
        ui.setCellFactory(table -> renderer.apply(table));
        return this;
    }

    /**
     * Set cell renderer.
     * 
     * @param renderer
     * @return
     */
    public UITableColumn<RowValue, ColumnValue> render(BiConsumer<UITableCell, ColumnValue> renderer) {
        ui.setCellFactory(table -> new UITableCell(renderer).ui);
        return this;
    }

    /**
     * @version 2017/12/02 18:11:59
     */
    public class UITableCell implements LabelHelper<UITableCell, TableCell<RowValue, ColumnValue>> {

        /** The user renderer. */
        private final BiConsumer<UITableCell, ColumnValue> renderer;

        /** The actual widget. */
        private final TableCell<RowValue, ColumnValue> ui = new TableCell<RowValue, ColumnValue>() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void updateItem(ColumnValue item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    renderer.accept(UITableCell.this, item);
                }
            }
        };

        /**
         * @param renderer
         */
        private UITableCell(BiConsumer<UITableCell, ColumnValue> renderer) {
            this.renderer = renderer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TableCell ui() {
            return ui;
        }
    }

    /**
     * @version 2018/09/09 23:26:36
     */
    private static class Internal<S, T> extends TableColumn<S, T> {

        /**
         * {@inheritDoc}
         */
        @Override
        public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
            return EnhancedCSSProperty.metadata(super.getCssMetaData());
        }
    }
}
