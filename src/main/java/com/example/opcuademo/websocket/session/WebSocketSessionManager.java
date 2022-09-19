package com.example.opcuademo.websocket.session;

import static com.example.opcuademo.websocket.WebSocketLogFormat.*;
import static com.example.opcuademo.websocket.message.LogEventType.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.opcuademo.websocket.ClientSessionInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketSessionManager implements SessionManager<ClientSessionInfo> {
	private static final String PING_MESSAGE = "PING";
	private static final long HEART_BEAT_TIME = 5000;
	private static final long HEART_BEAT_DELAY_TIME = 10000;
	private final Map<String, String> userSessionStore = new ConcurrentHashMap<>();
	private final Map<WebSocketSession, ClientSessionInfo> sessionStore = new ConcurrentHashMap<>();

	@Override
	public void registerNewSession(WebSocketSession session) {
		ClientSessionInfo clientSessionInfo = ClientSessionInfo.createFirstConnectInfo();
		sessionStore.put(session, clientSessionInfo);
	}

	@Override
	public ClientSessionInfo getSessionInformation(WebSocketSession session) {
		return sessionStore.get(session);
	}

	@Override
	public void putSessionInformation(
		WebSocketSession session, ClientSessionInfo information
	) {
		sessionStore.put(session, information);
	}

	@Override
	public ClientSessionInfo removeSessionInformation(WebSocketSession session) {
		return sessionStore.remove(session);
	}

	@Override
	public Optional<WebSocketSession> findWebSocketSessionBySessionId(String sessionId) {
		return sessionStore.keySet().stream().filter(session -> session.getId().equals(sessionId)).findFirst();
	}

	@Override
	public String findUserCurrentSessionId(String userSessionKey) {
		return userSessionStore.get(userSessionKey);
	}

	@Override
	public boolean hasExistUserSessionId(String userSessionKey) {
		return userSessionStore.containsKey(userSessionKey);
	}

	@Override
	public String removeUserSessionId(String userSessionKey) {
		return userSessionStore.remove(userSessionKey);
	}

	@Override
	public void putUserSessionId(String userSessionKey, String sessionId) {
		userSessionStore.put(userSessionKey, sessionId);
	}

	@Scheduled(fixedDelay = HEART_BEAT_TIME)
	@Override
	public void websocketConnectionManagement() throws Exception {
		for (Map.Entry<WebSocketSession, ClientSessionInfo> sessionInfoEntry : sessionStore.entrySet()) {
			WebSocketSession session = sessionInfoEntry.getKey();
			ClientSessionInfo clientSessionInformation = sessionInfoEntry.getValue();

			if (clientSessionInformation.getLastPingPongRelayTime() >= HEART_BEAT_DELAY_TIME) {
				log.error("[FORCED_CONNECT_CLOSE][CLIENT][{}] - Not response server PING message. CLIENT_INFO: {}",
					session.getId(), clientSessionInformation.getStatusMessage()
				);
				session.close(CloseStatus.GOING_AWAY);
				return;
			}

			if (!session.isOpen()) {
				log.error(
					"[FORCED_CONNECT_CLOSE][CLIENT][{}] - Socket is not open. CLIENT_INFO: {}",
					session.getId(),
					clientSessionInformation.getStatusMessage()
				);
				session.close(CloseStatus.GOING_AWAY);
				return;
			}

			sendPingMessage(session, clientSessionInformation);
		}

	}

	private void sendPingMessage(
		WebSocketSession session, ClientSessionInfo clientSessionInformation
	) throws IOException {
		try {
			long lastPingTime = System.currentTimeMillis();
			log.debug(WEB_SOCKET_LOG_FORMAT, PING_EVENT, session.getId(), "Send Ping Message");
			log.debug(
				WEB_SOCKET_LOG_FORMAT, PING_EVENT, session.getId(),
				"" + clientSessionInformation + " Update Last Ping Time To " + lastPingTime
			);
			session.sendMessage(new TextMessage(PING_MESSAGE));
			clientSessionInformation.setLastPingTime(System.currentTimeMillis());
			sessionStore.put(session, clientSessionInformation);
		} catch (IOException e) {
			log.error(
				WEB_SOCKET_LOG_FORMAT, PING_EVENT, session.getId(),
				"Send Ping Message Fail. WebSocket Session Will Be Closed."
			);
			session.close();
		}
	}

}
