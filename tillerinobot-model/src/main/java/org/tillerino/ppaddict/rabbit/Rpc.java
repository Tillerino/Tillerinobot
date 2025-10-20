package org.tillerino.ppaddict.rabbit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.tillerino.ppaddict.util.Result;

/**
 * Marks a method as an RPC. This method can then be called across processes connected by RabbitMQ. The annotated method
 * must return a {@link Result} and must not throw any exceptions. Any thrown exception will end the server. The RPC
 * transmits the MDC back and forth: the caller's MDC is set as the callee's MDC and the callee's MDC is added to the
 * caller's MDC.
 *
 * <p>See also {@link RabbitRpc}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Rpc {
    /** @return unique name of the queue that calls are sent to. */
    String queue();

    /** @return milliseconds. */
    int timeout();
}
