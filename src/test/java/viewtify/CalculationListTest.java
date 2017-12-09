/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

import kiss.Variable;

/**
 * @version 2017/12/05 23:45:03
 */
public class CalculationListTest {

    @Test
    public void map() throws Exception {
        ObservableList<String> source = FXCollections.observableArrayList("one", "two", "three");
        CalcList<String> result = CalcList.calculate(source).map(v -> v.toUpperCase());
        assert result.size() == 3;
        assert result.get().get(0).equals("ONE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.isValid() == true;

        source.add("four");
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("ONE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.get().get(3).equals("FOUR");
        assert result.isValid() == true;
    }

    @Test
    public void flatObservable() throws Exception {
        Value<String> v1 = Value.of("one");
        Value<String> v2 = Value.of("two");
        Value<String> v3 = Value.of("three");
        ObservableList<Value<String>> source = FXCollections.observableArrayList(v1, v2, v3);
        CalcList<String> result = CalcList.calculate(source).flatObservable(v -> v.property);
        assert result.size() == 3;
        assert result.get().get(0).equals("one");
        assert result.get().get(1).equals("two");
        assert result.get().get(2).equals("three");

        // add to source list
        Value<String> v4 = Value.of("four");
        source.add(v4);
        assert result.size() == 4;
        assert result.get().get(0).equals("one");
        assert result.get().get(1).equals("two");
        assert result.get().get(2).equals("three");
        assert result.get().get(3).equals("four");

        // change on source list
        source.set(0, Value.of("change"));
        assert result.size() == 4;
        assert result.get().get(0).equals("change");
        assert result.get().get(1).equals("two");
        assert result.get().get(2).equals("three");
        assert result.get().get(3).equals("four");

        // change on source item
        v2.property.set("TWO");
        assert result.size() == 4;
        assert result.get().get(0).equals("change");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("three");
        assert result.get().get(3).equals("four");

        // dispose
        result.dispose();
        v2.property.set("No Effect");
        assert result.isValid() == true;
        source.remove(0);
        assert result.isValid() == true;
        source.add(0, v1);
        assert result.isValid() == true;
    }

    @Test
    public void flatObservableAndMap() throws Exception {
        Value<String> v1 = Value.of("one");
        Value<String> v2 = Value.of("two");
        Value<String> v3 = Value.of("three");
        ObservableList<Value<String>> source = FXCollections.observableArrayList(v1, v2, v3);
        CalcList<String> result = CalcList.calculate(source).flatObservable(v -> v.property).map(String::toUpperCase);
        assert result.size() == 3;
        assert result.get().get(0).equals("ONE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.isValid() == true;

        // add to source list
        Value<String> v4 = Value.of("four");
        source.add(v4);
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("ONE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.get().get(3).equals("FOUR");
        assert result.isValid() == true;

        // change on source list
        source.set(0, Value.of("change"));
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("CHANGE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.get().get(3).equals("FOUR");
        assert result.isValid() == true;

        // change on source item
        v2.property.set("property");
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("CHANGE");
        assert result.get().get(1).equals("PROPERTY");
        assert result.get().get(2).equals("THREE");
        assert result.get().get(3).equals("FOUR");
        assert result.isValid() == true;

        // dispose
        result.dispose();
        v2.property.set("No Effect");
        assert result.isValid() == true;
        source.remove(0);
        assert result.isValid() == true;
        source.add(0, v1);
        assert result.isValid() == true;
    }

    @Test
    public void flatObservableAndReduce() throws Exception {
        Value<Integer> v1 = Value.of(1);
        Value<Integer> v2 = Value.of(2);
        Value<Integer> v3 = Value.of(3);
        ObservableList<Value<Integer>> source = FXCollections.observableArrayList(v1, v2, v3);
        Calculation<Integer> result = CalcList.calculate(source).flatObservable(v -> v.property).reduce(0, (p, q) -> p + q);
        assert result.get() == 6;

        // add to source list
        Value<Integer> v4 = Value.of(4);
        source.add(v4);
        assert result.get() == 10;

        // change on source list
        source.set(0, Value.of(10));
        assert result.get() == 19;

        // change on source item
        v2.property.set(5);
        assert result.get() == 22;

        // dispose
        result.dispose();
        v2.property.set(1000);
        assert result.isValid() == true;
        source.remove(0);
        assert result.isValid() == true;
        source.add(0, v1);
        assert result.isValid() == true;
    }

    @Test
    public void flatVariable() throws Exception {
        Value<String> v1 = Value.of("one");
        Value<String> v2 = Value.of("two");
        Value<String> v3 = Value.of("three");
        ObservableList<Value<String>> source = FXCollections.observableArrayList(v1, v2, v3);
        CalcList<String> result = CalcList.calculate(source).flatVariable(v -> v.variable);
        assert result.isValid() == false;
        assert result.size() == 3;
        assert result.get().get(0).equals("one");
        assert result.get().get(1).equals("two");
        assert result.get().get(2).equals("three");
        assert result.isValid() == true;

        // add to source list
        Value<String> v4 = Value.of("four");
        source.add(v4);
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("one");
        assert result.get().get(1).equals("two");
        assert result.get().get(2).equals("three");
        assert result.get().get(3).equals("four");
        assert result.isValid() == true;

        // change on source list
        source.set(0, Value.of("change"));
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("change");
        assert result.get().get(1).equals("two");
        assert result.get().get(2).equals("three");
        assert result.get().get(3).equals("four");
        assert result.isValid() == true;

        // change on source item
        v2.variable.set("variable");
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("change");
        assert result.get().get(1).equals("variable");
        assert result.get().get(2).equals("three");
        assert result.get().get(3).equals("four");
        assert result.isValid() == true;

        // dispose
        result.dispose();
        v2.variable.set("No Effect");
        assert result.isValid() == true;
        source.remove(0);
        assert result.isValid() == true;
        source.add(0, v1);
        assert result.isValid() == true;
    }

    @Test
    public void flatVariableAndMap() throws Exception {
        Value<String> v1 = Value.of("one");
        Value<String> v2 = Value.of("two");
        Value<String> v3 = Value.of("three");
        ObservableList<Value<String>> source = FXCollections.observableArrayList(v1, v2, v3);
        CalcList<String> result = CalcList.calculate(source).flatVariable(v -> v.variable).map(String::toUpperCase);
        assert result.size() == 3;
        assert result.get().get(0).equals("ONE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.isValid() == true;

        // add to source list
        Value<String> v4 = Value.of("four");
        source.add(v4);
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("ONE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.get().get(3).equals("FOUR");
        assert result.isValid() == true;

        // change on source list
        source.set(0, Value.of("change"));
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("CHANGE");
        assert result.get().get(1).equals("TWO");
        assert result.get().get(2).equals("THREE");
        assert result.get().get(3).equals("FOUR");
        assert result.isValid() == true;

        // change on source item
        v2.variable.set("variable");
        assert result.isValid() == false;
        assert result.size() == 4;
        assert result.get().get(0).equals("CHANGE");
        assert result.get().get(1).equals("VARIABLE");
        assert result.get().get(2).equals("THREE");
        assert result.get().get(3).equals("FOUR");
        assert result.isValid() == true;

        // dispose
        result.dispose();
        v2.variable.set("No Effect");
        assert result.isValid() == true;
        source.remove(0);
        assert result.isValid() == true;
        source.add(0, v1);
        assert result.isValid() == true;
    }

    @Test
    public void flatVariableAndReduce() throws Exception {
        Value<Integer> v1 = Value.of(1);
        Value<Integer> v2 = Value.of(2);
        Value<Integer> v3 = Value.of(3);
        ObservableList<Value<Integer>> source = FXCollections.observableArrayList(v1, v2, v3);
        Calculation<Integer> result = CalcList.calculate(source).flatVariable(v -> v.variable).reduce(0, (p, q) -> p + q);
        assert result.get() == 6;
        assert result.isValid() == true;

        // add to source list
        Value<Integer> v4 = Value.of(4);
        source.add(v4);
        assert result.isValid() == false;
        assert result.get() == 10;
        assert result.isValid() == true;

        // change on source list
        source.set(0, Value.of(10));
        assert result.isValid() == false;
        assert result.get() == 19;
        assert result.isValid() == true;

        // change on source item
        v2.variable.set(5);
        assert result.isValid() == false;
        assert result.get() == 22;
        assert result.isValid() == true;

        // dispose
        result.dispose();
        v2.variable.set(10000);
        assert result.isValid() == true;
        source.remove(0);
        assert result.isValid() == true;
        source.add(0, v1);
        assert result.isValid() == true;
    }

    @Test
    public void complex() throws Exception {
        Value<String> v1 = Value.of("one");
        Value<String> v2 = Value.of("two");
        ObservableList<Box> source = FXCollections.observableArrayList(new Box(v1), new Box(v2));

        Calculation<Boolean> result = CalcList.calculate(source).map(box -> box.item).flatVariable(v -> v.variable).isNot("ACTIVE");
        assert result.isValid() == false;
        assert result.get() == true;
        assert result.isValid() == true;

        Value<String> v3 = Value.of("ACTIVE");
        source.add(new Box(v3));
        source.remove(0);
        assert result.isValid() == false;
        assert result.get() == false;
        assert result.isValid() == true;
    }

    /**
     * @version 2017/12/06 1:26:43
     */
    private static class Value<T> {

        public final Variable<T> variable = Variable.empty();

        public final ObjectProperty<T> property = new SimpleObjectProperty();

        /**
         * Create new Value<String>.
         * 
         * @param Value<String>
         * @return
         */
        public static <T> Value<T> of(T value) {
            Value<T> v = new Value<T>();
            v.variable.set(value);
            v.property.set(value);

            return v;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Value [variable=" + variable + ", property=" + property + "]";
        }
    }

    /**
     * @version 2017/12/09 1:43:04
     */
    private static class Box {

        private Value<String> item;

        /**
         * @param item
         */
        private Box(Value<String> item) {
            this.item = item;
        }

    }
}
