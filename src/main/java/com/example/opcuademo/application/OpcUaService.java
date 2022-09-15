package com.example.opcuademo.application;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.springframework.stereotype.Service;

import com.example.opcuademo.common.Connect;
import com.example.opcuademo.common.Values;

import lombok.extern.slf4j.Slf4j;
import static com.example.opcuademo.common.Connect.*;
import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static java.util.Collections.singletonList;
import static org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn.Both;


@Service
@Slf4j
public class OpcUaService {

	public void startTask() throws UaException, ExecutionException, InterruptedException {
		 NodeId nodeId = new NodeId(3, "AirConditioner_1.Temperature");

		OpcUaClient client = Connect.connect().get();

		final NodeId[] moreIds = new NodeId[] {
			Identifiers.Server_ServerStatus_BuildInfo_ManufacturerName,
			Identifiers.Server_ServerStatus_BuildInfo_ProductName,
			Identifiers.Server_ServerStatus_CurrentTime,
			nodeId
		};
		CompletableFuture<List<DataValue>> future = read(
			client,
			AttributeId.Value,
			moreIds);


		List<DataValue> values = future.get();
		Values.dumpValues(System.out, asList(moreIds), values);

		CompletableFuture<List<DataValue>> future2 = read(
			client,
			AttributeId.BrowseName,
			moreIds);

		List<DataValue> values2 = future2.get();

		Variant value = values.get(0).getValue();


		Values.dumpValues(System.out, asList(moreIds), values2);

	}

	public CompletableFuture<DataValue> read(
		final OpcUaClient client,
		final NodeId nodeId) {

		return client.readValue(0, TimestampsToReturn.Both, nodeId);
	}

	public CompletableFuture<List<DataValue>> read(
		final OpcUaClient client,
		final AttributeId attributeId,
		final NodeId... nodeIds) {

		return client
			.read(
				0,
				Both,
				asList(nodeIds),
				nCopies(nodeIds.length, attributeId.uid()));
	}

}
