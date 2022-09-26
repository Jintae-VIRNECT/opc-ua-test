package com.example.opcuademo.application;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.stereotype.Service;

import com.example.opcuademo.common.OpcUaClientConnectionPool;
import com.example.opcuademo.common.Values;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequiredArgsConstructor
public class OpcUaService2 {

	private static final AtomicInteger clientHandles = new AtomicInteger();
	private final OpcUaClientConnectionPool opcUaClientConnectionPool;


	public void startTask() throws UaException, ExecutionException, InterruptedException {

		OpcUaClient client = opcUaClientConnectionPool.getConnection();

		final NodeId[] moreIds = new NodeId[] {
			Identifiers.Server_ServerStatus_BuildInfo_ManufacturerName,
			Identifiers.Server_ServerStatus_BuildInfo_ProductName,
			Identifiers.Server_ServerStatus_CurrentTime
		};

		// read values

		final CompletableFuture<List<DataValue>> future = read(
			client,
			AttributeId.Value,
			moreIds);

		final List<DataValue> values2 = future.get();
		Values.dumpValues(System.out, asList(moreIds), values2);

		client.disconnect().get();

	}


	public static CompletableFuture<List<DataValue>> read(
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




	public void stopTask() throws ExecutionException, InterruptedException {

		opcUaClientConnectionPool.shutdown();

	}
}
