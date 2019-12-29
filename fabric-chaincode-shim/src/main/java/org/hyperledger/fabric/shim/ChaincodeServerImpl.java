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
    private GrpcServer grpcServer;

    private final ChaincodeBase chaincodeBase;

    /**
     * configure and init server.
     *
     * @param portChaincodeServer - port chaincode server for example 9999
     * @param tlsConfig - tls config to connect with peer
     * @param chaincodeBase - chaincode implementation (invoke, init)
     * @throws IOException
     */
    public ChaincodeServerImpl(final ChaincodeBase chaincodeBase, final int portChaincodeServer, final TlsConfig tlsConfig) throws IOException {
        this.chaincodeBase = chaincodeBase;
        this.chaincodeBase.processEnvironmentOptions();
        this.chaincodeBase.validateOptions();

        // init metrics - for disable set enc variable 'CHAINCODE_METRICS_ENABLED=false'
        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);

        // create listener and grpc server
        grpcServer = new NettyGrpcServer(portChaincodeServer, tlsConfig, chaincodeBase);
    }

    /**
     * For run external chaincode.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        if (grpcServer == null) {
            throw new IOException("null server");
        }

        grpcServer.start();
    }

    /**
     * Stop grpc server.
     *
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.stop();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     * @throws InterruptedException
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.blockUntilShutdown();
        }
    }
}
