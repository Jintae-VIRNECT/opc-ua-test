package com.example.opcuademo.SuscriptionTest.common;

import static com.google.common.collect.Lists.*;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.opcuademo.common.NodeIds;
import com.example.opcuademo.common.Values;

import io.glutamate.lang.Exceptions;
import io.glutamate.str.Tables;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)

public class OpcUaClientTest {

	@Value("${opc-ua.host}")
	private String host;
	@Value("${opc-ua.port}")
	private String port;

	private OpcUaClient client;

	private  final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;
	private final AtomicInteger clientHandles = new AtomicInteger();



	@BeforeEach
	public void create() throws Exception {

		String endpoint = String.format("opc.tcp://%s:%s", host, port);

		client = OpcUaClient.create(endpoint);
		client.connect().get();
	}

	@Test
	public void subscription() throws Exception {

		CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

		UaSubscription subscription= client.getSubscriptionManager()
			.createSubscription(1000.0).get();

		NodeId nodeId = new NodeId(3, "AirConditioner_1.Temperature");

		AtomicInteger clientHandles = new AtomicInteger();

		MonitoringParameters parameters = new MonitoringParameters(
			uint(clientHandles.getAndIncrement()), // must be set
			1_000.0, // sampling interval
			null,
			uint(10),
			true);

		ReadValueId readValueId = new ReadValueId(
			nodeId,
			AttributeId.Value.uid(),
			null,
			null);

		MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
			readValueId,
			MonitoringMode.Reporting,
			parameters);

		UaSubscription.ItemCreationCallback onItemCreated =
			(item, id) -> item.setValueConsumer(this::onSubscriptionValue);

		List<UaMonitoredItem> items = subscription.createMonitoredItems(
			TimestampsToReturn.Both,
			newArrayList(request),
			onItemCreated
		).get();

		for (UaMonitoredItem item : items) {
			if (item.getStatusCode().isGood()) {
				System.out.println("item created for nodeId={}"+ item.getReadValueId().getNodeId());
			} else {
				System.out.println(
					"failed to create item for nodeId= " + item.getReadValueId().getNodeId()+" (status={})"+
						item.getStatusCode());
			}
		}

		Thread.sleep(5000);
		future.complete(client);

	}

	@Test
	public void completableFutureSubscription(){
		NodeId nodeId = new NodeId(3, "AirConditioner_1.Temperature");

		client.getSubscriptionManager().createSubscription(1000.0)
			.thenCompose(subscription -> subscribeTo(
				subscription,
				AttributeId.Value,
				nodeId
			)).thenApply(v->client);
	}

	private  CompletableFuture<UaMonitoredItem> subscribeTo(
		final UaSubscription subscription,
		final AttributeId attributeId,
		final NodeId... nodeIds) {
		final List<MonitoredItemCreateRequest> requests = new ArrayList<>();
		final AtomicInteger clientHandles = new AtomicInteger();

		for (final NodeId nodeId : nodeIds) {

			final MonitoringParameters parameters = new MonitoringParameters(
				uint(clientHandles.getAndIncrement()), // must be set
				1_000.0, // sampling interval
				null,
				uint(10),
				true);

			final ReadValueId readValueId = new ReadValueId(
				nodeId,
				attributeId.uid(),
				null,
				null);

			final MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
				readValueId,
				MonitoringMode.Reporting,
				parameters);

			requests.add(request);

		}
		BiConsumer<UaMonitoredItem,DataValue> consumer = //
			(item, value) -> dumpValues(
				System.out,
				Collections.singletonList(item.getReadValueId().getNodeId()),
				Collections.singletonList(value));



		BiConsumer<UaMonitoredItem, Integer> onItemCreated = //
			(monitoredItem, id) -> monitoredItem.setValueConsumer(consumer);

		return subscription
			.createMonitoredItems(
				TimestampsToReturn.Both,
				requests,
				onItemCreated
			)
			.thenApply(result -> result.get(0));
	}

	private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
		System.out.println("item = " + item.getReadValueId().getNodeId());
		System.out.println("value = " + value.getValue());
	}

	public void dumpValues(final PrintStream out, final List<NodeId> nodeIds, final List<DataValue> values) {
		final int len = Integer.max(nodeIds.size(), values.size());

		final List<List<String>> data = new ArrayList<>(len);

		for (int i = 0; i < Integer.min(nodeIds.size(), values.size()); i++) {

			final List<String> row = new ArrayList<>(5);
			data.add(row);

			final DataValue value = values.get(i);

			row.add(nodeIds.get(i).toParseableString());
			row.add(toString(value.getValue()));
			row.add(toString(value.getStatusCode()));
			row.add(TIMESTAMP_FORMATTER.format(value.getServerTime().getJavaDate().toInstant()));
			row.add(TIMESTAMP_FORMATTER.format(value.getSourceTime().getJavaDate().toInstant()));
		}

		Exceptions.wrap(() -> {
			Tables.showTable(out,
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
}
