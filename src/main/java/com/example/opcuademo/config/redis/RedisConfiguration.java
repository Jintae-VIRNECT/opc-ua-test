package com.example.opcuademo.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.opcuademo.websocket.message.CustomErrorHandler;
import com.example.opcuademo.websocket.session.RedisSessionMessageSubscriber;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
@EnableRedisRepositories
public class RedisConfiguration {
	private static final String NOTIFICATION = "notification";
	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		log.info("REDIS HOST: {}", this.host);
		log.info("REDIS PORT: {}", this.port);
		return new LettuceConnectionFactory(host, port);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplateForObject(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		return redisTemplate;
	}

	@Bean
	public MessageListenerAdapter messageListenerAdapter(RedisSessionMessageSubscriber redisSessionMessageSubscriber) {
		return new MessageListenerAdapter(redisSessionMessageSubscriber);
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		MessageListenerAdapter messageListenerAdapter, RedisConnectionFactory redisConnectionFactory
	) {
		RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
		redisMessageListenerContainer.addMessageListener(
			messageListenerAdapter, new ChannelTopic(NOTIFICATION));
		redisMessageListenerContainer.setErrorHandler(new CustomErrorHandler());
		return redisMessageListenerContainer;
	}
}
