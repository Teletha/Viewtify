/*
 * Copyright (C) 2019 viewtify Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package viewtify.model;

import kiss.Manageable;
import kiss.Singleton;
import kiss.Storable;

/**
 * @version 2018/09/24 12:19:45
 */
@Manageable(lifestyle = Singleton.class)
public abstract class ModelHelper<Self extends ModelHelper> implements Storable<Self> {
}
