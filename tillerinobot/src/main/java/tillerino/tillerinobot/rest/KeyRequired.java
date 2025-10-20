package tillerino.tillerinobot.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Marks classes and methods which require a general key to be present in the API */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface KeyRequired {}
