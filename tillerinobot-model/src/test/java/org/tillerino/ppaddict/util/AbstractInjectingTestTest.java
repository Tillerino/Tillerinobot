package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;

import com.google.inject.AbstractModule;

@TestModule(AbstractInjectingTestTest.SomeModule.class)
public class AbstractInjectingTestTest extends AbstractInjectingTest {
	@Inject
	private String s;

	@Test
	public void wasInjected() throws Exception {
		assertThat(s).isEqualTo("Hello");
	}

	public static final class SomeModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(String.class).toInstance("Hello");
		}
	}
}
