/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.util;

import java.util.concurrent.atomic.AtomicBoolean;

import kiss.I;
import kiss.WiseRunnable;

public final class GuardedOperation {

    /** The sync state. */
    private final AtomicBoolean processing = new AtomicBoolean();

    /** The error handling. */
    private boolean ignoreError;

    /**
     * Guaranteed to be executed only once at a time. The difference with the synchronized block is
     * that it does not allow recursive execution by the same thread and if the process is rejected,
     * this operator will skip it instead of waiting.
     * 
     * @param process An atomic process.
     */
    public void guard(WiseRunnable process) {
        if (process != null && processing.compareAndSet(false, true)) {
            try {
                process.run();
            } catch (Throwable e) {
                if (!ignoreError) {
                    throw I.quiet(e);
                }
            } finally {
                processing.set(false);
            }
        }
    }

    /**
     * Ignore error while processing.
     * 
     * @return
     */
    public GuardedOperation ignoreError() {
        ignoreError = true;
        return this;
    }
}
