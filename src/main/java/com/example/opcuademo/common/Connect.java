package com.example.opcuademo.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

public class Connect {

	private static OpcUaClientConfig buildConfiguration(final List<EndpointDescription> endpoints) {

		final OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();

		cfg.setEndpoint(findBest(endpoints));

		return cfg.build();

	}

	public static EndpointDescription findBest(final List<EndpointDescription> endpoints) {
		/*
		 * We simply assume we have at least one and pick the first one. In a more
		 * productive scenario you would actually evaluate things like ciphers and
		 * security.
		 */
		return endpoints.get(0);
	}

	// create client

	public static CompletableFuture<OpcUaClient> createClient() {
		final String endpoint = String.format("opc.tcp://%s:%s", Constants.HOST, Constants.PORT);

		return DiscoveryClient
			.getEndpoints(endpoint) // look up endpoints from remote
			.thenCompose(endpoints -> {
				try {
					return CompletableFuture.completedFuture(OpcUaClient.create(buildConfiguration(endpoints)));
				} catch (final UaException e) {
					e.printStackTrace();
				}
				return null;
			});
	}

	// connect

	public static CompletableFuture<OpcUaClient> connect() {
		return createClient()
			.thenCompose(opcUaClient -> opcUaClient.connect()) // trigger connect
			.thenApply(obj -> OpcUaClient.class.cast(obj)); // cast result of connect from UaClient to OpcUaClient
	}

	// synchronous way of doing things

	public static OpcUaClient createClientSync() throws InterruptedException, ExecutionException, UaException {
		final String endpoint = String.format("opc.tcp://%s:%s", Constants.HOST, Constants.PORT);

		final List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpoint)
			.get();

		return OpcUaClient.create(buildConfiguration(endpoints));
	}

	public static OpcUaClient connectSync() throws InterruptedException, ExecutionException, UaException {
		final OpcUaClient client = createClientSync();

		client.connect()
			.get();

		return client;
	}
}
