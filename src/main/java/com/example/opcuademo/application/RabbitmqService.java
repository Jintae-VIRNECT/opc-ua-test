// package com.example.opcuademo.application;
//
// import org.springframework.amqp.core.ExchangeTypes;
// import org.springframework.amqp.rabbit.annotation.Argument;
// import org.springframework.amqp.rabbit.annotation.Exchange;
// import org.springframework.amqp.rabbit.annotation.Queue;
// import org.springframework.amqp.rabbit.annotation.QueueBinding;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.stereotype.Service;
//
// import com.example.opcuademo.config.RabbitmqProperty;
//
// import lombok.AccessLevel;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @Service
// @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
// public class RabbitmqService {
//
//
// 	@RabbitListener(bindings = @QueueBinding(
// 		value = @Queue(arguments = {@Argument(name = "x-dead-letter-exchange", value = "dlx"),
// 			@Argument(name = "x-dead-letter-routing-key", value = "dlx.push")}),
// 		exchange = @Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC),
// 		key = "demo.opc"
// 	), containerFactory = "rabbitListenerContainerFactory")
// 	public void getAllPushMessage2(String value) {
// 		log.info("value ==== {}",value);
// 	}
// }
