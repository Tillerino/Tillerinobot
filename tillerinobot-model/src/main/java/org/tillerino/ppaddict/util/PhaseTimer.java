package org.tillerino.ppaddict.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a simple timer to measure the duration of phases of a process.
 * Since there is a lot of heap activity, this is meant for rough measurements.
 * It is meant to be serialized to be able to trace events through all queues and threads.
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

	private long endOfLastPhase = System.nanoTime();
	private @Nullable List<Phase> completedPhases;

	public void completePhase(String name) {
		long now = System.nanoTime();
		if (completedPhases == null) {
			completedPhases = new ArrayList<>();
		}
		completedPhases.add(new Phase(name, now - endOfLastPhase));
		endOfLastPhase = now;
	}

	public void print() {
		if (completedPhases == null) {
			return;
		}
		System.out.println("total:" + leftPad((int) Math.round(completedPhases.stream().mapToLong(p -> p.duration).sum() / 1_000_000D)) + "ms"
				+ completedPhases.stream()
				.map(p -> ", " + p.name + ":" + leftPad((int) Math.round(p.duration / 1_000_000D)) + "ms")
				.collect(Collectors.joining()));
	}

	public static String leftPad(int x) {
		return StringUtils.leftPad(Integer.toString(x), 5);
	}

	record Phase(String name, long duration) {
	}
}
