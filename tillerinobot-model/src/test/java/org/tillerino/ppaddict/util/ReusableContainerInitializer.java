package org.tillerino.ppaddict.util;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.FailableConsumer;
import org.testcontainers.containers.GenericContainer;

@RequiredArgsConstructor
public class ReusableContainerInitializer<T extends GenericContainer<T>> {
    private final T container;

    private final FailableConsumer<T, Exception> initialize;

    private String initializedId = null;

    public T start() {
        container.start();
        if (!Objects.equals(initializedId, container.getContainerId())) {
            try {
                initialize.accept(container);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error initializing container", e);
            }
            initializedId = container.getContainerId();
        }
        return container;
    }
}
