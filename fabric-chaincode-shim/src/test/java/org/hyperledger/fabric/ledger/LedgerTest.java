/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.Test;

public class LedgerTest {

    @Test
    public void getLedger() {

        final Context ctx = mock(Context.class);
        final Ledger ledger = Ledger.getLedger(ctx);

        assertThat(ledger).isNotNull();
        assertThat(ledger).isInstanceOf(Ledger.class);

        // assert that the ledger instance is new
        final Ledger ledger2 = Ledger.getLedger(ctx);
        assertThat(ledger2).isNotSameAs(ledger);
    }

    @Test
    public void getCollection() {

        final Context ctx = mock(Context.class);
        final Ledger ledger = Ledger.getLedger(ctx);
        final Collection collection = ledger.getDefaultCollection();
        assertThat(collection).isNotNull();
        assertThat(collection).isInstanceOf(Collection.class);

        collection.placeholder();

        final Collection collection2 = ledger.getDefaultCollection();
        assertThat(collection2).isNotSameAs(collection);
    }

    @Test
    public void getNamedCollection() {

        final Context ctx = mock(Context.class);
        final Ledger ledger = Ledger.getLedger(ctx);
        final Collection collection = ledger.getCollection("myPrivateCollection");
        assertThat(collection).isNotNull();
        assertThat(collection).isInstanceOf(Collection.class);

        final Collection collection2 = ledger.getCollection("myPrivateCollection");
        assertThat(collection2).isNotSameAs(collection);
    }

    @Test
    public void getOrganizationCollection() {

        final Context ctx = mock(Context.class);
        final Ledger ledger = Ledger.getLedger(ctx);
        final Collection collection = ledger.getOrganizationCollection("org1");
        assertThat(collection).isNotNull();
        assertThat(collection).isInstanceOf(Collection.class);

        final Collection collection2 = ledger.getOrganizationCollection("org1");
        assertThat(collection2).isNotSameAs(collection);
    }

}
