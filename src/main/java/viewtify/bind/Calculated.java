/*
 * Copyright (C) 2019 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify.bind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.Observable;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.LongExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import kiss.I;
import kiss.Signal;
import kiss.Variable;
import viewtify.Viewtify;

/**
 * @version 2017/12/06 23:28:47
 */
public class Calculated<T> extends ObjectBinding<T> {

    /** The actual calculation. */
    private final Supplier<T> calculation;

    /** Store for {@link #dispose()}. */
    private final Observable[] dependencies;

    /**
     * @param calculation
     * @param dependencies
     */
    public Calculated(Supplier calculation, Calculated outer, Observable... dependencies) {
        this.calculation = calculation;

        List<Observable> list = new ArrayList();
        if (outer != null) {
            list.add(outer);
        }

        for (Observable observable : dependencies) {
            if (observable != null) {
                list.add(observable);
            }
        }
        bind(this.dependencies = list.toArray(new Observable[list.size()]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T computeValue() {
        try {
            return calculation.get();
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        unbind(dependencies);
    }

    /**
     * Type filtering.
     * 
     * @param type
     * @return
     */
    public <R> Calculated<R> as(Class<R> type) {
        Objects.requireNonNull(type);

        return (Calculated<R>) take(v -> type.isInstance(v));
    }

    /**
     * Creates a new {@link Calculated} that holds a concatenated of the value held by this
     * {@link Calculated}, and is empty when this {@link Calculated} is empty.
     * 
     * @param mapper function to map the value held by this ObservableValue.
     */
    public Calculated<String> concat(Object value) {
        Calculated calculation = Viewtify.calculate(value).or("");

        return new Calculated<String>(() -> Objects.toString(get()) + Objects.toString(calculation.getValue()), this, calculation);
    }

    /**
     * Creates a new {@link Calculated} that holds a concatenated of the value held by this
     * {@link Calculated}, and is empty when this {@link Calculated} is empty.
     * 
     * @param mapper function to map the value held by this ObservableValue.
     */
    public Calculated<String> concat(Object... values) {
        Calculated base = this;

        for (Object value : values) {
            base = base.concat(value);
        }
        return base;
    }

    /**
     * Returns a new ObservableValue that, when this ObservableValue holds value {@code x}, holds
     * the value held by {@code f(x)}, and is empty when this ObservableValue is empty.
     */
    public <R> Calculated<R> flatObservable(Function<? super T, ObservableValue<R>> mapper) {
        return new Calculated<R>(null, this) {

            /** The latest mapper value. */
            private ObservableValue<R> latest;

            /**
             * {@inheritDoc}
             */
            @Override
            protected R computeValue() {
                if (latest != null) {
                    unbind(latest);
                }

                T value = Calculated.this.get();

                if (value == null) {
                    latest = null;
                    return null;
                }

                latest = mapper.apply(Calculated.this.get());

                if (latest == null) {
                    return null;
                } else {
                    bind(latest);
                    return latest.getValue();
                }
            }
        };
    }

    /**
     * Returns a new ObservableValue that, when this ObservableValue holds value {@code x}, holds
     * the value held by {@code f(x)}, and is empty when this ObservableValue is empty.
     */
    public Calculated<Integer> flatInteger(Function<? super T, IntegerExpression> mapper) {
        return flatObservable(mapper::apply).as(Integer.class);
    }

    /**
     * Returns a new ObservableValue that, when this ObservableValue holds value {@code x}, holds
     * the value held by {@code f(x)}, and is empty when this ObservableValue is empty.
     */
    public Calculated<Long> flatLong(Function<? super T, LongExpression> mapper) {
        return flatObservable(mapper::apply).as(Long.class);
    }

    /**
     * Returns a new ObservableValue that, when this ObservableValue holds value {@code x}, holds
     * the value held by {@code f(x)}, and is empty when this ObservableValue is empty.
     */
    public Calculated<Double> flatDouble(Function<? super T, DoubleExpression> mapper) {
        return flatObservable(mapper::apply).as(Double.class);
    }

    /**
     * Returns a new ObservableValue that holds a mapping of the value held by this ObservableValue,
     * and is empty when this ObservableValue is empty.
     * 
     * @param mapper function to map the value held by this ObservableValue.
     */
    public <R> Calculated<R> flatVariable(Function<? super T, Variable<R>> mapper) {
        return flatObservable(v -> Viewtify.calculate(mapper.apply(v)));
    }

    /**
     * Returns a new ObservableValue that holds a mapping of the value held by this ObservableValue,
     * and is empty when this ObservableValue is empty.
     * 
     * @param mapper function to map the value held by this ObservableValue.
     */
    public <R> Calculated<R> flatMap(Function<? super T, Signal<R>> mapper) {
        return flatVariable(v -> mapper.apply(v).to());
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> is(T... values) {
        return is(I.set(values));
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> is(Set<T> values) {
        return map(v -> values.contains(v));
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> is(ObservableValue<T>... values) {
        return new Calculated<>(() -> {
            T now = get();

            for (ObservableValue<T> value : values) {
                if (Objects.equals(now, value.getValue())) {
                    return true;
                }
            }
            return false;
        }, this, values);
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> is(Variable<T>... values) {
        return is(Viewtify.calculate(values));
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> isNot(T... values) {
        return isNot(I.set(values));
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> isNot(Set<T> values) {
        return map(v -> !values.contains(v));
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> isNot(ObservableValue<T>... values) {
        return new Calculated<>(() -> {
            T now = get();

            for (ObservableValue<T> value : values) {
                if (Objects.equals(now, value.getValue())) {
                    return false;
                }
            }
            return true;
        }, this, values);
    }

    /**
     * Equality check.
     * 
     * @param active
     * @return
     */
    public Calculated<Boolean> isNot(Variable<T>... values) {
        return isNot(Viewtify.calculate(values));
    }

    /**
     * Creates a new {@link Calculated} that holds {@code true} if this {@code ObjectExpression} is
     * {@code null}.
     *
     * @return A new {@code Calculation}.
     */
    public Calculated<Boolean> isAbsent() {
        return map(v -> v == null);
    }

    /**
     * Creates a new {@link Calculated} that holds {@code true} if this {@code ObjectExpression} is
     * not {@code null}.
     *
     * @return A new {@code Calculation}.
     */
    public Calculated<Boolean> isPresent() {
        return map(v -> v != null);
    }

    /**
     * Creates a new {@link Calculated} that holds a mapping of the value held by this
     * {@link Calculated}, and is empty when this {@link Calculated} is empty.
     * 
     * @param mapper function to map the value held by this ObservableValue.
     */
    public <R> Calculated<R> map(Function<? super T, ? extends R> mapper) {
        return new Calculated(() -> mapper.apply(get()), this);
    }

    /**
     * Creates a new {@link Calculated} that holds the value held by this {@link Calculated}, or
     * {@code other} when this {@link Calculated} is empty.
     */
    public Calculated<T> or(T other) {
        return new Calculated(() -> {
            T value = get();

            return value == null ? other : value;
        }, this);
    }

    /**
     * Creates a new {@link Calculated} that holds the value held by this {@link Calculated}, or
     * the value held by {@code other} when this {@link Calculated} is empty.
     */
    public Calculated<T> or(ObservableValue<T> other) {
        return new Calculated(() -> {
            T value = get();

            return value == null ? other.getValue() : value;
        }, this, other);
    }

    /**
     * Creates a new {@link Calculated} that holds the value held by this {@link Calculated}, or
     * the value held by {@code other} when this {@link Calculated} is empty.
     */
    public Calculated<T> or(Variable<T> other) {
        return or(Viewtify.calculate(other));
    }

    /**
     * Creates a new {@link Calculated} that holds the same value as this {@link Calculated} when
     * the value does not satisfy the predicate and is empty when this {@link Calculated} is empty
     * or its value satisfies the given predicate.
     */
    public Calculated<T> skip(Predicate<T> predicate) {
        return take(predicate.negate());
    }

    /**
     * Creates a new {@link Calculated} that holds the same value as this {@link Calculated} when
     * the value satisfies the predicate and is empty when this {@link Calculated} is empty or its
     * value does not satisfy the given predicate.
     */
    public Calculated<T> take(Predicate<T> predicate) {
        return new Calculated(() -> {
            T value = get();

            return predicate.test(value) ? value : null;
        }, this);
    }

    /**
     * Creates a new {@link Calculated} that holds the trimed {@link String} value.
     */
    public Calculated<String> trim() {
        return map(v -> Objects.toString(v).trim());
    }

    /**
     * Convert to {@link Signal}.
     * 
     * @return
     */
    public Signal<T> observe() {
        return new Signal<>((observer, disposer) -> {
            ChangeListener<T> listener = (s, o, n) -> observer.accept(n);

            addListener(listener);
            return disposer.add(() -> removeListener(listener));
        });
    }
}