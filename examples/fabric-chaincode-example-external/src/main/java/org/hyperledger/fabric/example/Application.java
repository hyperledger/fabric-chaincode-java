/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.example;

import org.hyperledger.fabric.shim.ChaincodeServer;
import org.hyperledger.fabric.shim.ChaincodeServerImpl;

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
     *
     *  To enable metrics ensure that there is a standard format Java properites file
     * examples/fabric-chaincode-example-external/src/main/resources/config.props
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ChaincodeServer chaincodeServer = new ChaincodeServerImpl(new SimpleAsset());
        chaincodeServer.start();
    }
}