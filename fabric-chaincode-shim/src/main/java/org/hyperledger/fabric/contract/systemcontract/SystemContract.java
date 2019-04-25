/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.systemcontract;

import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;

import io.swagger.v3.oas.annotations.info.Info;

@Contract(namespace = "org.hyperledger.fabric.SystemContract", info = @Info(title = "Fabric System Contract", description = "Provides information about the contracts within this container"))
public class SystemContract implements ContractInterface {

    public SystemContract() {

    }

    @Transaction(submit=false)
    public String getMetadata() {
        String jsonmetadata = MetadataBuilder.getMetadata();
        return jsonmetadata;
    }

}
