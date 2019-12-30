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
     * run external chaincode server.
     *
     * @throws IOException, InterruptedException problem while start grpc server
     */
    void start() throws IOException, InterruptedException;

    /**
     * shutdown now grpc server.
     */
    void stop();

}
