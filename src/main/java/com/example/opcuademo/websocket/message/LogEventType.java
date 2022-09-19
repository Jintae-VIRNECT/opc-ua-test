package com.example.opcuademo.websocket.message;

public enum LogEventType {
	CONNECT_EVENT("NEW_CONNECT"),
	REGISTER_EVENT("REGISTER"),
	REGISTER_DUPLICATE_EVENT("REGISTER_DUPLICATE"),
	ACCESS_STATUS_EVENT("ACCESS_STATUS"),
	CONNECT_CLOSE_EVENT("CONNECTION_CLOSE"),
	REMOTE_EXIT_EVENT("REMOTE_EXIT"),
	FORCE_LOGOUT_EVENT("FORCE_LOGOUT"),
	WORKSPACE_UPDATE("WORKSPACE_UPDATE"),
	PING_EVENT("PING"),
	PONG_EVENT("PONG"),
	MESSAGE_HANDLE_EVENT("MESSAGE_HANDLE");

	private String event;

	LogEventType(String event) {
		this.event = event;
	}

	public String getEvent() {
		return event;
	}

	@Override
	public String toString() {
		return event;
	}
}
