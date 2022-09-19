package com.example.opcuademo.websocket.session;

import java.util.Optional;

import org.springframework.web.socket.WebSocketSession;

public interface SessionManager<T> {
	/**
	 * Register new websocket session in store
	 * @param session - Websocket session
	 */
	void registerNewSession(WebSocketSession session);

	/**
	 * find additional information by websocket session
	 * @param session - session information for search additional session information
	 * @return - additional session information
	 */
	T getSessionInformation(WebSocketSession session);

	/**
	 * Put new additional session information with websocket session
	 * @param session
	 * @param information
	 */
	void putSessionInformation(WebSocketSession session, T information);

	/**
	 * Remove additional session information by websocket session
	 * @param session - websocket session
	 * @return - additional information which deleted by websocket session in session manager
	 */
	T removeSessionInformation(WebSocketSession session);

	Optional<WebSocketSession> findWebSocketSessionBySessionId(String sessionId);


	void websocketConnectionManagement() throws Exception;

	
	// Todo: 나중에 리팩토링으로 분리하자, 메서드 이름도 수정
	/*
	* User Session History Handle
	* */

	String findUserCurrentSessionId(String userSessionKey);

	boolean hasExistUserSessionId(String userSessionKey);

	String removeUserSessionId(String userSessionKey);

	void putUserSessionId(String userSessionKey, String sessionId);
}
