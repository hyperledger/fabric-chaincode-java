/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

import java.io.IOException;

public class NettyChaincodeServer implements ChaincodeServer {

    /**
     * Server.
     */
    private final GrpcServer grpcServer;

    /**
     * configure and init server.
     *
     * @param chaincodeBase - chaincode implementation (invoke, init)
     * @param chaincodeServerProperties - setting for grpc server
     * @throws IOException
     */
    public NettyChaincodeServer(final ChaincodeBase chaincodeBase, final ChaincodeServerProperties chaincodeServerProperties) throws IOException {
        // create listener and grpc server
        grpcServer = new NettyGrpcServer(chaincodeBase, chaincodeServerProperties);
    }

    /**
     * run external chaincode server.
     *
     * @throws IOException          problem while start grpc server
     * @throws InterruptedException thrown when block and awaiting shutdown gprc server
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
