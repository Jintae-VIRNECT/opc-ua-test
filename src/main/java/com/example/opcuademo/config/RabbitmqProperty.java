package com.example.opcuademo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;

/**
 * Project: PF-Message
 * DATE: 2021-03-23
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Getter
public class RabbitmqProperty {
    private final String host;
    private final String username;
    private final String password;
    private final int port;

    public RabbitmqProperty(String host, String username, String password, int port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    @Override
    public String toString() {
        return "RabbitmqProperty{" +
                "host='" + host + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
