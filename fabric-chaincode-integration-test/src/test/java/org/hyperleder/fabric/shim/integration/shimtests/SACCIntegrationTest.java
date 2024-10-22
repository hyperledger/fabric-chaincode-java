/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.shimtests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import org.hyperleder.fabric.shim.integration.util.FabricState;
import org.hyperleder.fabric.shim.integration.util.InvokeHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Basic Java Chaincode Test */
public class SACCIntegrationTest {

    @BeforeAll
    public static void setUp() throws Exception {
        FabricState.getState().start();
    }

    @Test
    public void TestLedger() {

        InvokeHelper helper = InvokeHelper.newHelper("shimcc", "sachannel");
        String text = helper.invoke("org1", "putBulkStates");
        assertThat(text, containsString("success"));

        text = helper.invoke("org1", "getByRange", "key120", "key170");
        assertThat(text, containsString("50"));

        text = helper.invoke("org1", "getByRangePaged", "key120", "key170", "10", "");
        System.out.println(text);
        assertThat(text, containsString("key130"));

        text = helper.invoke("org1", "getMetricsProviderName");
        System.out.println(text);
        assertThat(text, containsString("org.hyperledger.fabric.metrics.impl.DefaultProvider"));
    }
}
