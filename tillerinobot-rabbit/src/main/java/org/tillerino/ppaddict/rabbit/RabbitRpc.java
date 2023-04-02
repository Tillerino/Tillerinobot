package org.tillerino.ppaddict.rabbit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.MDC;
import org.tillerino.ppaddict.util.Result;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcSnapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.RpcClientParams;
import com.rabbitmq.client.RpcServer;
import com.rabbitmq.client.AMQP.BasicProperties;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for setting up RPCs through RabbitMQ, see also {@link Rpc}.
 */
@Slf4j
public class RabbitRpc {
	/**
	 * Implements a dynamic proxy which performs RPCs to implement the interfaces methods.
	 *
	 * @param <I> interface must have a single, {@link Rpc}-annotated method.
	 * @return this instance can be used concurrently by multiple threads
	 */
	public static <I> I remoteCallProxy(Connection connection, Class<I> cls, @NonNull Object timeoutResponse) {
		MethodProps<I> methodProps = new MethodProps<>(cls, RabbitMqConfiguration.mapper());
		Validate.isTrue(TypeUtils.isAssignable(timeoutResponse.getClass(), methodProps.errorType), "Timeout response must be of type %s but was %s", methodProps.errorType, timeoutResponse.getClass());
		Result<Object, Object> timeoutResult = Result.err(timeoutResponse);

		Object p = Proxy.newProxyInstance(cls.getClassLoader(), new Class[] { cls }, (proxy, method, args) -> {
			RpcClient rpcClient = methodProps.rpcClient(connection);
			String response;
			try {
				response = rpcClient.stringCall(methodProps.writeJsonRequest(args));
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
	 * @param errorResponse response in case of an error. This must be of the error type returned by the {@link Rpc}-annotated method.
	 */
	public static <I> RpcServer handleRemoteCalls(Connection connection, Class<I> cls, I handler, @NonNull Object errorResponse) throws IOException {
		MethodProps<I> methodProps = new MethodProps<>(cls, RabbitMqConfiguration.mapper());
		TypeUtils.isAssignable(errorResponse.getClass(), methodProps.errorType);

		return methodProps.rpcServer(connection, handler, errorResponse);
	}

	static class MethodProps<I> {
		private final ObjectMapper objectMapper;
		private final Rpc config;
		private final JavaType jacksonReturnType;
		private final Type errorType;
		private final List<JavaType> argTypes;
		private final Method method;

		RpcClient rpcClient;


		MethodProps(Class<I> cls, ObjectMapper objectMapper) {
			Validate.isTrue(cls.isInterface(), "%s must be an interface", cls);
			Validate.isTrue(cls.getDeclaredMethods().length == 1, "%s must have exactly one declared method", cls);
			this.method = cls.getDeclaredMethods()[0];

			this.config = Validate.notNull(method.getAnnotation(Rpc.class), "method %s not annotated with RPC", method);
			if (method.getReturnType() != Result.class) {
				throw new ContextedRuntimeException("Return type must be Result")
					.addContextValue("method", method);
			}
			errorType = TypeUtils.getTypeArguments(method.getGenericReturnType(), Result.class).get(Result.class.getTypeParameters()[1]);
			this.jacksonReturnType = objectMapper.getTypeFactory().constructType(method.getGenericReturnType());
			this.argTypes = Stream.of(method.getGenericParameterTypes()).map(objectMapper.getTypeFactory()::constructType).toList();
			this.objectMapper = objectMapper;
		}

		RpcClient rpcClient(Connection connection) throws IOException {
			if (rpcClient != null) {
				return rpcClient;
			}

			Channel channel = connection.createChannel();
			declareQueue(channel);

			RpcClientParams params = new RpcClientParams()
					.channel(channel)
					.correlationIdSupplier(() -> UUID.randomUUID().toString())
					.exchange("")
					.routingKey(config.queue())
					.replyTo(channel.queueDeclare().getQueue())
					.timeout(config.timeout());

			return rpcClient = new RpcClient(params);
		}

		private void declareQueue(Channel channel) throws IOException {
			Map<String, Object> args = Map.of("x-message-ttl", config.timeout());
			channel.queueDeclareNoWait(config.queue(), true, false, false, args);
		}

		RpcServer rpcServer(Connection connection, I handler, Object errorResponse) throws IOException {
			Channel channel = connection.createChannel();
			channel.basicQos(1); // for proper parallelization (if needed)

			declareQueue(channel);

			// serialize immediately so that we can always return something
			byte[] errorBytes = writeJsonResponse(Result.err(errorResponse), MdcSnapshot.create(Map.of()));
			return new RpcServer(channel, config.queue()) {
				@Override
				public byte[] handleCall(byte[] requestBody, BasicProperties replyProperties) {
					try {
						MDC.clear();
						return writeJsonResponse(method.invoke(handler, readJsonRequest(requestBody)), MdcUtils.getSnapshot());
					} catch (Exception e) {
						log.error("error handling RPC with correlation id {}", replyProperties.getCorrelationId(), e);
						return errorBytes;
					} finally {
						MDC.clear();
					}
				}
			};
		}

		String writeJsonRequest(Object[] args) throws JsonProcessingException {
			ObjectNode tree = objectMapper.createObjectNode();
			tree.putPOJO("args", args);
			tree.putPOJO("mdc", MdcUtils.getSnapshot());
			return objectMapper.writeValueAsString(tree);
		}

		Object[] readJsonRequest(byte[] response) throws IOException {
			JsonNode tree = objectMapper.readTree(response);
			List<String> fieldNames = IteratorUtils.toList(tree.fieldNames());
			JsonNode mdcJson = Validate.notNull(tree.get("mdc"), "Missing field mdc. Fields: %s", fieldNames);
			MdcSnapshot mdc = objectMapper.treeToValue(mdcJson, MdcSnapshot.class);

			JsonNode argsJson = Validate.notNull(tree.get("args"), "Missing field args. Fields: %s", fieldNames);
			if (!argsJson.isArray()) {
				throw new IllegalStateException();
			}
			if (argsJson.size() != argTypes.size()) {
				throw new IllegalStateException();
			}
			Object[] args = new Object[argTypes.size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = objectMapper.treeToValue(argsJson.get(i), argTypes.get(i));
			}

			mdc.apply();
			return args;
		}

		byte[] writeJsonResponse(Object result, MdcSnapshot mdc) throws JsonProcessingException {
			ObjectNode tree = objectMapper.createObjectNode();
			tree.putPOJO("result", result);
			tree.putPOJO("mdc", mdc);
			return objectMapper.writeValueAsBytes(tree);
		}

		Object readJsonResponse(String response) throws JsonProcessingException {
			JsonNode tree = objectMapper.readTree(response);
			List<String> fieldNames = IteratorUtils.toList(tree.fieldNames());

			JsonNode mdcJson = Validate.notNull(tree.get("mdc"), "Missing field mdc. Fields: %s", fieldNames);
			MdcSnapshot mdc = objectMapper.treeToValue(mdcJson, MdcSnapshot.class);

			JsonNode argsJson = Validate.notNull(tree.get("result"), "Missing field result. Fields: %s", fieldNames);
			Object result = objectMapper.treeToValue(argsJson, jacksonReturnType);

			mdc.apply();

			return result;
		}
	}
}
