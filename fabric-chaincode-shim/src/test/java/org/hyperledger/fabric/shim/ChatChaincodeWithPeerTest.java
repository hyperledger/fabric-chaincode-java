/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.hyperledger.fabric.shim.impl.ChaincodeMessageFactory;
import org.hyperledger.fabric.shim.utils.MessageUtil;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.INIT;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.INVOKE_CHAINCODE;
import static org.junit.jupiter.api.Assertions.*;

class ChatChaincodeWithPeerTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final String TEST_CHANNEL = "testChannel";

    @BeforeEach
    void setEnv() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("PORT_CHAINCODE_SERVER", "9999");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "false");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");
    }

    @AfterEach
    void clearEnv() {
        environmentVariables.clear("CORE_CHAINCODE_ID_NAME");
        environmentVariables.clear("PORT_CHAINCODE_SERVER");
        environmentVariables.clear("CORE_PEER_ADDRESS");
        environmentVariables.clear("CORE_PEER_TLS_ENABLED");
        environmentVariables.clear("CORE_PEER_TLS_ROOTCERT_FILE");
        environmentVariables.clear("CORE_TLS_CLIENT_KEY_PATH");
        environmentVariables.clear("CORE_TLS_CLIENT_CERT_PATH");
    }

    @Test
    void initNull() throws IOException {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    ChatChaincodeWithPeer chatChaincodeWithPeer = new ChatChaincodeWithPeer(null);
                },
                "chaincodeBase can't be null"
        );
    }

    @Test
    void init() throws IOException {
        ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);
        ChatChaincodeWithPeer chatChaincodeWithPeer = new ChatChaincodeWithPeer(chaincodeBase);
    }

    @Test
    void connectEnvNotSet() throws IOException {
        clearEnv();

        Assertions.assertThrows(
                IOException.class,
                () -> {
                    ChaincodeBase chaincodeBase = new EmptyChaincode();
                    ChatChaincodeWithPeer chatChaincodeWithPeer = new ChatChaincodeWithPeer(chaincodeBase);
                },
                "chaincode id not set, set env 'CORE_CHAINCODE_ID_NAME', for example 'CORE_CHAINCODE_ID_NAME=mycc'"
        );
    }

    @Test
    void connectNull() throws IOException {
        ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);

        ChatChaincodeWithPeer chatChaincodeWithPeer = new ChatChaincodeWithPeer(chaincodeBase);
        assertNull(chatChaincodeWithPeer.connect(null));
    }

    @Test
    void connectAndReceiveRegister() throws IOException {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);

        ChatChaincodeWithPeer chatChaincodeWithPeer = new ChatChaincodeWithPeer(chaincodeBase);
        final StreamObserver<ChaincodeShim.ChaincodeMessage> connect = chatChaincodeWithPeer.connect(new StreamObserver<ChaincodeShim.ChaincodeMessage>() {
            @Override
            public void onNext(ChaincodeShim.ChaincodeMessage value) {
                assertEquals(ChaincodeShim.ChaincodeMessage.Type.REGISTER, value.getType());
                assertEquals("\u0012\u0004mycc", value.getPayload().toStringUtf8());
            }

            @Override
            public void onError(Throwable t) {
                assertNull(t);
            }

            @Override
            public void onCompleted() {
            }
        });
        assertNotNull(connect);

        final ByteString payload = org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput.newBuilder().addArgs(ByteString.copyFromUtf8("")).build()
                .toByteString();
        final ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, TEST_CHANNEL, "0", payload, null);
        connect.onNext(initMsg);

        try {
            final List<byte[]> args = Stream.of("invoke", "a", "1").map(x -> x.getBytes(UTF_8)).collect(toList());
            final ByteString invocationSpecPayload = Chaincode.ChaincodeSpec.newBuilder().setChaincodeId(Chaincode.ChaincodeID.newBuilder().setName(chaincodeBase.getId()).build())
                    .setInput(Chaincode.ChaincodeInput.newBuilder().addAllArgs(args.stream().map(ByteString::copyFrom).collect(Collectors.toList())).build()).build()
                    .toByteString();

            final ChaincodeShim.ChaincodeMessage invokeChaincodeMessage = ChaincodeShim.ChaincodeMessage.newBuilder().setType(INVOKE_CHAINCODE).setChannelId(TEST_CHANNEL)
                    .setTxid("1").setPayload(invocationSpecPayload).build();
            connect.onNext(invokeChaincodeMessage);
            System.out.println(invokeChaincodeMessage.getPayload().toStringUtf8());
        } catch (Exception e) {

        }

        try {
            final List<byte[]> args = Stream.of("invoke", "a", "1").map(x -> x.getBytes(UTF_8)).collect(toList());
            final ByteString invocationSpecPayload = Chaincode.ChaincodeSpec.newBuilder().setChaincodeId(Chaincode.ChaincodeID.newBuilder().setName(chaincodeBase.getId()).build())
                    .setInput(Chaincode.ChaincodeInput.newBuilder().addAllArgs(args.stream().map(ByteString::copyFrom).collect(Collectors.toList())).build()).build()
                    .toByteString();

            final ChaincodeShim.ChaincodeMessage invokeChaincodeMessage = ChaincodeShim.ChaincodeMessage.newBuilder().setType(INVOKE_CHAINCODE).setChannelId(TEST_CHANNEL)
                    .setTxid("2").setPayload(invocationSpecPayload).build();
            connect.onNext(invokeChaincodeMessage);
            System.out.println(invokeChaincodeMessage.getPayload().toStringUtf8());
        } catch (Exception e) {

        }
    }
}