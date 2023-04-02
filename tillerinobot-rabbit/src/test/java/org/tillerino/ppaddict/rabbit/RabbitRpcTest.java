package org.tillerino.ppaddict.rabbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.tillerino.ppaddict.util.Result.err;
import static org.tillerino.ppaddict.util.Result.ok;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.awaitility.Awaitility;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.MDC;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.Result;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcServer;

public class RabbitRpcTest {
	public RabbitMqContainerConnection rabbit = new RabbitMqContainerConnection();

	public ExecutorServiceRule exec = new ExecutorServiceRule(Executors::newCachedThreadPool);

	@Rule
	public RuleChain rules = RuleChain.outerRule(exec)
		.around(rabbit);

	interface MyRpcInterface {
		@Rpc(queue = "simple_call", timeout = 1000)
		Result<String, String> aSimpleCall(int arg);
	}

	@Test
	public void testTimeout() throws Exception {
		MyRpcInterface proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), MyRpcInterface.class, "Timeout");
		// no server running
		assertThat(proxy.aSimpleCall(1)).isEqualTo(err("Timeout"));

		Channel channel = rabbit.getConnection().createChannel();
		// wait for message TTL
		Awaitility.await().until(() -> channel.messageCount("simple_call") == 0L);

		// start server. we can't spy on a lambda, so full declaration here
		MyRpcInterface handler = spy(new MyRpcInterface() {
			@Override
			public Result<String, String> aSimpleCall(int arg) {
				System.out.println("SERVER: " + arg);
				return Result.ok("x" + arg);
			}
		});
		RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), MyRpcInterface.class, handler, "Error");
		exec.submit(rpcServer::mainloop);

		// now call is working
		assertThat(proxy.aSimpleCall(2)).isEqualTo(ok("x2"));
		// expired message never reached server
		verify(handler, only()).aSimpleCall(2);
	}

	@Test
	public void testMdc() throws Exception {
		MyRpcInterface proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), MyRpcInterface.class, "Timeout");
		RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), MyRpcInterface.class, x -> {
			MDC.put("mdcserverkey", "mdcserverval" + x);
			return ok("x" + x + MDC.get("mdckey"));
		}, "Error");
		exec.submit(rpcServer::mainloop);

		MDC.clear();
		assertThat(proxy.aSimpleCall(1)).isEqualTo(ok("x1null"));
		assertThat(MDC.get("mdcserverkey")).isEqualTo("mdcserverval1");

		MDC.clear();
		MDC.put("mdckey", "mdcval");
		assertThat(proxy.aSimpleCall(2)).isEqualTo(ok("x2mdcval"));
		assertThat(MDC.get("mdcserverkey")).isEqualTo("mdcserverval2");
		MDC.clear();
	}

	@Test
	public void serverSideError() throws Exception {
		MyRpcInterface proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), MyRpcInterface.class, "Timeout");
		RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), MyRpcInterface.class, x -> {
			throw new RuntimeException("failure for testing");
		}, "Error");
		exec.submit(rpcServer::mainloop);

		assertThat(proxy.aSimpleCall(1)).isEqualTo(err("Error"));
	}

	@Test
	public void multipleMethods() throws Exception {
		interface MultiRpcInterface extends MyRpcInterface {
			@Rpc(queue = "simple_call", timeout = 1000)
			Result<Integer, String> parseString(String arg);
		}

		MultiRpcInterface proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), MultiRpcInterface.class, "Timeout");
		RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), MultiRpcInterface.class, new MultiRpcInterface() {
			@Override
			public Result<String, String> aSimpleCall(int arg) {
				return ok("" + arg);
			}

			@Override
			public Result<Integer, String> parseString(String arg) {
				return ok(Integer.valueOf(arg));
			}
		}, "Error");
		exec.submit(rpcServer::mainloop);

		assertThat(proxy.aSimpleCall(1)).isEqualTo(ok("1"));
		assertThat(proxy.parseString("2")).isEqualTo(ok(2));
	}

	@Test
	public void defaultMethod() throws Exception {
		interface WithDefaultMethod extends MyRpcInterface {
			default Result<String, String> outer(Integer arg) {
				return aSimpleCall(arg + 10);
			}
		}

		WithDefaultMethod proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), WithDefaultMethod.class, "Timeout");
		RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), WithDefaultMethod.class, x -> ok("x" + x), "Error");
		exec.submit(rpcServer::mainloop);

		assertThat(proxy.outer(1)).isEqualTo(ok("x11"));
}

	@Test
	public void allTheValidations() throws Exception {
		interface Empty {
		}
		assertThatThrownBy(() -> RabbitRpc.remoteCallProxy(rabbit.getConnection(), Empty.class, "Timeout"))
			.hasMessageEndingWith("Empty has no methods annotated with @Rpc.");

		interface NotEverythingAnnotated {
			void x();
		}
		assertThatThrownBy(() -> RabbitRpc.remoteCallProxy(rabbit.getConnection(), NotEverythingAnnotated.class, "Timeout"))
			.hasMessageEndingWith("NotEverythingAnnotated.x() not annotated with @Rpc.");

		interface NonUniqueQueueNames {
			@Rpc(queue = "queue_one", timeout = 12)
			Result<String, String> x();

			@Rpc(queue = "queue_two", timeout = 12)
			Result<Integer, String> y();
		}
		assertThatThrownBy(() -> RabbitRpc.remoteCallProxy(rabbit.getConnection(), NonUniqueQueueNames.class, "Timeout"))
			.hasMessageStartingWith("Queue names not unique: [queue_one, queue_two].");

		interface NotReturningResult {
			@Rpc(queue = "queue_name", timeout = 12)
			String x();
		}
		assertThatThrownBy(() -> RabbitRpc.remoteCallProxy(rabbit.getConnection(), NotReturningResult.class, "Timeout"))
			.hasMessageEndingWith("NotReturningResult.x() must return Result.");

		class NotAnInterface {
		}
		assertThatThrownBy(() -> RabbitRpc.remoteCallProxy(rabbit.getConnection(), NotAnInterface.class, "Timeout"))
			.hasMessageEndingWith("NotAnInterface must be an interface.");
		
		interface InconsistentErrorTypes {
			@Rpc(queue = "queue_name", timeout = 12)
			Result<String, String> x();

			@Rpc(queue = "queue_name", timeout = 13)
			Result<Integer, Integer> y();
		}
		assertThatThrownBy(() -> RabbitRpc.remoteCallProxy(rabbit.getConnection(), InconsistentErrorTypes.class, "Timeout"))
			.hasMessageStartingWith("Return error types on one interface must be consistent.");

		interface DuplicatedMethodName {
			@Rpc(queue = "queue_name", timeout = 12)
			Result<String, String> x(String s);

			@Rpc(queue = "queue_name", timeout = 13)
			Result<Integer, String> x();
		}
		assertThatThrownBy(() -> RabbitRpc.remoteCallProxy(rabbit.getConnection(), DuplicatedMethodName.class, "Timeout"))
			.hasMessageStartingWith("Method names must be unique.");
	}

	@Test
	@Ignore
	public void sequentialThroughput() throws Exception {
		MyRpcInterface proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), MyRpcInterface.class, "Timeout");
		RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), MyRpcInterface.class, x -> ok("x" + x), "Error");
		exec.submit(rpcServer::mainloop);

		long now = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			assertThat(proxy.aSimpleCall(i)).isEqualTo(ok("x" + i));
		}
		System.out.println(System.currentTimeMillis() - now);
	}

	@Test
	@Ignore
	public void manyToOneThroughput() throws Exception {
		MyRpcInterface proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), MyRpcInterface.class, "Timeout");
		RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), MyRpcInterface.class, x -> ok("x" + x), "Error");
		exec.submit(rpcServer::mainloop);

		long now = System.currentTimeMillis();
		IntFunction<Future<?>> mapper = __ -> exec.submit(() -> {
			for (int i = 0; i < 10000; i++) {
				assertThat(proxy.aSimpleCall(i)).isEqualTo(ok("x" + i));
			}
		});
		List<Future<?>> futures = IntStream.range(0, 10).mapToObj(mapper).toList();
		for (Future<?> fut : futures) {
			fut.get();
		}
		System.out.println(System.currentTimeMillis() - now);
	}

	@Test
	@Ignore
	public void manyToManyThroughput() throws Exception {
		MyRpcInterface proxy = RabbitRpc.remoteCallProxy(rabbit.getConnection(), MyRpcInterface.class, "Timeout");
		// start 10 servers
		for(int i = 0; i < 10; i++) {
			RpcServer rpcServer = RabbitRpc.handleRemoteCalls(rabbit.getConnection(), MyRpcInterface.class, x -> ok("x" + x), "Error");
			exec.submit(rpcServer::mainloop);
		}

		long now = System.currentTimeMillis();
		IntFunction<Future<?>> mapper = __ -> exec.submit(() -> {
			for (int i = 0; i < 10000; i++) {
				assertThat(proxy.aSimpleCall(i)).isEqualTo(ok("x" + i));
			}
		});
		List<Future<?>> futures = IntStream.range(0, 10).mapToObj(mapper).toList();
		for (Future<?> fut : futures) {
			fut.get();
		}
		System.out.println(System.currentTimeMillis() - now);
	}
}
