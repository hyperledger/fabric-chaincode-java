/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Properties;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.protos.peer.ChaincodeID;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.hyperledger.fabric.traces.Traces;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class ChaincodeSupportClientTest {
    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    void testStartInvocationTaskManagerAndRequestObserverNull() throws IOException {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final ManagedChannelBuilder<?> managedChannelBuilder = chaincodeBase.newChannelBuilder();
        ChaincodeSupportClient chaincodeSupportClient = new ChaincodeSupportClient(managedChannelBuilder);

        assertThatThrownBy(
                        () -> {
                            final ChaincodeID chaincodeId = ChaincodeID.newBuilder()
                                    .setName("chaincodeIdNumber12345")
                                    .build();
                            final InvocationTaskManager itm =
                                    InvocationTaskManager.getManager(chaincodeBase, chaincodeId);

                            final StreamObserver<ChaincodeMessage> requestObserver = null;
                            chaincodeSupportClient.start(itm, requestObserver);
                        },
                        "StreamObserver 'requestObserver' for chat with peer can't be null")
                .isInstanceOf(IOException.class);
        environmentVariables.remove("CORE_CHAINCODE_ID_NAME");
    }

    @Test
    void testStartInvocationTaskManagerNullAndRequestObserver() throws IOException {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final ManagedChannelBuilder<?> managedChannelBuilder = chaincodeBase.newChannelBuilder();
        ChaincodeSupportClient chaincodeSupportClient = new ChaincodeSupportClient(managedChannelBuilder);

        assertThatThrownBy(
                        () -> {
                            chaincodeSupportClient.start(null, new StreamObserver<ChaincodeMessage>() {
                                @Override
                                public void onNext(final ChaincodeMessage value) {}

                                @Override
                                public void onError(final Throwable t) {}

                                @Override
                                public void onCompleted() {}
                            });
                        },
                        "InvocationTaskManager 'itm' can't be null")
                .isInstanceOf(IOException.class);
        environmentVariables.remove("CORE_CHAINCODE_ID_NAME");
    }
}
