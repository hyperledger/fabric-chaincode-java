/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

class NettyGrpcServerTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

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
    void initNoTls() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void initTls() {
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "true");
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    chaincodeBase.processEnvironmentOptions();
                    chaincodeBase.validateOptions();
                    Assertions.assertTrue(chaincodeBase.isTlsEnabled());
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase);
                },
                "not implemented yet"
        );
    }

    @Test
    void initNull() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(null);
                },
                "chaincode must be specified"
        );
    }

    @Test
    void initNullEnvNotSet() {
        clearEnv();
        try {
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void initEnvNotSet() {
        clearEnv();
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase);
                },
                "chaincode server port not defined in system env. for example 'PORT_CHAINCODE_SERVER=9999'"
        );
    }

    @Test
    void initEnvSetPortChaincodeServer() {
        clearEnv();
        environmentVariables.set("PORT_CHAINCODE_SERVER", "9999");

        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase);
                },
                "chaincode id not set, set env 'CORE_CHAINCODE_ID_NAME', for example 'CORE_CHAINCODE_ID_NAME=mycc'"
        );
    }

    @Test
    void initEnvSetPortChaincodeServerAndCoreChaincodeIdName() throws IOException {
        clearEnv();
        environmentVariables.set("PORT_CHAINCODE_SERVER", "9998");
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();
        chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);

        NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase);

    }

    @Test
    void startAndStopSetCoreChaincodeIdName() {
        clearEnv();
        environmentVariables.set("PORT_CHAINCODE_SERVER", "9999");
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        try {
            ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            chaincodeBase.validateOptions();

            Properties props = chaincodeBase.getChaincodeConfig();
            Metrics.initialize(props);

            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase);
            new Thread(() -> {
                try {
                    nettyGrpcServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nettyGrpcServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStop() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase);
            new Thread(() -> {
                try {
                    nettyGrpcServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nettyGrpcServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
