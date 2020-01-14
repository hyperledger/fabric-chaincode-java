/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.ledger;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.ledger.impl.LedgerImpl;

/**
 * Ledger representing the overall shared Transaction Data of the Network.
 *
 * It is composed of a number of collections, one being the public or world
 * state, and other private data collections, including the implicit
 * organizational collections.
 *
 * Ledger objects can be passed between methods if required. All operations on
 * the Ledger directly or via any child object such as a Collection will be
 * controlled by the supplied transactional context.
 *
 */
public interface Ledger {

    /**
     * Get the Ledger instance that represents the current ledger state.
     *
     * Any interactions with the ledger will be done under the control of the
     * transactional context supplied. The ledger object may be passed to other
     * methods if required.
     *
     * A new instance is returned on each call.
     *
     * @param ctx Context The Transactional context to use for interactions with
     *            this ledger
     * @return Ledger instance
     */
    static Ledger getLedger(final Context ctx) {
        return new LedgerImpl(ctx);
    };

    /**
     * Return the a collection based on the supplied name.
     *
     * Private Data collections can be accessed by name.
     *
     * A new instance of a Collection object is returned on each call.
     *
     * @param name
     * @return Collection instance
     */
    Collection getCollection(String name);

    /**
     * Return the World State collection.
     *
     * A new instance of a Collection object is returned on each call.
     *
     * @return Collection instance
     */
    Collection getDefaultCollection();

    /**
     * Return a implicit organization collection.
     *
     * Given the mspid of the ogranization return the private data collection that
     * is implicitly created
     *
     * A new instance of a Collection object is returned on each call.
     *
     * @param mspid String Organization's mspid
     * @return Collection instance
     */
    Collection getOrganizationCollection(String mspid);

}
