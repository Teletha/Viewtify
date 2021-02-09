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

import org.controlsfx.control.PopOver;

public abstract class ReferenceHolder {

    /** The reusable managed popup base. (SINGLETON) */
    static final PopOver popover = new PopOver();
    static {
        popover.setDetachable(false);
        popover.setOnHidden(e -> popover.setContentNode(null));
    }

    /** The reference holder. */
    volatile CollectableHelper.Ð collectable;
}