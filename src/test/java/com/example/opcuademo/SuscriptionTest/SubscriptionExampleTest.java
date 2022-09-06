package com.example.opcuademo.SuscriptionTest;

import static com.google.common.collect.Lists.*;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class SubscriptionExampleTest {


	private CompletableFuture<OpcUaClient> future = new CompletableFuture<>();
	private DefaultTrustListManager trustListManager;

	private OpcUaClient client;


	@BeforeEach
	void init() throws Exception {


		this.client = createClient();

	}


	@Test
	public void opc_ua_데이터_연동_확인_테스트 () throws Exception {

		client.connect().get();
		// synchronous connect

		// create a subscription @ 1000ms
		UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

		// subscribe to the Value attribute of the server's CurrentTime node
		NodeId nodeIdNumeric = new NodeId(2,"System.Temp.Value");


		ReadValueId readValueId = new ReadValueId(
			nodeIdNumeric,
			AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE
		);
		UInteger clientHandle = subscription.nextClientHandle();

		MonitoringParameters parameters = new MonitoringParameters(
			clientHandle,
			1000.0,     // sampling interval
			null,    // filter, null means use default
			uint(10),   // queue size
			true        // discard oldest
		);

		MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
			readValueId,
			MonitoringMode.Reporting,
			parameters
		);
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

		// let the example run for 5 seconds then terminate
		Thread.sleep(5000);
		future.complete(client);
	}

	private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
		System.out.println("item = " + item.getReadValueId().getNodeId());
		System.out.println("value = " + value.getValue());
	}

	private OpcUaClient createClient() throws Exception {
		Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "client", "security");
		Files.createDirectories(securityTempDir);
		if (!Files.exists(securityTempDir)) {
			throw new Exception("unable to create security dir: " + securityTempDir);
		}
		String url = "opc.tcp://192.168.0.249:48030";

		File pkiDir = securityTempDir.resolve("pki").toFile();
		LoggerFactory.getLogger(getClass())
			.info("security dir: {}", securityTempDir.toAbsolutePath());
		LoggerFactory.getLogger(getClass())
			.info("security pki dir: {}", pkiDir.getAbsolutePath());
		KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

		trustListManager = new DefaultTrustListManager(pkiDir);

		DefaultClientCertificateValidator certificateValidator =
			new DefaultClientCertificateValidator(trustListManager);


		return OpcUaClient.create(
			url,
			endpoints ->
				endpoints.stream()
					.findFirst(),
			configBuilder ->
				configBuilder
					.setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
					.setApplicationUri("urn:eclipse:milo:examples:client")
					.setKeyPair(loader.getClientKeyPair())
					.setCertificate(loader.getClientCertificate())
					.setCertificateChain(loader.getClientCertificateChain())
					.setCertificateValidator(certificateValidator)
					.setIdentityProvider( new AnonymousProvider())
					.setRequestTimeout(uint(5000))
					.build()
		);
	}
}