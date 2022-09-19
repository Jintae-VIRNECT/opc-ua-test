package com.example.opcuademo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfiguration {

	@Bean
	public ConcurrentTaskScheduler concurrentTaskScheduler() {
		return new ConcurrentTaskScheduler();
	}
}
