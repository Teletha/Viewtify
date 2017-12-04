/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.calculation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.Test;

import kiss.Variable;
import viewtify.Viewtify;

/**
 * @version 2017/12/03 17:37:22
 */
public class CalculatableTest {

    @Test
    public void or() throws Exception {
        StringProperty p = new SimpleStringProperty("TEST");
        Calculatable<String> calc = Viewtify.calculate(p).or("OTHER");
        assert calc.get().equals("TEST");

        p.set(null);
        assert calc.get().equals("OTHER");
    }

    @Test
    public void orObservable() throws Exception {
        StringProperty p1 = new SimpleStringProperty("TEST");
        StringProperty p2 = new SimpleStringProperty("OTHER");

        Calculatable<String> calc = Viewtify.calculate(p1).or(p2);
        assert calc.get().equals("TEST");

        p1.set(null);
        assert calc.get().equals("OTHER");

        p2.set("CHANGE");
        assert calc.get().equals("CHANGE");

        p1.set("REVERT");
        assert calc.get().equals("REVERT");
    }

    @Test
    public void take() throws Exception {
        StringProperty p = new SimpleStringProperty("TEST");
        Calculatable<String> calc = Viewtify.calculate(p).take(v -> v.contains("S"));
        assert calc.get().equals("TEST");

        p.set("NOOO");
        assert calc.get() == null;
    }

    @Test
    public void map() throws Exception {
        StringProperty p = new SimpleStringProperty("TEST");
        Calculatable<String> calc = Viewtify.calculate(p).map(String::toLowerCase);
        assert calc.get().equals("test");

        p.set("CHANGE");
        assert calc.get().equals("change");

        p.set(null);
        assert calc.get() == null;
    }

    @Test
    public void calculateProperty() throws Exception {
        Nest nest = new Nest("TEST");
        ObjectProperty<Nest> p = new SimpleObjectProperty(nest);
        Calculatable<String> calc = Viewtify.calculate(p).calculateProperty(v -> v.property);
        assert calc.get().equals("TEST");

        // change inner value
        nest.property.set("CHANGE");
        assert calc.get().equals("CHANGE");

        // inner null
        nest.property.set(null);
        assert calc.get() == null;

        // change outer value
        p.set(new Nest("OUTER"));
        assert calc.get().equals("OUTER");

        // outer null
        p.set(null);
        assert calc.get() == null;
    }

    @Test
    public void calculateVariable() throws Exception {
        Nest nest = new Nest("TEST");
        ObjectProperty<Nest> p = new SimpleObjectProperty(nest);
        Calculatable<String> calc = Viewtify.calculate(p).calculateVariable(v -> v.variable);
        assert calc.get().equals("TEST");

        // change inner value
        nest.variable.set("CHANGE");
        assert calc.get().equals("CHANGE");

        // inner null
        nest.variable.set((String) null);
        assert calc.get() == null;

        // change outer value
        p.set(new Nest("OUTER"));
        assert calc.get().equals("OUTER");

        // outer null
        p.set(null);
        assert calc.get() == null;
    }

    /**
     * @version 2017/12/03 17:57:45
     */
    private static class Nest {

        private StringProperty property = new SimpleStringProperty();

        private Variable<String> variable = Variable.empty();

        private Nest(String text) {
            this.property.set(text);
            this.variable.set(text);
        }
    }
}