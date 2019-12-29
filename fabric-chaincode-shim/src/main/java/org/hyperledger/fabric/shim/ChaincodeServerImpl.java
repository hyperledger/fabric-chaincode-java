/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import org.hyperledger.fabric.metrics.Metrics;

import java.io.IOException;
import java.util.Properties;

public class ChaincodeServerImpl implements ChaincodeServer {

    private Server server;

    private final ChaincodeBase chaincodeBase;

    public ChaincodeServerImpl(ChaincodeBase chaincodeBase, String addressChaincodeServer, TlsConfig tlsConfig) throws IOException {
        this.chaincodeBase = chaincodeBase;
        this.chaincodeBase.processEnvironmentOptions();
        this.chaincodeBase.validateOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);

        // create listener and grpc server
        server = new ServerImpl(addressChaincodeServer, tlsConfig, chaincodeBase);
    }

    // Start the server
    public void start() throws Exception {
        server.start();
    }
    public void blockUntilShutdown() throws InterruptedException {
        server.blockUntilShutdown();
    }
}
