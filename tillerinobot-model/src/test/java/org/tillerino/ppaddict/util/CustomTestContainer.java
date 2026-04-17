package org.tillerino.ppaddict.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.testcontainers.containers.GenericContainer;

public class CustomTestContainer extends GenericContainer<CustomTestContainer> {
    public final List<String> logs = new ArrayList<>();

    public CustomTestContainer(String image) {
        super(image);
        setUpLogging();
    }

    public CustomTestContainer(final Future<String> image) {
        super(image);
        setUpLogging();
    }

    public CustomTestContainer logging(String loggerName) {
        return withLogConsumer(frame -> System.out.println(loggerName + ": " + frame.getUtf8StringWithoutLineEnding()));
    }

    private void setUpLogging() {
        withLogConsumer(frame -> logs.add(frame.getUtf8StringWithoutLineEnding()));
    }
}
