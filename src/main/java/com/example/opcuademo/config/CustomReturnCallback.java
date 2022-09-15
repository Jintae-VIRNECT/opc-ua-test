package com.example.opcuademo.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.rabbitmq.client.AMQP;

import lombok.extern.slf4j.Slf4j;

/**
 * Project: PF-Message
 * DATE: 2021-03-23
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
public class CustomReturnCallback implements RabbitTemplate.ReturnCallback {
	@Override
	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
		if (replyCode == AMQP.NO_ROUTE) {
			log.error(
				"Message No Routing Queue. message : [{}], replyCode : [{}], replyText : [{}], exchange : [{}], routingKey : [{}]",
				message, replyCode, replyText, exchange, routingKey
			);
		} else {
			log.warn(
				"Message Return Callback. message : [{}], replyCode : [{}], replyText : [{}], exchange : [{}], routingKey : [{}]",
				message, replyCode, replyText, exchange, routingKey
			);
		}
	}

}
