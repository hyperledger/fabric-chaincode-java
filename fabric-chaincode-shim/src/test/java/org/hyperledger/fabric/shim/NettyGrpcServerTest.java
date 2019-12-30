/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class NettyGrpcServerTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    void start_stop() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("PORT_CHAINCODE_SERVER", "9999");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "false");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");

        final ChaincodeBase cb = new EmptyChaincode();
        try {
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(cb);
//            nettyGrpcServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void start_blockUntilShutdown() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("PORT_CHAINCODE_SERVER", "9999");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "false");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");

        final ChaincodeBase cb = new EmptyChaincode();
//        try {
        try {
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(cb);
        } catch (IOException e) {
            e.printStackTrace();
        }
//            nettyGrpcServer.start();
//            nettyGrpcServer.blockUntilShutdown();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    void blockUntilShutdown() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("PORT_CHAINCODE_SERVER", "9999");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "false");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");

        final ChaincodeBase cb = new EmptyChaincode();
        try {
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(cb);

//            nettyGrpcServer.blockUntilShutdown();
//             | InterruptedException e
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void stop() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "false");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");

        final ChaincodeBase cb = new EmptyChaincode();
        try {
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(cb);

            nettyGrpcServer.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}