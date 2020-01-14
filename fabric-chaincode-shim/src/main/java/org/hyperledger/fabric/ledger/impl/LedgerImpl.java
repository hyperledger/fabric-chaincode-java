/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.ledger.impl;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.ledger.Collection;
import org.hyperledger.fabric.ledger.Ledger;
import org.hyperledger.fabric.shim.ChaincodeStub;

public final class LedgerImpl implements Ledger {

    // The Chaincode Stub or SPI to provide access to the underlying Fabric
    // APIs
    private final ChaincodeStub stub;

    /**
     * New Ledger Implementation.
     *
     * @param ctx Context transactional context to use
     */
    public LedgerImpl(final Context ctx) {
        this.stub = ctx.getStub();
    }

    @Override
    public Collection getCollection(final String name) {
        return new CollectionImpl(name, this);
    }

    @Override
    public Collection getDefaultCollection() {
        return this.getCollection(Collection.WORLD);
    }

    @Override
    public Collection getOrganizationCollection(final String mspid) {
        return this.getCollection("_implicit_org_" + mspid);
    }

}
