package com.example.opcuademo.websocket.message;

import org.springframework.util.ErrorHandler;

public class CustomErrorHandler implements ErrorHandler {
	@Override
	public void handleError(Throwable t) {
		System.out.println("t = " + t);
	}
}
