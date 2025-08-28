package org.tillerino.ppaddict.util;

import dagger.Provides;

/**
 * Hides {@link System#currentTimeMillis()} for unit tests.
 */
public interface Clock {
    /**
     * Returns the current time in milliseconds.
     *
     * @return  the difference, measured in milliseconds, between
     *          the current time and midnight, January 1, 1970 UTC.
     */
    long currentTimeMillis();

    static Clock system() {
      return System::currentTimeMillis;
    }

    @dagger.Module interface Module {
        @Provides
        static Clock systemClock() {
          return System::currentTimeMillis;
        }
    }
}
