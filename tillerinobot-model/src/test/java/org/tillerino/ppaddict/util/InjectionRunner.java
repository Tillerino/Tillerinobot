package org.tillerino.ppaddict.util;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import lombok.RequiredArgsConstructor;

/**
 * Must be annotated with {@link TestModule}. All {@link Inject} fields will be
 * injected before each test method.
 */
public class InjectionRunner extends BlockJUnit4ClassRunner {
	private static final Map<Set<Class<?>>, List<AbstractModule>> cachedInjectors = new LinkedHashMap<>() {
		private static final long serialVersionUID = 1L;

		protected boolean removeEldestEntry(Map.Entry<Set<Class<?>>, List<AbstractModule>> eldest) {
			return size() > 10;
		};
	};

	public InjectionRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		context = createInjector(getHierarchy());
		super.runChild(method, notifier);
		resetModules(method, notifier);
	}

	@Override
	protected Object createTest() throws Exception {
		return Guice.createInjector(context).getInstance(getTestClass().getJavaClass());
	}

	private List<AbstractModule> context;

	private void resetModules(FrameworkMethod method, RunNotifier notifier) {
		if (context == null) {
			return;
		}

		EachTestNotifier eachTestNotifier = new EachTestNotifier(notifier, describeChild(method));

		for (AbstractModule module : context) {
			if (module instanceof ResettableModule resettable) {
				try {
					resettable.reset();
				} catch (Exception e) {
					eachTestNotifier.addFailure(e);
				}
			}
		}
	}

	private Set<Class<?>> getHierarchy() {
		Set<Class<?>> classes = new LinkedHashSet<>();
		for (Class<?> testClass = getTestClass().getJavaClass(); testClass != null; testClass = testClass
				.getSuperclass()) {
			if (testClass.isAnnotationPresent(TestModule.class)) {
				classes.add(testClass);
			}
		}
		if (classes.isEmpty()) {
			throw new AssertionError("TestModule annotation not found in class hierarchy");
		}
		return classes;
	}

	private List<AbstractModule> createInjector(Set<Class<?>> hierarchy) {
		boolean cache = true;
		Set<Class<?>> key = new LinkedHashSet<>();
		List<Class<? extends AbstractModule>> realModules = new ArrayList<>();
		List<TestModule> forMocks = new ArrayList<>();
		for (Class<?> testClass : hierarchy) {
			TestModule annotation = testClass.getAnnotation(TestModule.class);
			List<Class<? extends AbstractModule>> modules = Arrays.asList(annotation.value());
			realModules.addAll(modules);
			key.addAll(modules);
			if (annotation.mocks().length > 0) {
				key.add(testClass);
				forMocks.add(annotation);
			}
			cache &= annotation.cache();
		}
		if (cache) {
			return cachedInjectors.computeIfAbsent(key, k -> createInjectorForClasses(realModules, forMocks));
		}
		return createInjectorForClasses(realModules, forMocks);
	}

	private static List<AbstractModule> createInjectorForClasses(List<Class<? extends AbstractModule>> cls,
			List<TestModule> mocks) {
		List<AbstractModule> modules = cls.stream().map(c -> {
			try {
				return c.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}).collect(toList());
		mocks.stream().map(MocksModule::new).forEach(modules::add);
		return modules;
	}

	@RequiredArgsConstructor
	private static class MocksModule extends AbstractModule implements ResettableModule {
		private final List<Object> mocks = new ArrayList<>();
		private final TestModule declaration;

		protected void configure() {
			Stream.of(declaration.mocks()).forEach(this::mockOne);
		}

		private <T> void mockOne(Class<T> toMock) {
			T mock = Mockito.mock(toMock);
			bind(toMock).toInstance(mock);
			mocks.add(mock);
		}

		public void reset() {
			mocks.forEach(Mockito::reset);
		}
	}
}
