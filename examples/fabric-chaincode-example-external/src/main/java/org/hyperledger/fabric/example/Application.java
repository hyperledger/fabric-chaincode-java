/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.example;

import org.hyperledger.fabric.shim.ChaincodeServer;
import org.hyperledger.fabric.shim.ChaincodeServerImpl;
import org.hyperledger.fabric.shim.TlsConfig;

import java.io.IOException;

public class Application {

    /**
     * run application with ENV variable.
     *
     * CORE_CHAINCODE_ID_NAME=externalcc:06d1d324e858751d6eb4211885e9fd9ff74b62cb4ffda2242277fac95d467033;
     * CORE_PEER_ADDRESS=127.0.0.1:7051;
     * CORE_PEER_TLS_ENABLED=false;
     * CORE_PEER_TLS_ROOTCERT_FILE=src/test/resources/ca.crt;
     * CORE_TLS_CLIENT_KEY_PATH=src/test/resources/client.key.enc;
     * CORE_TLS_CLIENT_CERT_PATH=src/test/resources/client.crt.enc;
     * PORT_CHAINCODE_SERVER=9999;
     * CHAINCODE_METRICS_ENABLED=false;
     * CHAINCODE_METRICS_PROVIDER=org.example.metrics.provider
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final String portChaincodeServer = System.getenv("PORT_CHAINCODE_SERVER");
        if (portChaincodeServer == null) {
            throw new IOException("chaincode server address not defined in system env 'ADDRESS_CHAINCODE_SERVER'");
        }
        final int port = Integer.parseInt(portChaincodeServer);

        final TlsConfig tlsConfig = new TlsConfig();
        tlsConfig.setDisabled(true);
        ChaincodeServer chaincodeServer = new ChaincodeServerImpl(new SimpleAsset(), port, tlsConfig);

        chaincodeServer.start();
        chaincodeServer.blockUntilShutdown();
    }
}