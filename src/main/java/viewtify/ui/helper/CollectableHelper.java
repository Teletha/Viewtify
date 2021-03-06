/*
 * Copyright (C) 2021 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify.ui.helper;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;
import viewtify.Viewtify;
import viewtify.property.SmartProperty;
import viewtify.ui.query.CompoundQuery;
import viewtify.util.GuardedOperation;
import viewtify.util.Translatable;

public interface CollectableHelper<Self extends ReferenceHolder & CollectableHelper<Self, E>, E> {

    /**
     * Returns the managed item list.
     * 
     * @return
     */
    Property<ObservableList<E>> itemsProperty();

    /**
     * Get the all items.
     * 
     * @return A live item list.
     */
    default ObservableList<E> items() {
        return refer().items.getValue();
    }

    /**
     * Sets all values as items.
     * 
     * @param items All items to set.
     * @return Chainable API.
     */
    default Self items(E... items) {
        return items(List.of(items));
    }

    /**
     * Sets all values as items.
     * 
     * @param items All items to set.
     * @return Chainable API.
     */
    default Self items(Signal<E> items) {
        return items(items.toList());
    }

    /**
     * Sets all values as items.
     * 
     * @param items All items to set.
     * @return Chainable API.
     */
    default Self items(BaseStream<E, ?> items) {
        return items(items::iterator);
    }

    /**
     * Sets all values as items.
     * 
     * @param items All items to set.
     * @return Chainable API.
     */
    default Self items(Iterable<E> items) {
        return items(I.signal(items));
    }

    /**
     * Sets all values as items.
     * 
     * @param items All items to set.
     * @return Chainable API.
     */
    default Self items(List<E> items) {
        Objects.requireNonNull(items);

        if (this instanceof CollectableItemRenderingHelper && !items.isEmpty() && items.get(0) instanceof Translatable) {
            ((CollectableItemRenderingHelper<?, Translatable>) this).renderByVariable(Translatable::toTraslated);
        }

        modifyItemUISafely(list -> {
            list.clear();
            list.setAll(items);
        });
        return (Self) this;
    }

    /**
     * Sets all values as items.
     * 
     * @param items All items to set.
     * @return Chainable API.
     */
    default Self items(ObservableList<E> items) {
        Objects.requireNonNull(items);

        if (this instanceof CollectableItemRenderingHelper && !items.isEmpty() && items.get(0) instanceof Translatable) {
            ((CollectableItemRenderingHelper<?, Translatable>) this).renderByVariable(Translatable::toTraslated);
        }

        if (items != null) {
            modifyItemUISafely(list -> refer().items.setValue(items));
        }
        return (Self) this;
    }

    /**
     * Specify all values from the start value to the end value.
     *
     * @param start The inclusive initial value.
     * @param end The inclusive upper bound.
     * @param mapper A value builder.
     * @return Chainable API.
     */
    default Self items(int start, int end, IntFunction<E> mapper) {
        return items(IntStream.range(start, end + 1).mapToObj(mapper).collect(Collectors.toList()));
    }

    /**
     * Initialize with the specified value. This value is automatically saved whenever it is
     * changed, and is restored the next time it is initialized.
     * 
     * @param initialItems The initial value is mandatory, null values are not accepted.
     * @return Chainable API.
     */
    default Self initialize(E... initialItems) {
        return initialize(List.of(initialItems));
    }

    /**
     * Initialize with the specified value. This value is automatically saved whenever it is
     * changed, and is restored the next time it is initialized.
     * 
     * @param initialItems The initial value is mandatory, null values are not accepted.
     * @return Chainable API.
     */
    default Self initialize(Signal<E> initialItems) {
        return initialize(initialItems.toList());
    }

    /**
     * Initialize with the specified value. This value is automatically saved whenever it is
     * changed, and is restored the next time it is initialized.
     * 
     * @param initialItems The initial value is mandatory, null values are not accepted.
     * @return Chainable API.
     */
    default Self initialize(BaseStream<E, ?> initialItems) {
        return initialize(initialItems::iterator);
    }

    /**
     * Initialize with the specified value. This value is automatically saved whenever it is
     * changed, and is restored the next time it is initialized.
     * 
     * @param initialItems The initial value is mandatory, null values are not accepted.
     * @return Chainable API.
     */
    default Self initialize(Iterable<E> initialItems) {
        return initialize(I.signal(initialItems));
    }

    /**
     * Initialize with the specified value. This value is automatically saved whenever it is
     * changed, and is restored the next time it is initialized.
     * 
     * @param initialItems The initial value is mandatory, null values are not accepted.
     * @return Chainable API.
     */
    default Self initialize(List<E> initialItems) {
        return initialize(initialItems.isEmpty() ? null : initialItems.get(0), initialItems);
    }

    /**
     * Initialize with the specified value. This value is automatically saved whenever it is
     * changed, and is restored the next time it is initialized.
     * 
     * @param defaultValue A default value.
     * @param initialItems The initial value is mandatory, null values are not accepted.
     * @return Chainable API.
     */
    default Self initialize(E defaultValue, List<E> initialItems) {
        if (defaultValue != null && !initialItems.contains(defaultValue)) {
            initialItems.add(defaultValue);
        }

        items(initialItems);

        if (this instanceof ValueHelper && !initialItems.isEmpty()) {
            ((ValueHelper) this).initialize(defaultValue);
        }
        return (Self) this;
    }

    /**
     * Returns the first item.
     * 
     * @return
     */
    default Variable<E> first() {
        ObservableList<E> items = refer().items.getValue();

        if (items.isEmpty()) {
            return Variable.empty();
        } else {
            return Variable.of(items.get(0));
        }
    }

    /**
     * Returns the last item.
     * 
     * @return
     */
    default Variable<E> last() {
        ObservableList<E> items = refer().items.getValue();

        if (items.isEmpty()) {
            return Variable.empty();
        } else {
            return Variable.of(items.get(items.size() - 1));
        }
    }

    /**
     * Return the number of items.
     * 
     * @return
     */
    default int size() {
        return refer().items.getValue().size();
    }

    /**
     * Return the index of the specified item or -1 if it is not found.
     * 
     * @param item A target to search.
     * @return An item index.
     */
    default int indexOf(E item) {
        return refer().items.getValue().indexOf(item);
    }

    /**
     * Check whether this collection has the specified item or not.
     * 
     * @param item A target to search.
     * @return A result.
     */
    default boolean has(E item) {
        return indexOf(item) != -1;
    }

    /**
     * Add the specified item.
     * 
     * @param index An index to add.
     * @param item An item to add.
     * @return Chainable API.
     */
    default Self addItemAt(int index, E item) {
        if (item != null && 0 <= index) {
            modifyItemUISafely(list -> list.add(Math.min(index, list.size()), item));
        }
        return (Self) this;
    }

    /**
     * Add the specified item.
     * 
     * @param index An index to add.
     * @param item An item to add.
     * @return Chainable API.
     */
    default Self addItemAtIfAbsent(int index, E item) {
        if (!has(item)) {
            addItemAt(index, item);
        }
        return (Self) this;
    }

    /**
     * Add the specified item at the first.
     * 
     * @param item An item to add.
     * @return Chainable API.
     */
    default Self addItemAtFirst(E item) {
        modifyItemUISafely(list -> list.add(0, item));
        return (Self) this;
    }

    /**
     * Add the specified item at the first.
     * 
     * @param item An item to add.
     * @return Chainable API.
     */
    default Self addItemAtFirstIfAbsent(E item) {
        if (!has(item)) {
            addItemAtFirst(item);
        }
        return (Self) this;
    }

    /**
     * Add the specified item at the last.
     * 
     * @param item An item to add.
     * @return Chainable API.
     */
    default Self addItemAtLast(E item) {
        if (item != null) {
            modifyItemUISafely(list -> list.add(item));
        }
        return (Self) this;
    }

    /**
     * Add the specified item at the last.
     * 
     * @param item An item to add.
     * @return Chainable API.
     */
    default Self addItemAtLastIfAbsent(E item) {
        if (!has(item)) {
            addItemAtLast(item);
        }
        return (Self) this;
    }

    /**
     * Remove the specified item.
     * 
     * @param item An item to remove.
     * @return Chainable API.
     */
    default Self removeItem(E item) {
        if (item != null) {
            modifyItemUISafely(list -> {
                Iterator<E> iterator = list.iterator();

                while (iterator.hasNext()) {
                    E next = iterator.next();

                    if (next == item) {
                        iterator.remove();
                        break;
                    }
                }
            });
        }
        return (Self) this;
    }

    /**
     * Remove all items.
     * 
     * @return
     */
    default Self removeItemAll() {
        modifyItemUISafely(List<E>::clear);
        return (Self) this;
    }

    /**
     * Remove the first item.
     * 
     * @return Chainable API.
     */
    default Self removeItemAtFirst() {
        modifyItemUISafely(list -> {
            Iterator<E> iterator = list.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        });
        return (Self) this;
    }

    /**
     * Remove the last item.
     * 
     * @return Chainable API.
     */
    default Self removeItemAtLast() {
        modifyItemUISafely(list -> {
            ListIterator<E> iterator = list.listIterator(list.size());
            if (iterator.hasPrevious()) {
                iterator.previous();
                iterator.remove();
            }
        });
        return (Self) this;
    }

    /**
     * Modify items in UI thread.
     * 
     * @param action
     */
    private void modifyItemUISafely(Consumer<ObservableList<E>> action) {
        Viewtify.inUI(() -> action.accept(refer().items.getValue()));
    }

    /**
     * Sort items by the specified {@link Comparator}.
     * 
     * @param sorter A item comparator.
     * @return Chainable API.
     */
    default Self sort(Comparator<E> sorter) {
        refer().sorter.set(sorter);
        return (Self) this;
    }

    /**
     * Get the filetering state.
     * 
     * @return
     */
    default Signal<Boolean> isFiltering() {
        return refer().filtering.expose.diff();
    }

    /**
     * Filter items by the specified condition.
     * 
     * @param filter A conditional filter.
     * @return Chainable API.
     */
    default Self take(Predicate<E> filter) {
        refer().filter.set(filter);
        return (Self) this;
    }

    /**
     * Filter items by the specified condition.
     * 
     * @param context An additional infomation.
     * @param filter A conditional filter.
     * @return Chainable API.
     */
    default <C> Self take(ObservableValue<C> context, BiPredicate<E, C> filter) {
        Viewtify.observing(context).to(c -> take(e -> filter.test(e, c)));
        return (Self) this;
    }

    /**
     * Filter items by the specified condition.
     * 
     * @param context An additional infomation.
     * @param filter A conditional filter.
     * @return Chainable API.
     */
    default <C> Self take(ValueHelper<?, C> context, BiPredicate<E, C> filter) {
        return take(context.valueProperty(), filter);
    }

    /**
     * Filter items by the specified condition.
     * 
     * @param filter A conditional filter.
     * @return Chainable API.
     */
    default Self skip(Predicate<E> filter) {
        return take(filter.negate());
    }

    /**
     * Filter items by the specified condition.
     * 
     * @param context An additional infomation.
     * @param filter A conditional filter.
     * @return Chainable API.
     */
    default <C> Self skip(ObservableValue<C> context, BiPredicate<E, C> filter) {
        return take(context, filter.negate());
    }

    /**
     * Filter items by the specified condition.
     * 
     * @param context An additional infomation.
     * @param filter A conditional filter.
     * @return Chainable API.
     */
    default <C> Self skip(ValueHelper<?, C> context, BiPredicate<E, C> filter) {
        return take(context.valueProperty(), filter.negate());
    }

    /**
     * Get the associated {@link CompoundQuery}.
     * 
     * @return
     */
    default CompoundQuery<E> query() {
        return refer().query();
    }

    /**
     * Observe each items's state to update view.
     * 
     * @param stateExtractor
     * @return Chainable API.
     */
    default Self observeItemState(Function<E, Variable> stateExtractor) {
        Ð<E> refer = refer();
        if (refer.disposers != null) {
            refer.disposers.values().forEach(Disposable::dispose);
            refer.disposers.clear();
        }
        refer.disposers = new WeakHashMap();
        refer.notifier = stateExtractor;
        return (Self) this;
    }

    /**
     * Retrieve the special reference holder.
     * 
     * @return
     */
    private Ð<E> refer() {
        ReferenceHolder holder = (ReferenceHolder) this;

        if (holder.collectable == null) {
            synchronized (this) {
                if (holder.collectable == null) {
                    holder.collectable = new Ð(this);
                }
            }
        }
        return holder.collectable;
    }

    /**
     * 
     */
    final class Ð<E> implements ListChangeListener<E> {

        /** The item holder. */
        private final Property<ObservableList<E>> items = new SmartProperty();

        /** The filtered state. */
        private final Signaling<Boolean> filtering = new Signaling();

        /** The item taking filter. */
        private final Variable<Predicate<E>> filter = Variable.empty();

        /** The item sorter. */
        private final Variable<Comparator<E>> sorter = Variable.empty();

        /** The item state observers. */
        private Function<E, Variable> notifier;

        /** The disposer for observers. */
        private WeakHashMap<E, Disposable> disposers;

        /** The sync state. */
        private final GuardedOperation updating = new GuardedOperation();

        /** Lazy initialization. */
        private volatile CompoundQuery<E> query;

        /**
         * Initialize date reference.
         * 
         * @param helper
         */
        private Ð(CollectableHelper<?, E> helper) {
            ObservableList<E> list = helper.itemsProperty().getValue();
            items.setValue(list);
            list.addListener(this);

            Viewtify.observing(items).combineLatest(filter.observing(), sorter.observing()).to(v -> {
                updating.guard(() -> {
                    ObservableList items = v.ⅰ;

                    if (v.ⅱ != null) {
                        FilteredList<E> filtered = items.filtered(v.ⅱ);
                        filtering.accept(items.size() != filtered.size());
                        items = filtered;
                    }

                    if (v.ⅲ != null) {
                        items = items.sorted(v.ⅲ);
                    }
                    helper.itemsProperty().setValue(items);
                });
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChanged(Change<? extends E> c) {
            if (notifier != null) {
                while (c.next()) {
                    for (E item : c.getRemoved()) {
                        Disposable disposable = disposers.remove(item);
                        if (disposable != null) {
                            disposable.dispose();
                        }
                    }
                    for (E item : c.getAddedSubList()) {
                        disposers.put(item, notifier.apply(item).observe().to(() -> {
                            // Dirty Hack : notify item change event to the source observable list
                            ObservableList<E> list = items.getValue();
                            list.set(list.indexOf(item), item);
                        }));
                    }
                }
            }
        }

        /**
         * Get the associated {@link CompoundQuery} lazily.
         * 
         * @return
         */
        private CompoundQuery<E> query() {
            if (query == null) {
                synchronized (this) {
                    if (query == null) {
                        query = new CompoundQuery();
                        query.updated.to(filter::set);
                    }
                }
            }
            return query;
        }
    }
}