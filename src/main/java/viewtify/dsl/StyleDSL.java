/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify.dsl;

import kiss.Extensible;
import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2018/08/29 15:09:15
 */
@Manageable(lifestyle = Singleton.class)
public abstract class StyleDSL extends stylist.StyleDSL implements Extensible {
}
