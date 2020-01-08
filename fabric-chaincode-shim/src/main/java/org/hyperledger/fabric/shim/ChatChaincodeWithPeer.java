/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import io.grpc.stub.StreamObserver;
import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.protos.peer.ChaincodeGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;

import java.io.IOException;
import java.util.logging.Logger;

public class ChatChaincodeWithPeer extends ChaincodeGrpc.ChaincodeImplBase {
    private static Logger logger = Logger.getLogger(ChatChaincodeWithPeer.class.getName());

    private ChaincodeBase chaincodeBase;

    ChatChaincodeWithPeer(final ChaincodeBase chaincodeBase) throws IOException {
        if (chaincodeBase == null) {
            throw new IOException("chaincodeBase can't be null");
        }
        chaincodeBase.validateOptions();

        this.chaincodeBase = chaincodeBase;
    }

    /**
     * Chaincode as a server - peer establishes a connection to the chaincode as a client
     * Currently only supports a stream connection.
     *
     * @param responseObserver
     * @return
     */
    @Override
    public StreamObserver<ChaincodeShim.ChaincodeMessage> connect(final StreamObserver<ChaincodeShim.ChaincodeMessage> responseObserver) {
        if (responseObserver == null) {
            return null;
        }

        try {
            return chaincodeBase.connectToPeer(responseObserver);
        } catch (Exception e) {
            logger.severe(() -> "catch exception while chaincodeBase.connectToPeer(responseObserver)." + Logging.formatError(e));
            return null;
        }
    }
}
