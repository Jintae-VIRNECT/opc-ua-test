package com.example.opcuademo.application;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.opcuademo.common.Connect;
import com.example.opcuademo.common.NodeIds;
import com.example.opcuademo.websocket.session.SessionManager;

import io.glutamate.lang.Exceptions;
import io.glutamate.str.Tables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class OpcUaService4 {

	@Value("${opc-ua.host}")
	private String host;
	@Value("${opc-ua.port}")
	private String port;
	private  final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;
	private final AtomicInteger clientHandles = new AtomicInteger();

	public void startTask() throws UaException, ExecutionException, InterruptedException {

		System.out.println("host = " + host);
		System.out.println("port = " + port);


		NodeId nodeId = new NodeId(3, "AirConditioner_1.Temperature");

		Connect.connect()
			.thenCompose(client -> client.getSubscriptionManager().createSubscription(1000.0)
			.thenCompose(subscription -> subscribeTo(
				subscription,
				AttributeId.Value,
				nodeId
			))	.thenApply(v -> client));


	}

	private  CompletableFuture<UaMonitoredItem> subscribeTo(
		UaSubscription subscription,
		AttributeId attributeId,
		NodeId... nodeIds) {
		List<MonitoredItemCreateRequest> requests = new ArrayList<>();
		AtomicInteger clientHandles = new AtomicInteger();

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

		UaMonitoredItem.ValueConsumer consumer = (item, value) ->  dumpValues(
			Collections.singletonList(item.getReadValueId().getNodeId()),
			Collections.singletonList(value));

		UaSubscription.ItemCreationCallback onItemCreated = //
			(monitoredItem, id) -> monitoredItem.setValueConsumer(consumer);



		return subscription
			.createMonitoredItems(
				TimestampsToReturn.Both,
				requests,
				onItemCreated
			)
			.thenApply(result -> result.get(0));
	}

	public void dumpValues( List<NodeId> nodeIds,  List<DataValue> values) {
		int len = Integer.max(nodeIds.size(), values.size());

		List<List<String>> data = new ArrayList<>(len);

		for (int i = 0; i < Integer.min(nodeIds.size(), values.size()); i++) {

			List<String> row = new ArrayList<>(5);
			data.add(row);

			DataValue value = values.get(i);

			row.add(nodeIds.get(i).toParseableString());
			row.add(toString(value.getValue()));
			row.add(toString(value.getStatusCode()));
			row.add(TIMESTAMP_FORMATTER.format(value.getServerTime().getJavaDate().toInstant()));
			row.add(TIMESTAMP_FORMATTER.format(value.getSourceTime().getJavaDate().toInstant()));

			// rabbitTemplate.convertAndSend("amq.topic", "demo.opc", value.getValue().getValue().toString());
		}



		Exceptions.wrap(() -> {
		Tables.showTable(System.out,
			Arrays.asList("Node Id", "Value", "State", "Timestamp(Server)", "Timestamp(Source)"),
			data, 2);
	});

	}

	public  String toString(final Variant value) {

		return String.format("%s : %s",
			value.getDataType() // get data type
				.map(id -> NodeIds.lookup(id).orElse(id.toParseableString())) // map to ID or use node id
				.orElse("<unknown>"), // default to "unknown"
			value.getValue());
	}

	public  String toString(final StatusCode statusCode) {
		return StatusCodes
			.lookup(statusCode.getValue()) // lookup
			.map(s -> s[0]) // pick name
			.orElse(statusCode.toString()); // or default to "toString"
	}

	public void testRabbitMq() {

		// rabbitTemplate.convertAndSend("amq.topic", "demo.opc", "zzzz");
	}
}
