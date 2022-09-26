package com.example.opcuademo.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ConfigurationProperties(prefix="opc-ua")
@Getter
@ToString
@ConstructorBinding
@AllArgsConstructor
public class OpcUaProperties {
	private String host;
	private String port;
	private String poolSize;
}
