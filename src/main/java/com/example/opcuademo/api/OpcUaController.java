package com.example.opcuademo.api;

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.stack.core.UaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.opcuademo.application.OpcUaService;
import com.example.opcuademo.application.OpcUaService2;
import com.example.opcuademo.application.OpcUaService3;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OpcUaController {

	private final OpcUaService opcUaService;
	private final OpcUaService2 opcUaService2;
	private final OpcUaService3 opcUaService3;

	@GetMapping("/test")
	public ResponseEntity<String> test1(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService.startTask();

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

	@GetMapping("/stop")
	public ResponseEntity<String> test4(
	) throws UaException, ExecutionException, InterruptedException {

		opcUaService2.stopTask();

		return ResponseEntity.ok("ok");
	}

}
