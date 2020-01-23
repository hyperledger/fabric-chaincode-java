/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.simplepath;

import static org.hamcrest.Matchers.is;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.READY;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.REGISTER;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.TRANSACTION;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput.Builder;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage.Response;
import org.hyperledger.fabric.shim.mock.peer.ChaincodeMockPeer;
import org.hyperledger.fabric.shim.mock.peer.RegisterStep;
import org.hyperledger.fabric.shim.mock.peer.ScenarioStep;
import org.hyperledger.fabric.shim.utils.MessageUtil;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;

import com.google.protobuf.ByteString;

public final class ContractSimplePath {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private ChaincodeMockPeer server;

    @After
    public void afterTest() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    /**
     * Test starting the contract logic
     *
     * @throws Exception
     */
    @Test
    public void testContract() throws Exception {

        final List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);
        ContractRouter.main(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});

        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        assertThat(server.getLastMessageSend().getType(), is(READY));
        assertThat(server.getLastMessageRcvd().getType(), is(REGISTER));
        setLogLevel("INFO");
    }

    public ChaincodeMessage newInvokeFn(final String[] args) {
        final Builder invokePayload = Chaincode.ChaincodeInput.newBuilder();
        for (final String arg : args) {
            invokePayload.addArgs(ByteString.copyFromUtf8(arg));
        }

        return MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload.build().toByteString(), null);
    }

    public String getLastReturnString() throws Exception {
        final Response resp = ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload());
        return (resp.getPayload().toStringUtf8());
    }

    public void setLogLevel(final String logLevel) {
        environmentVariables.set("CORE_CHAINCODE_LOGGING_SHIM", logLevel);
        environmentVariables.set("CORE_CHAINCODE_LOGGING_LEVEL", logLevel);
    }
}
