/*
 * Copyright 2019 IBM All Rights Reserved.
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

        ChatChaincodeWithPeer(final ChaincodeBase chaincodeBase) {
            this.chaincodeBase = chaincodeBase;
        }

        @Override
        public StreamObserver<ChaincodeShim.ChaincodeMessage> connect(final StreamObserver<ChaincodeShim.ChaincodeMessage> responseObserver) {
            try {
                final InnvocationTaskManager itm = chaincodeBase.connectToPeer(responseObserver);
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
            } catch (IOException e) {
                e.printStackTrace();
                // if we got error return nothing
                return null;
            }
        }
    }