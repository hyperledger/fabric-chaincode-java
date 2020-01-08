/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.example;

import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.shim.ChaincodeServer;
import org.hyperledger.fabric.shim.ChaincodeServerImpl;
import org.hyperledger.fabric.shim.GrpcServerSetting;

import java.io.IOException;

public class Application {

    private static final String PORT_CHAINCODE_SERVER = "PORT_CHAINCODE_SERVER";

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
        GrpcServerSetting grpcServerSetting = new GrpcServerSetting();

        final String portChaincodeServer = System.getenv(PORT_CHAINCODE_SERVER);
        if (portChaincodeServer == null) {
            throw new IOException("chaincode server port not defined in system env. for example 'PORT_CHAINCODE_SERVER=9999'");
        }
        final int port = Integer.parseInt(portChaincodeServer);

        grpcServerSetting.setPortChaincodeServer(port);

        ChaincodeServer chaincodeServer = new ChaincodeServerImpl(new ContractRouter(new String[] {"-a", "127.0.0.1:7052", "-i", "testId"}), grpcServerSetting);
        chaincodeServer.start();
    }
}