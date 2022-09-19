package com.example.opcuademo.websocket.message;

public class ResponseMessage {
	private String sessionId;
	private String message;
	private int code;

	public ResponseMessage(String sessionId, String message, int code) {
		this.sessionId = sessionId;
		this.message = message;
		this.code = code;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "ResponseMessage{" +
			"sessionId='" + sessionId + '\'' +
			", message='" + message + '\'' +
			", code=" + code +
			'}';
	}
}
