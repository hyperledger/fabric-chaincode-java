/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.systemcontract;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;

/**
 *
 */
@Contract(name = "org.hyperledger.fabric",
        info = @Info(title = "Fabric System Contract", description = "Provides information about the contracts within this container"))
public final class SystemContract implements ContractInterface {

    /**
     *
     */
    public SystemContract() {

    }

    /**
     *
     * @param ctx
     * @return Metadata
     */
    @Transaction(submit = false, name = "GetMetadata")
    public String getMetadata(final Context ctx) {
        final String jsonmetadata = MetadataBuilder.getMetadata();
        return jsonmetadata;
    }

}
