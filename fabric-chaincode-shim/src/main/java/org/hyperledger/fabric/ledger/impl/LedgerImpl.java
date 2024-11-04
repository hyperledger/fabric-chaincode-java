/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.ledger.impl;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.ledger.Collection;
import org.hyperledger.fabric.ledger.Ledger;

public final class LedgerImpl implements Ledger {

    /**
     * New Ledger Implementation.
     *
     * @param ctx Context transactional context to use
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public LedgerImpl(final Context ctx) {
        // Empty stub
    }

    @Override
    public Collection getCollection(final String name) {
        return new Collection() {
            @Override
            public void placeholder() {
                // Empty stub
            }
        };
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
