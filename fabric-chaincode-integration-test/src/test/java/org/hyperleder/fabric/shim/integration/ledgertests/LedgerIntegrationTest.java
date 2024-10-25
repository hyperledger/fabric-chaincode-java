/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.ledgertests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import org.hyperleder.fabric.shim.integration.util.FabricState;
import org.hyperleder.fabric.shim.integration.util.InvokeHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Basic Java Chaincode Test */
public class LedgerIntegrationTest {

    @BeforeAll
    public static void setUp() throws Exception {

        FabricState.getState().start();
    }

    @Test
    public void TestLedgers() {
        InvokeHelper helper = InvokeHelper.newHelper("ledgercc", "sachannel");

        String text = helper.invoke("org1", "accessLedgers");
        assertThat(text, containsString("success"));
    }
}
