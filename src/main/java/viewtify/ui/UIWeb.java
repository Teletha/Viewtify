/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.ui;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;

import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import kiss.I;
import kiss.Observer;
import kiss.Signal;
import viewtify.Viewtify;

public class UIWeb extends UserInterface<UIWeb, WebView> {

    private final WebEngine engine;

    private Duration intervalForEachActions = Duration.ofMillis(200);

    /**
     * @param view
     */
    public UIWeb(View view) {
        super(new WebView(), view);
        engine = ui.getEngine();
    }

    /**
     * Load the specified page.
     * 
     * @param uri A page URI to load.
     * @return Chainable API.
     */
    public Signal<UIWeb> load(String uri) {
        LocalServer.start();

        return new Signal<UIWeb>((observer, disposer) -> {
            engine.load(uri);
            observer.accept(this);

            return disposer;
        }).flatMap(UIWeb::awaitContentLoading);
    }

    /**
     * Input text to the specified form.
     * 
     * @param selector A css selector to select input form.
     * @param inputText A text to input.
     * @return Chainable API.
     */
    public Signal<UIWeb> input(String selector, String inputText) {
        return new Signal<>((observer, disposer) -> {
            Script script = new Script();
            script.write("document.querySelector(`", selector, "`).value = `", inputText, "`;");
            script.call(observer);
            return disposer;
        });
    }

    /**
     * Click the specified element.
     * 
     * @param selector A css selector to click.
     * @return Chainable API.
     */
    public Signal<UIWeb> click(String selector) {
        return new Signal<>((observer, disposer) -> {
            Script script = new Script();
            script.write("document.querySelector(`", selector, "`).click()");
            script.call(observer);
            return disposer;
        });
    }

    public Signal<UIWeb> awaitContentLoading() {
        return new Signal<UIWeb>((observer, diposer) -> {
            Viewtify.observe(engine.getLoadWorker().stateProperty())
                    .take(state -> state == State.SUCCEEDED)
                    .take(1)
                    .effect(this::insertDriver)
                    .to(state -> observer.accept(this));
            return diposer;
        }).on(Viewtify.UIThread);
    }

    private void insertDriver() {
        Script script = new Script();
        script.write("var script = document.createElement(`script`);");
        script.write("script.setAttribute(`type`, `text/javascript`);");
        script.write("script.setAttribute(`src`, `http://localhost:7526/driver.js`);");
        script.write("document.body.appendChild(script);");
        engine.executeScript(script.code.toString());
        System.out.println("insert driver");
    }

    /**
     * 
     */
    private class Script {

        StringBuilder code = new StringBuilder();

        void write(Object... codeFragments) {
            for (Object fragment : codeFragments) {
                code.append(String.valueOf(fragment).replace('`', '"'));
            }
        }

        private void call(Observer observer) {
            try {
                engine.executeScript(code.toString());
                observer.accept(UIWeb.this);
            } catch (Throwable e) {
                observer.error(e);
            }
        }
    }

    private static class LocalServer implements HttpHandler {

        static void start() {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 7526), 0);
                server.createContext("/", new LocalServer());
                server.setExecutor(Executors.newFixedThreadPool(10));
                server.start();
                System.out.println("start server");

            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(exchange);
        }
    }
}