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

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedSubscriptionDataExample implements ClientExample {

    public static void main(String[] args) throws Exception {
        ManagedSubscriptionDataExample example = new ManagedSubscriptionDataExample();

        new ClientExampleRunner(example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        client.connect().get();

        ManagedSubscription subscription = ManagedSubscription.create(client);

        subscription.addDataChangeListener((items, values) -> {
            for (int i = 0; i < items.size(); i++) {
                try {
                    logger.info(
                        "subscription value received: item={}, value={},readNamespaceTable={}",
                        items.get(i).getNodeId(), values.get(i).getValue(),
                        items.get(i).getClient().readNamespaceTable().getUri(0)
                    );

                } catch (UaException e) {
                    e.printStackTrace();
                }
            }
        });

        ManagedDataItem dataItem = subscription.createDataItem(
            Identifiers.Server_ServerStatus_CurrentTime
        );

        if (dataItem.getStatusCode().isGood()) {
            logger.info("item created for nodeId={}", dataItem.getNodeId());
            logger.info("=========================================================");
            logger.info(String.valueOf(dataItem.getMonitoredItem().getReadValueId()));

            // let the example run for 5 seconds before completing
            Thread.sleep(5000);

            dataItem.delete();
        } else {
            logger.warn(
                "failed to create item for nodeId={} (status={})",
                dataItem.getNodeId(), dataItem.getStatusCode()
            );
        }

        future.complete(client);
    }

}
