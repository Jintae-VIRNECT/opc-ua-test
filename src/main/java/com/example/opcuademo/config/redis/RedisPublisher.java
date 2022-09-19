package com.example.opcuademo.config.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.StatusMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {
	private final RedisTemplate<String, Object> redisTemplateForObject;

	public void publish(ChannelTopic channelTopic, StatusMessage message, String sessionId) {
		redisTemplateForObject.convertAndSend(channelTopic.getTopic(), message);
		log.info("[REDIS_PUBLISH][{}][{}] - {}", sessionId, channelTopic.getTopic(), message);
	}
}
