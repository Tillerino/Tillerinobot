package org.tillerino.ppaddict.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * This is a simple timer to measure the duration of phases of a process. Since there is a lot of heap activity, this is
 * meant for rough measurements. It is meant to be serialized to be able to trace events through all queues and threads.
 */
@Getter
@Setter
public class PhaseTimer {
    public static final String PREPROCESS = "preprocess";
    public static final String INTERNAL_QUEUE = "eventQueue";
    public static final String THREAD_POOL_QUEUE = "threadPoolQueue";
    public static final String HANDLE = "handle";
    public static final String RESPONSE_QUEUE = "responseQueue";
    public static final String POSTPROCESS = "postprocess";

    private static final ThreadLocal<PhaseTimer> current = new ThreadLocal<>();
    private long endOfLastPhase = System.nanoTime();
    private @Nullable List<Phase> completedPhases;
    private @Nullable List<Task> currentTasks;
    private @Nullable Long nested;

    public synchronized void completePhase(String name) {
        long now = System.nanoTime();
        if (completedPhases == null) {
            completedPhases = new ArrayList<>();
        }
        completedPhases.add(new Phase(name, now - endOfLastPhase, currentTasks));
        endOfLastPhase = now;
        currentTasks = null;
    }

    public synchronized void print() {
        if (completedPhases == null) {
            return;
        }
        System.out.println("total:"
                + leftPad((int) Math.round(
                        completedPhases.stream().mapToLong(p -> p.duration).sum() / 1_000_000D)) + "ms"
                + completedPhases.stream().map(p -> ", " + p).collect(Collectors.joining()));
    }

    public QuietCloseable pinToThread() {
        if (current.get() != null) {
            throw new IllegalStateException("PhaseTimer already pinned to thread");
        }
        current.set(this);
        return () -> {
            if (current.get() != this) {
                throw new IllegalStateException("PhaseTimer not pinned to thread");
            }
            current.set(null);
        };
    }

    public static QuietCloseable timeTask(String name) {
        PhaseTimer phaseTimer = current.get();
        if (phaseTimer == null) {
            // fine
            return () -> {};
        }
        long start = System.nanoTime();
        Long originalNested;
        synchronized (phaseTimer) {
            originalNested = phaseTimer.nested;
            phaseTimer.nested = 0L;
        }
        return () -> {
            synchronized (phaseTimer) {
                if (phaseTimer.currentTasks == null) {
                    phaseTimer.currentTasks = new ArrayList<>();
                }
                long total = System.nanoTime() - start;
                long self = total - phaseTimer.nested;
                phaseTimer.nested = originalNested != null ? originalNested + total : null;
                phaseTimer.currentTasks.add(new Task(name, total, self));
            }
        };
    }

    public static String leftPad(int x) {
        return StringUtils.leftPad(Integer.toString(x), 5);
    }

    record Phase(String name, long duration, List<Task> tasks) {
        @Override
        public String toString() {
            return name + ":" + leftPad((int) Math.round(duration / 1_000_000D)) + "ms" + formatTasks();
        }

        private String formatTasks() {
            if (tasks == null) {
                return "";
            }

            @SuppressFBWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
            record R(String name, int count, long total, long self) {
                static R from(Task task) {
                    return new R(task.name(), 1, task.duration(), task.self());
                }

                @Override
                public String toString() {
                    return name + " (" + count + "):"
                            + leftPad((int) Math.round(total / 1_000_000D)) + "ms >"
                            + leftPad((int) Math.round(self / 1_000_000D)) + "ms";
                }

                R add(R other) {
                    return new R(name, count + other.count, total + other.total, self + other.self);
                }
            }

            return tasks.stream()
                    .filter(t -> t.duration >= 500_000)
                    .map(R::from)
                    .collect(Collectors.toMap(R::name, Function.identity(), R::add, LinkedHashMap::new))
                    .values()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("|", " [", "]"));
        }
    }

    record Task(String name, long duration, long self) {}
}
