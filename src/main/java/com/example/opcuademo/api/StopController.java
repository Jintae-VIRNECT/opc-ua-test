package com.example.opcuademo.api;



import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.stack.core.UaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.opcuademo.application.StopService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StopController {

	private final StopService stopService;


	@GetMapping("/stop")
	public ResponseEntity<String> stop(
	) throws UaException, ExecutionException, InterruptedException {

		stopService.stopTask();

		return ResponseEntity.ok("ok");
	}

	@GetMapping("/disconnect")
	public ResponseEntity<String> disconnect(
	) throws UaException, ExecutionException, InterruptedException {

		stopService.disconnect();

		return ResponseEntity.ok("ok");
	}



	@GetMapping("/clear")
	public ResponseEntity<String> 	poolClear(
	)  {

		stopService.poolClear();

		return ResponseEntity.ok("ok");
	}
}
