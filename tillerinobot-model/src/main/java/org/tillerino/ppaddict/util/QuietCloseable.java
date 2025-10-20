package org.tillerino.ppaddict.util;

/** Like {@link AutoCloseable}, but without checked exceptions. */
public interface QuietCloseable extends AutoCloseable {
    @Override
    void close();
}
