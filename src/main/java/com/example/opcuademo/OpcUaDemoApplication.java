package com.example.opcuademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.opcuademo.config.RabbitmqProperty;
import com.example.opcuademo.config.properties.OpcUaProperties;

// import com.example.opcuademo.config.RabbitmqProperty;

@SpringBootApplication
@EnableConfigurationProperties({RabbitmqProperty.class, OpcUaProperties.class})
public class OpcUaDemoApplication {

	public static void main(String[] args) throws Exception {

		SpringApplication.run(OpcUaDemoApplication.class, args);
	}

}
