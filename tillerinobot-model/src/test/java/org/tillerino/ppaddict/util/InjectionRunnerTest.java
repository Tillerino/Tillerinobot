package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;

@TestModule(InjectionRunnerTest.SomeModule.class)
@RunWith(InjectionRunner.class)
public class InjectionRunnerTest {
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
