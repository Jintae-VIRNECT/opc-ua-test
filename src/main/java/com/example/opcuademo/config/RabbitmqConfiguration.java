// package com.example.opcuademo.config;
//
// import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
// import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// import lombok.RequiredArgsConstructor;
//
// @Configuration
// @RequiredArgsConstructor
// public class RabbitmqConfiguration {
// 	private final RabbitmqProperty rabbitmqProperty;
//
// 	@Bean
// 	public CachingConnectionFactory cachingConnectionFactory() {
// 		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
// 		connectionFactory.setPort(rabbitmqProperty.getPort());
// 		connectionFactory.setUsername(rabbitmqProperty.getUsername());
// 		connectionFactory.setPassword(rabbitmqProperty.getPassword());
// 		connectionFactory.setHost(rabbitmqProperty.getHost());
// 		connectionFactory.setPublisherReturns(true);
// 		return connectionFactory;
// 	}
//
// 	@Bean
// 	public RabbitTemplate rabbitTemplate() {
// 		RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory());
// 		rabbitTemplate.setMessageConverter(messageConverter());
// 		rabbitTemplate.setMandatory(true);
// 		rabbitTemplate.setReturnCallback(new CustomReturnCallback());
// 		return rabbitTemplate;
// 	}
//
// 	private Jackson2JsonMessageConverter messageConverter() {
// 		return new Jackson2JsonMessageConverter();
// 	}
//
// 	@Bean
// 	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() throws Exception {
//
// 		if (rabbitmqProperty.getPort() == 0) {
// 			throw new Exception("The config server does not exist. Restart the message server.");
// 		}
//
// 		final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
// 		factory.setConnectionFactory(cachingConnectionFactory());
// 		factory.setMessageConverter(messageConverter());
// 		return factory;
// 	}
// }
