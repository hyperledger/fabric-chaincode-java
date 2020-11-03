/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.ledgertests;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import org.hyperleder.fabric.shim.integration.util.FabricState;
import org.hyperleder.fabric.shim.integration.util.InvokeHelper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Basic Java Chaincode Test
 *
 */
public class LedgerIntegrationTest {

   @BeforeClass
    public static void setUp() throws Exception {
        FabricState.getState().start();
        FabricState.getState().startChannel("sbechannel");
    }

   @Test
    public void TestLedgers(){
        InvokeHelper helper = InvokeHelper.newHelper("ledgercc","sbechannel");
        
        String text = helper.invoke(new String[]{"accessLedgers"});
        assertThat(text, containsString("success"));
        
    }

}
