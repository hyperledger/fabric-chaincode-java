/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.ledgertests;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import org.hyperleder.fabric.shim.integration.util.InvokeHelper;
import org.junit.Test;

/**
 * Basic Java Chaincode Test
 *
 */
public class LedgerIntegrationTest {

   @Test
    public void TestLedgers(){
        InvokeHelper helper = InvokeHelper.newHelper("ledgercc","sachannel");
        
        String text = helper.invoke("org1",new String[]{"accessLedgers"});
        assertThat(text, containsString("success"));
        
    }

}
