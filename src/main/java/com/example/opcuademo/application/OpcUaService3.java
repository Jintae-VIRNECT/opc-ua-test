package com.example.opcuademo.application;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.springframework.stereotype.Service;

import com.example.opcuademo.common.Connect;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpcUaService3 {


	public static CompletableFuture<StatusCode> write(
		final OpcUaClient client,
		final NodeId nodeId,
		final Object value) {

		return client.writeValue(nodeId, new DataValue(new Variant(value)));
	}

	public void startTask() throws ExecutionException, InterruptedException {
		NodeId nodeId = new NodeId(3, "AirConditioner_1.Temperature");

		final boolean value = true;

		// first example

		Connect.connect()

			.thenCompose(client -> {

				return write(
					client,
					nodeId,
					value // value to write
				)

					.whenComplete((result, error) -> {
						if (error == null) {
							System.out.format("Result: %s%n", result);
						} else {
							System.out.println(" = " );
							error.printStackTrace();
						}
					})

					.thenCompose(v -> client.disconnect());
			})

			.get(); // wait for everything to complete
	}
}
