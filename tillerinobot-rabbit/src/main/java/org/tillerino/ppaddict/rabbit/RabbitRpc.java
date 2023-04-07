package org.tillerino.ppaddict.rabbit;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.MDC;
import org.tillerino.ppaddict.rabbit.RabbitRpc.InterfaceProps.MethodProps;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcSnapshot;
import org.tillerino.ppaddict.util.Result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.RpcClientParams;
import com.rabbitmq.client.RpcServer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for setting up RPCs through RabbitMQ, see also {@link Rpc}.
 */
@Slf4j
public class RabbitRpc {
	/**
	 * Implements a dynamic proxy which performs RPCs to implement the interfaces
	 * methods.
	 *
	 * @param <I> interface must have a single, {@link Rpc}-annotated method.
	 * @return this instance can be used concurrently by multiple threads
	 */
	public static <I> I remoteCallProxy(Connection connection, Class<I> cls, @NonNull Object timeoutResponse) {
		InterfaceProps<I> interfaceProps = new InterfaceProps<>(cls, RabbitMqConfiguration.mapper());
		Validate.isTrue(TypeUtils.isAssignable(timeoutResponse.getClass(), interfaceProps.errorType),
				"Timeout response must be of type %s but was %s", interfaceProps.errorType, timeoutResponse.getClass());
		Result<Object, Object> timeoutResult = Result.err(timeoutResponse);

		Object p = Proxy.newProxyInstance(cls.getClassLoader(), new Class[] { cls }, (proxy, method, args) -> {
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(proxy, method, args);
			}

			MethodProps<I> methodProps = interfaceProps.methods.get(method.getName());
			RpcClient rpcClient = methodProps.rpcClient(connection);
			String response;
			try {
				// when there are no args, the proxy gets null instead of an empty array :(
				String request = methodProps.writeJsonRequest(args != null ? args : new Object[0]);
				response = rpcClient.stringCall(request);
			} catch (TimeoutException e) {
				return timeoutResult;
			}
			return methodProps.readJsonResponse(response);
		});

		return (I) p;
	}

	/**
	 * Sets up a RabbitMQ-connected handler for a single RPC.
	 *
	 * @param <I> interface must have a single, {@link Rpc}-annotated method.
	 * @param handler must implement the RPC interface. Note that this can also
	 *        implement more RPC interfaces, but those won't be connected by this
	 *        call.
	 * @param errorResponse response in case of an error. This must be of the error
	 *        type returned by the {@link Rpc}-annotated method.
	 */
	public static <I> RpcServer handleRemoteCalls(Connection connection, Class<I> cls, I handler,
			@NonNull Object errorResponse) throws IOException {
		InterfaceProps<I> interfaceProps = new InterfaceProps<>(cls, RabbitMqConfiguration.mapper());
		Validate.isTrue(TypeUtils.isAssignable(errorResponse.getClass(), interfaceProps.errorType),
				"Error response must be of type %s but was %s", interfaceProps.errorType, errorResponse.getClass());

		return interfaceProps.rpcServer(connection, handler, errorResponse);
	}

	static class InterfaceProps<I> {
		private final ObjectMapper objectMapper;
		private final String queue;
		final Map<String, MethodProps<I>> methods = new LinkedHashMap<>();

		Type errorType = null;

		public InterfaceProps(Class<I> cls, ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
			Validate.isTrue(cls.isInterface(), "%s must be an interface.", cls);

			for (Method m : cls.getMethods()) {
				if (m.isDefault()) {
					continue;
				}

				MethodProps<I> props = new MethodProps<>(m, objectMapper);
				MethodProps<I> doubleMethod = methods.put(m.getName(), props);
				if (doubleMethod != null) {
					throw new ContextedRuntimeException("Method names must be unique.").addContextValue("interface", cls)
							.addContextValue("method", doubleMethod.method).addContextValue("method", m);
				}
				if (errorType != null && !props.errorType.equals(errorType)) {
					throw new ContextedRuntimeException("Return error types on one interface must be consistent.")
							.addContextValue("errorType", errorType).addContextValue("errorType", props.errorType);
				}
				errorType = props.errorType;
			}

			Validate.notNull(errorType, "%s has no methods annotated with @Rpc.", cls);

			Set<String> queues = methods.values().stream().map(m -> m.config.queue())
					.collect(Collectors.toCollection(TreeSet::new));
			Validate.isTrue(queues.size() == 1, "Queue names not unique: %s.", queues);
			this.queue = queues.iterator().next();
		}

		RpcRequest<I> readJsonRequest(byte[] response) throws IOException {
			JsonNode tree = objectMapper.readTree(response);
			List<String> fieldNames = IteratorUtils.toList(tree.fieldNames());
			JsonNode mdcJson = Validate.notNull(tree.get("mdc"), "Missing field mdc. Fields: %s", fieldNames);
			MdcSnapshot mdc = objectMapper.treeToValue(mdcJson, MdcSnapshot.class);

			JsonNode methodJson = Validate.notNull(tree.get("method"), "Missing field method. Fields: %s", fieldNames);
			Validate.isTrue(methodJson.isTextual());
			MethodProps<I> method = methods.get(methodJson.textValue());

			JsonNode argsJson = Validate.notNull(tree.get("args"), "Missing field args. Fields: %s", fieldNames);
			Validate.isTrue(argsJson.isArray());
			Validate.isTrue(argsJson.size() == method.argTypes.size());

			Object[] args = new Object[method.argTypes.size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = objectMapper.treeToValue(argsJson.get(i), method.argTypes.get(i));
			}

			return new RpcRequest<>(method, args, mdc);
		}

		byte[] writeJsonResponse(Object result, MdcSnapshot mdc) throws JsonProcessingException {
			ObjectNode tree = objectMapper.createObjectNode();
			tree.putPOJO("result", result);
			tree.putPOJO("mdc", mdc);
			return objectMapper.writeValueAsBytes(tree);
		}

		RpcServer rpcServer(Connection connection, I handler, Object errorResponse) throws IOException {
			Channel channel = connection.createChannel();
			channel.basicQos(1); // for proper parallelization (if needed)

			for (MethodProps<I> method : methods.values()) {
				method.declareQueue(channel);
			}

			// serialize immediately so that we can always return something
			byte[] errorBytes = writeJsonResponse(Result.err(errorResponse), MdcSnapshot.create(Map.of()));
			return new RpcServer(channel, queue) {
				@Override
				public byte[] handleCall(byte[] requestBody, BasicProperties replyProperties) {
					try {
						MDC.clear();
						RpcRequest<I> request = readJsonRequest(requestBody);
						request.mdc().apply();
						return writeJsonResponse(request.method.method.invoke(handler, request.args()),
								MdcUtils.getSnapshot());
					} catch (Exception e) {
						log.error("error handling RPC with correlation id {}", replyProperties.getCorrelationId(), e);
						return errorBytes;
					} finally {
						MDC.clear();
					}
				}
			};
		}

		static class MethodProps<I> {
			private final ObjectMapper objectMapper;
			private final Rpc config;
			private final JavaType jacksonReturnType;
			private final Type errorType;
			private final List<JavaType> argTypes;
			private final Method method;

			RpcClient rpcClient;

			MethodProps(Method method, ObjectMapper objectMapper) {
				this.method = method;

				this.config = Validate.notNull(method.getAnnotation(Rpc.class), "method %s not annotated with @Rpc.",
						method);
				Validate.isTrue(method.getReturnType() == Result.class, "%s must return Result.", method);
				errorType = TypeUtils.getTypeArguments(method.getGenericReturnType(), Result.class)
						.get(Result.class.getTypeParameters()[1]);
				this.jacksonReturnType = objectMapper.getTypeFactory().constructType(method.getGenericReturnType());
				this.argTypes = Stream.of(method.getGenericParameterTypes())
						.map(objectMapper.getTypeFactory()::constructType).toList();
				this.objectMapper = objectMapper;
			}

			RpcClient rpcClient(Connection connection) throws IOException {
				if (rpcClient != null) {
					return rpcClient;
				}

				Channel channel = connection.createChannel();
				declareQueue(channel);

				RpcClientParams params = new RpcClientParams().channel(channel)
						.correlationIdSupplier(() -> UUID.randomUUID().toString()).exchange("")
						.routingKey(config.queue()).replyTo(channel.queueDeclare().getQueue())
						.timeout(config.timeout());

				return rpcClient = new RpcClient(params);
			}

			private void declareQueue(Channel channel) throws IOException {
				Map<String, Object> args = Map.of("x-message-ttl", config.timeout());
				channel.queueDeclareNoWait(config.queue(), true, false, false, args);
			}

			String writeJsonRequest(@NonNull Object[] args) throws JsonProcessingException {
				ObjectNode tree = objectMapper.createObjectNode();
				tree.put("method", method.getName());
				tree.putPOJO("args", args);
				tree.putPOJO("mdc", MdcUtils.getSnapshot());
				return objectMapper.writeValueAsString(tree);
			}

			Object readJsonResponse(String response) throws JsonProcessingException {
				JsonNode tree = objectMapper.readTree(response);
				List<String> fieldNames = IteratorUtils.toList(tree.fieldNames());

				JsonNode mdcJson = Validate.notNull(tree.get("mdc"), "Missing field mdc. Fields: %s", fieldNames);
				MdcSnapshot mdc = objectMapper.treeToValue(mdcJson, MdcSnapshot.class);

				JsonNode argsJson = Validate.notNull(tree.get("result"), "Missing field result. Fields: %s",
						fieldNames);
				Object result = objectMapper.treeToValue(argsJson, jacksonReturnType);

				mdc.apply();

				return result;
			}
		}

		record RpcRequest<I>(MethodProps<I> method, Object[] args, MdcSnapshot mdc) {
		}
	}
}
