package com.example.opcuademo.websocket.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketRequestMessage<T> {
	private T data;
	private Command command;

	@Override
	public String toString() {
		return "{" +
			"data=" + data +
			", command='" + command + '\'' +
			'}';
	}
}
