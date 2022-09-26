package com.example.opcuademo.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.milo.opcua.stack.core.UaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.opcuademo.application.OpcUaService;
import com.example.opcuademo.application.OpcUaService2;
import com.example.opcuademo.application.OpcUaService3;
import com.example.opcuademo.application.OpcUaService4;
import com.example.opcuademo.application.OpcUaService5;
import com.example.opcuademo.config.properties.OpcUaProperties;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OpcUaController {

	private final OpcUaProperties opcUaProperties;
	private final OpcUaService opcUaService;
	private final OpcUaService2 opcUaService2;
	private final OpcUaService3 opcUaService3;
	private final OpcUaService4 opcUaService4;
	private final OpcUaService5 opcUaService5;

	@GetMapping("/test")
	public ResponseEntity<String> test1(
		HttpServletResponse response
	) throws UaException, ExecutionException, InterruptedException, IOException {
		String redirect_uri="http://www.google.com";
		System.out.println("opcUaProperties.getHost() = " + opcUaProperties.getHost());
		response.sendRedirect(redirect_uri);

		return ResponseEntity.ok("ok");
	}

	@GetMapping("/test2")
	public ResponseEntity<String> test2(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService2.startTask();

		return ResponseEntity.ok("ok");
	}

	@GetMapping("/test3")
	public ResponseEntity<String> test3(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService3.startTask();

		return ResponseEntity.ok("ok");
	}

	@GetMapping("/test4")
	public ResponseEntity<String> test4(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService4.startTask();

		return ResponseEntity.ok("ok");
	}

	@GetMapping("/stop")
	public ResponseEntity<String> test5(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService2.stopTask();

		return ResponseEntity.ok("ok");
	}

	@GetMapping("/test5")
	public ResponseEntity<String> test6(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService5.startTask();

		return ResponseEntity.ok("ok");
	}

	@GetMapping("/test6")
	public ResponseEntity<String> test7(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService4.testRabbitMq();

		return ResponseEntity.ok("ok");
	}

}
