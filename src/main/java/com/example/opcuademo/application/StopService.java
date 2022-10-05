package com.example.opcuademo.application;

import org.springframework.stereotype.Service;

import com.example.opcuademo.common.OpcUaClientConnectionPool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StopService {

	private final OpcUaClientConnectionPool opcUaClientConnectionPool;


	public void stopTask(){

		opcUaClientConnectionPool.stopSubscription();

	}

	public void disconnect(){

		opcUaClientConnectionPool.disconnect();

		log.info("disconnect client" );
	}

	public void poolClear()  {

		opcUaClientConnectionPool.shutdown();

	}
}
