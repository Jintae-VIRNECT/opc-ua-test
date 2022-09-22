package com.example.opcuademo.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties(prefix="opc-ua")
@Getter
@Setter
@ToString
@Configuration
public class OpcUaProperties {

	private String host;
	private String port;

}
