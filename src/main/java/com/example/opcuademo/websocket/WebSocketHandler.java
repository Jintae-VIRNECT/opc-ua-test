package com.example.opcuademo.websocket;

import static com.example.opcuademo.websocket.message.LogEventType.*;
import static com.example.opcuademo.websocket.WebSocketLogFormat.*;
import static com.example.opcuademo.websocket.message.WebSocketResponseCode.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.opcuademo.config.redis.RedisPublisher;
import com.example.opcuademo.websocket.message.WebSocketRequestMessage;
import com.example.opcuademo.websocket.message.WebSocketResponseMessage;
import com.example.opcuademo.websocket.session.SessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info(WEB_SOCKET_LOG_FORMAT, CONNECT_EVENT, session.getId(), "New Socket Connect.");
		sessionManager.registerNewSession(session);

		WebSocketResponseMessage connectSuccessMessage = connectSuccessResponse(session.getId());
		String successResponseMessage = messageConvertToText(connectSuccessMessage);

		// Send Register Success Message
		session.sendMessage(new TextMessage(successResponseMessage));
		log.info(WEB_SOCKET_LOG_FORMAT, CONNECT_EVENT, session.getId(), "Connect Success. " + successResponseMessage);
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
		log.debug(WEB_SOCKET_LOG_FORMAT, PONG_EVENT, session.getId(), "Server Received Pong Message.");
		ClientSessionInfo clientSessionInfo = sessionManager.getSessionInformation(session);
		log.debug(
			WEB_SOCKET_LOG_FORMAT, PONG_EVENT, session.getId(),
			"" + clientSessionInfo + " Update Last Pong Time To " + lastPongTime
		);
		clientSessionInfo.setLastPongTime(lastPongTime);
		sessionManager.putSessionInformation(session, clientSessionInfo);
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




}
