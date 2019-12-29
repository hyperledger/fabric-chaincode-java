/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

import java.io.IOException;

/**
 * External chaincode server.
 */
public interface ChaincodeServer {

    /**
     * For run external chaincode.
     *
     * @throws IOException problem while start grpc server
     */
    void start() throws IOException;

    /**
     * Stop grpc server.
     *
     * @throws InterruptedException
     */
    void stop() throws InterruptedException;

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     *
     * @throws InterruptedException
     */
    void blockUntilShutdown() throws InterruptedException;

}
