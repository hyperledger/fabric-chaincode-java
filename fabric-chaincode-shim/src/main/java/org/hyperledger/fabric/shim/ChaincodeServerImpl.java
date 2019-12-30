/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

import org.hyperledger.fabric.metrics.Metrics;

import java.io.IOException;
import java.util.Properties;

public class ChaincodeServerImpl implements ChaincodeServer {

    /**
     * Server.
     *
     */
    private final GrpcServer grpcServer;

    private final ChaincodeBase chaincodeBase;

    /**
     * configure and init server.
     *
     * @param chaincodeBase - chaincode implementation (invoke, init)
     * @throws IOException
     */
    public ChaincodeServerImpl(final ChaincodeBase chaincodeBase) throws IOException {
        this.chaincodeBase = chaincodeBase;
        this.chaincodeBase.processEnvironmentOptions();
        this.chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);

        // create listener and grpc server
        grpcServer = new NettyGrpcServer(chaincodeBase);
    }

    /**
     * run external chaincode server.
     *
     * @throws IOException, InterruptedException problem while start grpc server
     */
    public void start() throws IOException, InterruptedException {
        grpcServer.start();
        grpcServer.blockUntilShutdown();
    }

    /**
     * shutdown now grpc server.
     */
    public void stop() {
        grpcServer.stop();
    }
}
