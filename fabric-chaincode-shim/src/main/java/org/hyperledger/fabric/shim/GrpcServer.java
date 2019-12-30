/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

import java.io.IOException;

/**
 * Common interface for grpc server.
 */
public interface GrpcServer {

    /**
     * start grpc server.
     *
     * @throws IOException problem while start grpc server
     */
    void start() throws IOException;

    /**
     * shutdown and await termination 'TIMEOUT_AWAIT_TIMEOUT_SECONDS.
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
