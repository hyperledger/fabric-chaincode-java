/*
* SPDX-License-Identifier: Apache-2.0
*/

package org.example;

import java.io.IOException;
import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.shim.ChaincodeServer;
import org.hyperledger.fabric.shim.ChaincodeServerProperties;
import org.hyperledger.fabric.shim.NettyChaincodeServer;

public class Application {

    private static final String CHAINCODE_SERVER_ADDRESS = "CHAINCODE_SERVER_ADDRESS";
    private static final String CORE_CHAINCODE_ID = "CORE_CHAINCODE_ID";

    public static void main(String[] args) throws Exception {
        ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();

        final String portChaincodeServer = System.getenv(CHAINCODE_SERVER_ADDRESS);
        if (portChaincodeServer == null || portChaincodeServer.isEmpty()) {
            throw new IOException("chaincode server port not defined in system env. for example 'CHAINCODE_SERVER_ADDRESS=0.0.0.0:9999");
        }
        final int port = Integer.parseInt(portChaincodeServer);
        chaincodeServerProperties.setPortChaincodeServer(port);

        final String coreChaincodeIdName = System.getenv(CORE_CHAINCODE_ID);
        if (coreChaincodeIdName == null || coreChaincodeIdName.isEmpty()) {
            throw new IOException("core chaincode id not defined in system env. for example 'CORE_CHAINCODE_ID=externalcc:06d1d324e858751d6eb4211885e9fd9ff74b62cb4ffda2242277fac95d467033'");
        }

        ContractRouter contractRouter = new ContractRouter(new String[] {"-i", coreChaincodeIdName});
        ChaincodeServer chaincodeServer = new NettyChaincodeServer(contractRouter, chaincodeServerProperties);

        contractRouter.startRouterWithChaincodeServer(chaincodeServer);
    }

}