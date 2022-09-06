/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.example.opcuademo.SuscriptionTest;

import static com.google.common.collect.Lists.*;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionExample implements ClientExample {

    private static  Logger logger = LoggerFactory.getLogger(SubscriptionExample.class);


    public static void main(String[] args) throws Exception {
        SubscriptionExample example = new SubscriptionExample();

        new ClientExampleRunner(example).run();
    }

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        client.connect().get();
        // synchronous connect

        // create a subscription @ 1000ms
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

        // subscribe to the Value attribute of the server's CurrentTime node
        NodeId nodeIdNumeric = new NodeId(3,"AirConditioner_1.Temperature");


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
                logger.info("item created for nodeId={}", item.getReadValueId().getNodeId());

            } else {
                logger.warn(
                    "failed to create item for nodeId={} (status={})",
                    item.getReadValueId().getNodeId(), item.getStatusCode());
            }
        }

        // let the example run for 5 seconds then terminate
        Thread.sleep(5000);
        future.complete(client);
    }

    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        System.out.printf("item={%s} %n value={%s} %n",item.getReadValueId().getNodeId(),value.getValue());

    }

}
