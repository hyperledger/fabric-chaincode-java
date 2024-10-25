/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyperledger.fabric.protos.peer.ChaincodeMessage.Type.READY;
import static org.hyperledger.fabric.protos.peer.ChaincodeMessage.Type.REGISTER;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.mock.peer.ChaincodeMockPeer;
import org.hyperledger.fabric.shim.mock.peer.RegisterStep;
import org.hyperledger.fabric.shim.mock.peer.ScenarioStep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public final class ChaincodeWithoutPackageTest {
    private ChaincodeMockPeer server;

    @AfterEach
    public void afterTest() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testRegisterChaincodeWithoutPackage() throws Exception {
        final ChaincodeBase cb = new EmptyChaincodeWithoutPackage();

        final List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());

        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});

        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        assertThat(server.getLastMessageSend().getType()).isEqualTo(READY);
        assertThat(server.getLastMessageRcvd().getType()).isEqualTo(REGISTER);
    }
}
