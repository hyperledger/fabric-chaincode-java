/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.protobuf.ByteString;

public final class InvocationTaskManagerTest {

    private InvocationTaskManager itm;
    private ChaincodeBase chaincode;
    private Logger perfLogger;

    @BeforeEach
    public void setup() {
        Metrics.initialize(new Properties());

        chaincode = Mockito.mock(ChaincodeBase.class);
        final ChaincodeID id = ChaincodeID.newBuilder().setName("randomname").build();
        when(chaincode.getChaincodeConfig()).thenReturn(new Properties());
        this.itm = InvocationTaskManager.getManager(chaincode, id);

        perfLogger = LogManager.getLogManager().getLogger("org.hyperledger.Performance");
        perfLogger.setLevel(Level.ALL);
        this.itm.setResponseConsumer((value) -> {
        });
    }

    @AfterEach
    public void teardown() {

        itm.shutdown();
        perfLogger.setLevel(Level.INFO);
    }

    @Test
    public void register() throws UnsupportedEncodingException {
        itm.register();
    }

    @Test
    public void onMessageTestTx() throws UnsupportedEncodingException {

        final ChaincodeMessage msg = ChaincodeMessageFactory.newEventMessage(ChaincodeMessage.Type.TRANSACTION,
                "mychannel", "txid", ByteString.copyFrom("Hello", "UTF-8"));

        when(chaincode.getChaincodeConfig()).thenReturn(new Properties());
        chaincode.setState(ChaincodeBase.CCState.READY);

        itm.onChaincodeMessage(msg);

    }

    @Test
    public void onWrongCreatedState() throws UnsupportedEncodingException {

        perfLogger.setLevel(Level.ALL);
        final ChaincodeMessage msg = ChaincodeMessageFactory.newEventMessage(ChaincodeMessage.Type.TRANSACTION,
                "mychannel", "txid", ByteString.copyFrom("Hello", "UTF-8"));

        when(chaincode.getChaincodeConfig()).thenReturn(new Properties());
        chaincode.setState(ChaincodeBase.CCState.CREATED);

        itm.onChaincodeMessage(msg);

    }

    @Test
    public void onWrongEstablishedState() throws UnsupportedEncodingException {

        final ChaincodeMessage msg = ChaincodeMessageFactory.newEventMessage(ChaincodeMessage.Type.TRANSACTION,
                "mychannel", "txid", ByteString.copyFrom("Hello", "UTF-8"));

        when(chaincode.getChaincodeConfig()).thenReturn(new Properties());
        chaincode.setState(ChaincodeBase.CCState.ESTABLISHED);

        // final InvocationTaskManager itm =
        // InvocationTaskManager.getManager(chaincode, id);
        itm.onChaincodeMessage(msg);

    }

    @Test
    public void onErrorResponse() throws UnsupportedEncodingException {

        final ChaincodeMessage msg = ChaincodeMessageFactory.newEventMessage(ChaincodeMessage.Type.ERROR, "mychannel",
                "txid", ByteString.copyFrom("Hello", "UTF-8"));

        when(chaincode.getChaincodeConfig()).thenReturn(new Properties());
        chaincode.setState(ChaincodeBase.CCState.READY);

        itm.onChaincodeMessage(msg);

    }
}
