package org.tillerino.ppaddict.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        Logger logger = LoggerFactory.getLogger(loggerName);
        return withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()));
    }

    private void setUpLogging() {
        withLogConsumer(frame -> logs.add(frame.getUtf8StringWithoutLineEnding()));
    }
}
