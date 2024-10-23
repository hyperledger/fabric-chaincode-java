/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.protos.peer.ChaincodeID;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.hyperledger.fabric.traces.Traces;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class InnvocationTaskManagerTest {
    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    void setEnv() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "false");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");
    }

    @AfterEach
    void clearEnv() {
        environmentVariables.remove("CORE_CHAINCODE_ID_NAME");
        environmentVariables.remove("CORE_PEER_ADDRESS");
        environmentVariables.remove("CORE_PEER_TLS_ENABLED");
        environmentVariables.remove("CORE_PEER_TLS_ROOTCERT_FILE");
        environmentVariables.remove("CORE_TLS_CLIENT_KEY_PATH");
        environmentVariables.remove("CORE_TLS_CLIENT_CERT_PATH");
    }

    @Test
    void getManager() throws IOException {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Traces.initialize(props);
        Metrics.initialize(props);

        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName("chaincodeIdNumber12345").build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);
    }

    @Test
    void getManagerChaincodeIDNull() throws IOException {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, null);
                },
                "chaincodeId can't be null");
    }

    @Test
    void getManagerChaincodeBaseNull() throws IOException {

        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName("chaincodeIdNumber12345").build();

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    final InvocationTaskManager itm = InvocationTaskManager.getManager(null, chaincodeId);
                },
                "chaincode is null");
    }

    @Test
    void onChaincodeMessage() throws IOException {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName("chaincodeIdNumber12345").build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);

        Assertions.assertThrows(
                IllegalArgumentException.class, () -> itm.onChaincodeMessage(null), "chaincodeMessage is null");
    }

    @Test
    void setResponseConsumer() throws IOException {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName("chaincodeIdNumber12345").build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);
        itm.setResponseConsumer(null);
    }

    @Test
    void registerException() {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName("chaincodeIdNumber12345").build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);

        Assertions.assertThrows(IllegalArgumentException.class, itm::register, "outgoingMessage is null");
    }

    @Test
    void onChaincodeMessageREGISTER() {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName("chaincodeIdNumber12345").build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);
        final Consumer<ChaincodeMessage> consumer = t -> {
            assertEquals(ChaincodeMessageFactory.newRegisterChaincodeMessage(chaincodeId), t);
        };

        itm.setResponseConsumer(consumer);
        final ChaincodeMessage chaincodeMessage = ChaincodeMessageFactory.newRegisterChaincodeMessage(chaincodeId);
        itm.onChaincodeMessage(chaincodeMessage);
    }

    @Test
    void onChaincodeMessageInvokeChaincode() {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final String chaincodeIdNumber = "chaincodeIdNumber12345";
        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName(chaincodeIdNumber).build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);
        final Consumer<ChaincodeMessage> consumer = t -> {
            assertEquals(ChaincodeMessageFactory.newRegisterChaincodeMessage(chaincodeId), t);
        };

        itm.setResponseConsumer(consumer);
        final ChaincodeMessage chaincodeMessage = ChaincodeMessageFactory.newInvokeChaincodeMessage(
                chaincodeIdNumber, "txid", ByteString.copyFromUtf8(""));
        itm.onChaincodeMessage(chaincodeMessage);
    }

    @Test
    void onChaincodeMessagePutState() {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        final String chaincodeIdNumber = "chaincodeIdNumber12345";
        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName(chaincodeIdNumber).build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);
        final Consumer<ChaincodeMessage> consumer = t -> {
            assertEquals(ChaincodeMessageFactory.newRegisterChaincodeMessage(chaincodeId), t);
        };

        itm.setResponseConsumer(consumer);
        final ChaincodeMessage chaincodeMessage = ChaincodeMessageFactory.newPutStateEventMessage(
                chaincodeIdNumber, "txid", "collection", "key", ByteString.copyFromUtf8("value"));
        itm.onChaincodeMessage(chaincodeMessage);
    }

    @Test
    void shutdown() throws IOException {

        final ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Traces.initialize(props);
        Metrics.initialize(props);

        final ManagedChannelBuilder<?> managedChannelBuilder = chaincodeBase.newChannelBuilder();
        ChaincodeSupportClient chaincodeSupportClient = new ChaincodeSupportClient(managedChannelBuilder);

        final ChaincodeID chaincodeId =
                ChaincodeID.newBuilder().setName("chaincodeIdNumber12345").build();
        final InvocationTaskManager itm = InvocationTaskManager.getManager(chaincodeBase, chaincodeId);
        itm.shutdown();
    }
}
