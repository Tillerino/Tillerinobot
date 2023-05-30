package org.tillerino.mormon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the key columns of a persisted class.
 * Since the order of fields is not constant at runtime, it is of no use to us
 * to annotate fields with a key-annotation in the case of compound keys.
 * Instead, we use this annotation at the class level.
 *
 * <p>
 * Whenever we load data from the database without specifying a query,
 * a query is automatically constructed from this annotation.
 * The placeholders in that query are then filled from the given object array.
 * The order of the values in that array must match the order of the columns in this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KeyColumn {
	String[] value();
}