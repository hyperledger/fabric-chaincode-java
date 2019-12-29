/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import java.io.IOException;

public class ServerImpl implements Server {

    private GrpcServer grpcServer;

    public ServerImpl(String addressChaincodeServer, TlsConfig tlsConfig, ChaincodeBase chaincodeBase) throws IOException {
        grpcServer = new NettyGrpcServer(addressChaincodeServer, tlsConfig, chaincodeBase);
    }

    public void start() throws IOException {
        if (grpcServer == null) {
            throw new IOException("null server");
        }

        grpcServer.start();
    }

    public void stop() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.stop();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.blockUntilShutdown();
        }
    }
}
