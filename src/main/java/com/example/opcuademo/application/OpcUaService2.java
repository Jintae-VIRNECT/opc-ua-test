package com.example.opcuademo.application;

import static com.example.opcuademo.common.Values.*;
import static java.util.Collections.*;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.stereotype.Service;

import com.example.opcuademo.common.Connect;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpcUaService2 {

	private static final AtomicInteger clientHandles = new AtomicInteger();



	public void startTask() throws UaException, ExecutionException, InterruptedException {


		List<NodeId> nodeId = Arrays.asList(new NodeId(3, "AirConditioner_1.Temperature"));

		CompletableFuture<OpcUaClient> future = Connect.connect().thenCompose(client -> client.getSubscriptionManager().createSubscription(1000.0)
			.thenCompose(subscription -> subscribeTo(
				subscription,
				AttributeId.Value,
				nodeId
			))
			.thenApply(v -> client));

		future.complete(Connect.connectSync());
	}

	private CompletableFuture<UaMonitoredItem> subscribeTo(
		 UaSubscription subscription,
		 AttributeId attributeId,
		 List<NodeId> nodeIds) {

		// subscription request

		 List<MonitoredItemCreateRequest> requests = new ArrayList<>();

		for ( NodeId nodeId : nodeIds) {

			 MonitoringParameters parameters = new MonitoringParameters(
				uint(clientHandles.getAndIncrement()), // must be set
				1_000.0, // sampling interval
				null,
				uint(10),
				true);

			 ReadValueId readValueId = new ReadValueId(
				nodeId,
				attributeId.uid(),
				null,
				null);

			 MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
				readValueId,
				MonitoringMode.Reporting,
				parameters);

			requests.add(request);

		}


		UaSubscription.ItemCreationCallback onItemCreated =
			(item, id) -> item.setValueConsumer(this::onSubscriptionValue);

		return subscription
			.createMonitoredItems(
				TimestampsToReturn.Both,
				requests,
				onItemCreated
			)
			.thenApplyAsync(result -> result.get(0));

	}

	private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
		log.info("item = {}, value = {}",item.getReadValueId().getNodeId(),value.getValue());
	}

	public void stopTask() throws ExecutionException, InterruptedException {

		Connect.connect().get().disconnect();
	}
}
