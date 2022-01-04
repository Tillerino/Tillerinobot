package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Closeable;
import java.io.Reader;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.MockUtil;
import org.testcontainers.shaded.org.apache.commons.io.input.NullReader;

import com.google.inject.AbstractModule;

@TestModule(
		value = InjectionRunnerTest.SomeModule.class,
		mocks = Closeable.class,
		binds = @Bind(api = Reader.class, impl = InjectionRunnerTest.DummyReader.class))
@RunWith(InjectionRunner.class)
public class InjectionRunnerTest {
	@Inject
	private String s;
	@Inject
	private Closeable c;
	@Inject
	private Reader a;

	@Test
	public void wasInjected() throws Exception {
		assertThat(s).isEqualTo("Hello");
		assertThat(MockUtil.isMock(c));
		assertThat(a).isInstanceOf(DummyReader.class);
	}

	public static final class SomeModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(String.class).toInstance("Hello");
		}
	}

	public static class DummyReader extends NullReader {
		public DummyReader() {
			super(0);
		}
	}
}
