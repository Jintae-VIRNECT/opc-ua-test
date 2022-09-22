package com.example.opcuademo.websocket;

import static com.example.opcuademo.websocket.WebSocketLogFormat.*;
import static com.example.opcuademo.websocket.message.LogEventType.*;
import static com.example.opcuademo.websocket.message.WebSocketResponseCode.*;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.opcuademo.application.OpcUaService4;
import com.example.opcuademo.common.Connect;
import com.example.opcuademo.common.NodeIds;
import com.example.opcuademo.config.redis.RedisPublisher;
import com.example.opcuademo.websocket.message.OpcUaResponseMessage;
import com.example.opcuademo.websocket.message.WebSocketRequestMessage;
import com.example.opcuademo.websocket.message.WebSocketResponseMessage;
import com.example.opcuademo.websocket.session.SessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.glutamate.lang.Exceptions;
import io.glutamate.str.Tables;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.StatusMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
	private static final ChannelTopic REDIS_CHANNEL = new ChannelTopic("opc-ua/demo");
	private static final String LOGIN_STATUS = "Login";
	private static final String LOGOUT_STATUS = "Logout";
	private static final String PONG_MESSAGE = "PONG";
	private static final long HEART_BEAT_TIME = 5000;
	private final SessionManager<ClientSessionInfo> sessionManager;
	private final ObjectMapper objectMapper;
	private final RedisPublisher redisPublisher;
	private final OpcUaService4 opcuaService4;
	@Value("${opc-ua.host}")
	private String host;
	@Value("${opc-ua.port}")
	private String port;
	private  final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;
	private final AtomicInteger clientHandles = new AtomicInteger();


	private OpcUaClient opcUaClient;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info(WEB_SOCKET_LOG_FORMAT, CONNECT_EVENT, session.getId(), "New Socket Connect.");
		sessionManager.registerNewSession(session);



		WebSocketResponseMessage connectSuccessMessage = connectSuccessResponse(session.getId());
		String successResponseMessage = messageConvertToText(connectSuccessMessage);

		// Send Register Success Message
		session.sendMessage(new TextMessage(successResponseMessage));
		responseOpcUaData(session);

		log.info(WEB_SOCKET_LOG_FORMAT, CONNECT_EVENT, session.getId(), "Connect Success. " + successResponseMessage);
	}

	public void responseOpcUaData(WebSocketSession session) throws UaException, ExecutionException, InterruptedException {

		startTask(session);

	}

	public void startTask(WebSocketSession session) throws UaException, ExecutionException, InterruptedException {


		String endpoint = String.format("opc.tcp://%s:%s", host, port);

		System.out.println("endpoint = " + endpoint);

		opcUaClient = OpcUaClient.create(endpoint);
		opcUaClient.connect().get();

		NodeId nodeId = new NodeId(3, "AirConditioner_1.Temperature");

		Connect.connect().thenCompose(client -> client.getSubscriptionManager().createSubscription(1000.0)
			.thenCompose(subscription -> subscribeTo(
				session,
				subscription,
				AttributeId.Value,
				nodeId
			))	.thenApply(v -> client));


	}

	private CompletableFuture<UaMonitoredItem> subscribeTo(
		WebSocketSession session, UaSubscription subscription,
		AttributeId attributeId,
		NodeId... nodeIds
	) {
		List<MonitoredItemCreateRequest> requests = new ArrayList<>();
		AtomicInteger clientHandles = new AtomicInteger();

		for ( NodeId nodeId : nodeIds) {

			MonitoringParameters parameters = new MonitoringParameters(
				uint(clientHandles.getAndIncrement()), // must be set
				1_000.0, // sampling interval
				null,
				uint(10),
				true);

			ReadValueId readValueId = new ReadValueId(
				nodeId,
				attributeId.uid(),
				null,
				null);

			MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
				readValueId,
				MonitoringMode.Reporting,
				parameters);

			requests.add(request);

		}

		UaMonitoredItem.ValueConsumer consumer = (item, value) -> {
			try {
				dumpValues(
					session,
					Collections.singletonList(item.getReadValueId().getNodeId()),
					Collections.singletonList(value));
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		UaSubscription.ItemCreationCallback onItemCreated = //
			(monitoredItem, id) -> monitoredItem.setValueConsumer(consumer);



		return subscription
			.createMonitoredItems(
				TimestampsToReturn.Both,
				requests,
				onItemCreated
			)
			.thenApply(result -> result.get(0));
	}

	public void dumpValues(
		WebSocketSession session, List<NodeId> nodeIds, List<DataValue> values
	) throws IOException {
		int len = Integer.max(nodeIds.size(), values.size());

		List<List<String>> data = new ArrayList<>(len);

		for (int i = 0; i < Integer.min(nodeIds.size(), values.size()); i++) {

			List<String> row = new ArrayList<>(5);
			data.add(row);
			NodeId nodeId = nodeIds.get(i);

			DataValue value = values.get(i);

			row.add(nodeIds.get(i).toParseableString());
			row.add(toString(value.getValue()));
			row.add(toString(value.getStatusCode()));
			row.add(TIMESTAMP_FORMATTER.format(value.getServerTime().getJavaDate().toInstant()));
			row.add(TIMESTAMP_FORMATTER.format(value.getSourceTime().getJavaDate().toInstant()));

			OpcUaResponseMessage opcUaResponseMessage = new OpcUaResponseMessage();

			opcUaResponseMessage.setValue(value.getValue().getValue().toString());
			opcUaResponseMessage.setNodeId(nodeId.getIdentifier().toString());


			String successResponseMessage = messageConvertToText2(opcUaResponseMessage);

			session.sendMessage(new TextMessage(successResponseMessage));

		}





		Exceptions.wrap(() -> {
			Tables.showTable(System.out,
				Arrays.asList("Node Id", "Value", "State", "Timestamp(Server)", "Timestamp(Source)"),
				data, 2);
		});

	}

	public  String toString(final Variant value) {

		return String.format("%s : %s",
			value.getDataType() // get data type
				.map(id -> NodeIds.lookup(id).orElse(id.toParseableString())) // map to ID or use node id
				.orElse("<unknown>"), // default to "unknown"
			value.getValue());
	}

	public  String toString(final StatusCode statusCode) {
		return StatusCodes
			.lookup(statusCode.getValue()) // lookup
			.map(s -> s[0]) // pick name
			.orElse(statusCode.toString()); // or default to "toString"
	}


	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		// Check Pong Message
		if (message.getPayload().equals(PONG_MESSAGE)) {
			handlerPongMessage(session);
			return;
		}

		// Incoming Text Message Logging
		log.info(WEB_SOCKET_LOG_FORMAT, MESSAGE_HANDLE_EVENT, session.getId(), message.getPayload());

		// Parse Client Request Message
		WebSocketRequestMessage<Object> requestMessage = objectMapper.readValue(
			message.getPayload(), new TypeReference<WebSocketRequestMessage<Object>>() {
			}
		);

		System.out.println("requestMessage = " + requestMessage);

	}

	private void handlerPongMessage(WebSocketSession session) {
		long lastPongTime = System.currentTimeMillis();
		log.info(WEB_SOCKET_LOG_FORMAT, PONG_EVENT, session.getId(), "Server Received Pong Message.");
		ClientSessionInfo clientSessionInfo = sessionManager.getSessionInformation(session);
		log.info(
			WEB_SOCKET_LOG_FORMAT, PONG_EVENT, session.getId(),
			"" + clientSessionInfo + " Update Last Pong Time To " + lastPongTime
		);
		clientSessionInfo.setLastPongTime(lastPongTime);
		sessionManager.putSessionInformation(session, clientSessionInfo);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		log.info(WEB_SOCKET_LOG_FORMAT, CONNECT_CLOSE_EVENT, session.getId(), "Connection Closed. " + status);
		ClientSessionInfo removedClientSessionInfo = sessionManager.removeSessionInformation(session);

		opcUaClient.disconnect();

		if (removedClientSessionInfo == null) {
			log.error(
				WEB_SOCKET_LOG_FORMAT, CONNECT_CLOSE_EVENT, session.getId(),
				"Connection Closed. Client Information Not Found"
			);
			return;
		}

		log.info(
			WEB_SOCKET_LOG_FORMAT, CONNECT_CLOSE_EVENT, session.getId(),
			"Closed Client Session Info " + removedClientSessionInfo
		);

		StatusMessage statusMessage = removedClientSessionInfo.getStatusMessage();

		// if not send user information, do nothing
		if (statusMessage == null || statusMessage.getStatus() == null) {
			log.error(
				WEB_SOCKET_LOG_FORMAT, CONNECT_CLOSE_EVENT, session.getId(),
				"Connection Closed. Client Status Information Not Found"
			);
			return;
		}

		statusMessage.setStatus(LOGOUT_STATUS);
		log.info(
			WEB_SOCKET_LOG_FORMAT, CONNECT_CLOSE_EVENT, session.getId(), "Publish Logout Status Event. " + statusMessage
		);
		redisPublisher.publish(REDIS_CHANNEL, statusMessage, session.getId());
		String removedSessionId = sessionManager.removeUserSessionId(statusMessage.getUserAccessStatusKey());
		log.info(
			WEB_SOCKET_LOG_FORMAT, CONNECT_CLOSE_EVENT, session.getId(),
			"Current User Session Information Deleted. Removed Session Id Is [" + removedSessionId + "]"
		);
		log.info(
			WEB_SOCKET_LOG_FORMAT, CONNECT_CLOSE_EVENT, session.getId(),
			"Current Session Id [" + session.getId() + "] and Removed Session Id [" + removedSessionId + "] Is Equal ["
				+ session.getId().equals(removedSessionId) + "]"
		);
	}

	private WebSocketResponseMessage connectSuccessResponse(final String sessionId) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("sessionId", sessionId);
		payload.put("heartbeat", HEART_BEAT_TIME);

		WebSocketResponseMessage message = WebSocketResponseMessage.of(CLIENT_CONNECT_SUCCESS);
		message.setData(payload);
		return message;
	}

	private String messageConvertToText(WebSocketResponseMessage webSocketResponseMessage) throws
		JsonProcessingException {
		return objectMapper.writeValueAsString(webSocketResponseMessage);
	}

	private String messageConvertToText2(OpcUaResponseMessage webSocketResponseMessage) throws
		JsonProcessingException {
		return objectMapper.writeValueAsString(webSocketResponseMessage);
	}





}
