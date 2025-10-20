package org.tillerino.ppaddict.util;

public @interface Bind {
    Class<?> api();

    Class<?> impl();
}
