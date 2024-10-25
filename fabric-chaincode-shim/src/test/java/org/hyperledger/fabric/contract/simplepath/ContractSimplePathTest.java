/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.simplepath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyperledger.fabric.protos.peer.ChaincodeMessage.Type.READY;
import static org.hyperledger.fabric.protos.peer.ChaincodeMessage.Type.REGISTER;
import static org.hyperledger.fabric.protos.peer.ChaincodeMessage.Type.TRANSACTION;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.protos.peer.ChaincodeInput;
import org.hyperledger.fabric.protos.peer.ChaincodeInput.Builder;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.Response;
import org.hyperledger.fabric.shim.mock.peer.ChaincodeMockPeer;
import org.hyperledger.fabric.shim.mock.peer.RegisterStep;
import org.hyperledger.fabric.shim.mock.peer.ScenarioStep;
import org.hyperledger.fabric.shim.utils.MessageUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public final class ContractSimplePathTest {
    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private ChaincodeMockPeer server;

    @AfterEach
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

        assertThat(server.getLastMessageSend().getType()).isEqualTo(READY);
        assertThat(server.getLastMessageRcvd().getType()).isEqualTo(REGISTER);
        setLogLevel("INFO");
    }

    public ChaincodeMessage newInvokeFn(final String[] args) {
        final Builder invokePayload = ChaincodeInput.newBuilder();
        for (final String arg : args) {
            invokePayload.addArgs(ByteString.copyFromUtf8(arg));
        }

        return MessageUtil.newEventMessage(
                TRANSACTION, "testChannel", "0", invokePayload.build().toByteString(), null);
    }

    public String getLastReturnString() throws Exception {
        final Response resp = Response.parseFrom(server.getLastMessageRcvd().getPayload());
        return (resp.getPayload().toStringUtf8());
    }

    public void setLogLevel(final String logLevel) {
        environmentVariables.set("CORE_CHAINCODE_LOGGING_SHIM", logLevel);
        environmentVariables.set("CORE_CHAINCODE_LOGGING_LEVEL", logLevel);
    }
}
