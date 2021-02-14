package org.tillerino.ppaddict.util;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

/**
 * Must be annotated with {@link TestModule}. All {@link Inject} fields will be
 * injected before each test method.
 */
public abstract class AbstractInjectingTest {
	@Rule
	public final DescriptionRule description = new DescriptionRule();

	private List<AbstractModule> context;

	private static final Map<Set<Class<? extends AbstractModule>>, List<AbstractModule>> cachedInjectors = new LinkedHashMap<>() {
		private static final long serialVersionUID = 1L;
		protected boolean removeEldestEntry(Map.Entry<Set<Class<? extends AbstractModule>>, List<AbstractModule>> eldest) {
			return size() > 10;
		};
	};

	@Before
	public void inject() throws Exception {
		context = createInjector(getConfig());
		Guice.createInjector(context).injectMembers(this);
	}

	@After
	public void resetModules() throws Exception {
		if (context == null) {
			return;
		}

		for (AbstractModule module : context) {
			if (module instanceof ResettableModule) {
				ResettableModule resettable = (ResettableModule) module;
				resettable.reset();
			}
		}
	}

	private TestModule getConfig() {
		for (Class<?> testClass = description.description.getTestClass(); testClass != null; testClass = testClass
				.getSuperclass()) {
			if (testClass.isAnnotationPresent(TestModule.class)) {
				return testClass.getAnnotation(TestModule.class);
			}
		}
		throw new AssertionError("TestModule annotation not found in class hierarchy");
	}

	private List<AbstractModule> createInjector(TestModule config) throws Exception {
		Set<Class<? extends AbstractModule>> l = new HashSet<>(Arrays.asList(config.value()));
		if (config.cache()) {
			return cachedInjectors.computeIfAbsent(l, AbstractInjectingTest::createInjector);
		}
		return createInjector(l);
	}

	private static List<AbstractModule> createInjector(Set<Class<? extends AbstractModule>> cls) {
			List<AbstractModule> modules = cls.stream().map(c -> {
				try {
					return c.getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException
						| NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}).collect(toList());
			return modules;
	}

	public static class DescriptionRule extends TestWatcher {
		private Description description;

		@Override
		protected void starting(Description description) {
			this.description = description;
		}
	}
}
