/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import java.io.IOException;
import java.net.URISyntaxException;
import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
final class ChaincodeServerImplTest {
    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    void setEnv() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CHAINCODE_SERVER_ADDRESS", "0.0.0.0:9999");
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
        environmentVariables.remove("CHAINCODE_SERVER_ADDRESS");
    }

    @Test
    void init() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            ChaincodeServer chaincodeServer =
                    new NettyChaincodeServer(chaincodeBase, chaincodeBase.getChaincodeServerConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void initEnvNotSet() {
        clearEnv();
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            ChaincodeServer chaincodeServer =
                    new NettyChaincodeServer(chaincodeBase, chaincodeBase.getChaincodeServerConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStop() {
        try {
            final ChaincodeBase chaincodeBase =
                    new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"});
            ChaincodeServer chaincodeServer =
                    new NettyChaincodeServer(chaincodeBase, chaincodeBase.getChaincodeServerConfig());
            new Thread(() -> {
                        try {
                            chaincodeServer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chaincodeServer.stop();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
