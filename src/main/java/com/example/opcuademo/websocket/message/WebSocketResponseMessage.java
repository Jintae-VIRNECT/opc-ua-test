package com.example.opcuademo.websocket.message;

import java.util.Map;

public class WebSocketResponseMessage {
	private final String command;
	private final int code;
	private final String message;
	private Map<String, Object> data;

	public WebSocketResponseMessage(String command, String message, int code) {
		this.command = command;
		this.message = message;
		this.code = code;
	}

	public static WebSocketResponseMessage of(WebSocketResponseCode responseCode) {
		return new WebSocketResponseMessage(
			responseCode.getCommand(), responseCode.getMessage(), responseCode.getCode()
		);
	}

	public String getCommand() {
		return command;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "{" +
			"command='" + command + '\'' +
			", data=" + data +
			", code=" + code +
			", message='" + message + '\'' +
			'}';
	}
}
