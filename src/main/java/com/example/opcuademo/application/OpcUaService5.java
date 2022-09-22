package com.example.opcuademo.application;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.springframework.stereotype.Service;

import com.example.opcuademo.common.OpcUaClientConnectionPool;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpcUaService5 {


	@SneakyThrows
	public void startTask()  {

		OpcUaClient client = OpcUaClientConnectionPool.createClient();

		OpcUaClientConfig config = client.getConfig();
		String endpointUrl = config.getEndpoint().getEndpointUrl();
		System.out.println("endpointUrl = " + endpointUrl);

		ManagedSubscription subscription = ManagedSubscription.create(client);
		subscription.addDataChangeListener((items, values) -> {
			for (int i = 0; i < items.size(); i++) {
				log.info(
					"subscription value received: item={}, value={}",
					items.get(i).getNodeId(), values.get(i).getValue()
				);
			}
		});
		ManagedDataItem dataItem = subscription.createDataItem(
			Identifiers.Server_ServerStatus_CurrentTime
		);

		if (dataItem.getStatusCode().isGood()) {
			log.info("item created for nodeId={}", dataItem.getNodeId());

			// let the example run for 5 seconds before completing
		} else {
			log.warn(
				"failed to create item for nodeId={} (status={})",
				dataItem.getNodeId(), dataItem.getStatusCode()
			);
		}

	}
}
