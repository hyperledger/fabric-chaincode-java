/*
 * Copyright 2020 IBM All Rights Reserved.
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
     * run external chaincode server.
     *
     * @throws IOException problem while start grpc server
     * @throws InterruptedException thrown when block and awaiting shutdown gprc server
     */
    void start() throws IOException, InterruptedException;

    /**
     * shutdown now grpc server.
     */
    void stop();

}
