package com.example.opcuademo.websocket.session;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.opcuademo.websocket.ClientSessionInfo;
import com.example.opcuademo.websocket.message.WebSocketResponseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSessionMessageSubscriber implements MessageListener {
	private final SessionManager<ClientSessionInfo> sessionManager;
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {


		String messages = redisTemplate.getStringSerializer().deserialize(message.getBody());

		System.out.println("messages = " + messages);

	}

	private String messageConvertToText(WebSocketResponseMessage webSocketResponseMessage) throws
		JsonProcessingException {
		return objectMapper.writeValueAsString(webSocketResponseMessage);
	}

}
