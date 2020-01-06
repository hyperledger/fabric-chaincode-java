/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import io.grpc.stub.StreamObserver;
import org.hyperledger.fabric.protos.peer.ChaincodeGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.impl.InnvocationTaskManager;

import java.io.IOException;

public class ChatChaincodeWithPeer extends ChaincodeGrpc.ChaincodeImplBase {

    private ChaincodeBase chaincodeBase;

    ChatChaincodeWithPeer(final ChaincodeBase chaincodeBase) throws IOException {
        if (chaincodeBase == null) {
            throw new IOException("chaincodeBase can't be null");
        }

        if (chaincodeBase.getId() == null || chaincodeBase.getId().isEmpty()) {
            throw new IOException("chaincode id not set, set env 'CORE_CHAINCODE_ID_NAME', for example 'CORE_CHAINCODE_ID_NAME=mycc'");
        }
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

        final InnvocationTaskManager itm;
        try {
             itm = chaincodeBase.connectToPeer(responseObserver);
            if (itm == null) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            responseObserver.onError(e);
            return null;
        }

        return new StreamObserver<ChaincodeShim.ChaincodeMessage>() {
            @Override
            public void onNext(final ChaincodeShim.ChaincodeMessage value) {
                itm.onChaincodeMessage(value);
            }

            @Override
            public void onError(final Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
