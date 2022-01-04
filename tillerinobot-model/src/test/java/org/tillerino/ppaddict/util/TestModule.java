package org.tillerino.ppaddict.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.AbstractModule;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestModule {
	Class<? extends AbstractModule>[] value();

	Class<?>[] mocks() default { };

	boolean cache() default true;

	Bind[] binds() default { };
}
