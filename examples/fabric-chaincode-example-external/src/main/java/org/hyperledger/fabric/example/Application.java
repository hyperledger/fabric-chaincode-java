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
    private static final String CORE_PEER_ADDRESS = "CORE_PEER_ADDRESS";
    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";

    /**
     * run application with ENV variable.
     *
     * CORE_CHAINCODE_ID_NAME=externalcc:06d1d324e858751d6eb4211885e9fd9ff74b62cb4ffda2242277fac95d467033;
     * CORE_PEER_ADDRESS=127.0.0.1:7051;
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
        if (portChaincodeServer == null || portChaincodeServer.isEmpty()) {
            throw new IOException("chaincode server port not defined in system env. for example 'PORT_CHAINCODE_SERVER=9999'");
        }
        final int port = Integer.parseInt(portChaincodeServer);
        grpcServerSetting.setPortChaincodeServer(port);

        final String corePeerAddress = System.getenv(CORE_PEER_ADDRESS);
        if (corePeerAddress == null || corePeerAddress.isEmpty()) {
            throw new IOException("core peer address not defined in system env. for example 'CORE_PEER_ADDRESS=127.0.0.1:7052'");
        }

        final String coreChaincodeIdName = System.getenv(CORE_CHAINCODE_ID_NAME);
        if (coreChaincodeIdName == null || coreChaincodeIdName.isEmpty()) {
            throw new IOException("core peer address not defined in system env. for example 'CORE_CHAINCODE_ID_NAME=externalcc:06d1d324e858751d6eb4211885e9fd9ff74b62cb4ffda2242277fac95d467033'");
        }

        ChaincodeServer chaincodeServer = new ChaincodeServerImpl(new ContractRouter(new String[] {"-a", corePeerAddress, "-i", coreChaincodeIdName}), grpcServerSetting);
        chaincodeServer.start();
    }
}