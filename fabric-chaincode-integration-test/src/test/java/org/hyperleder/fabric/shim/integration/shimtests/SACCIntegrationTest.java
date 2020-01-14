/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.shimtests;
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
public class SACCIntegrationTest {

    @BeforeClass
    public static void setUp() throws Exception {
        FabricState.getState().start();       
        FabricState.getState().startChannel("sachannel");
    }

   @Test
    public void TestLedger(){

        InvokeHelper helper = InvokeHelper.newHelper("shimcc", "sachannel");
        String text = helper.invoke(new String[]{"putBulkStates"});
        assertThat(text, containsString("success"));
        
        text = helper.invoke(new String[]{"getByRange","key120","key170"});
        assertThat(text, containsString("50"));

        text = helper.invoke(new String[]{"getByRangePaged","key120","key170","10",""});
        System.out.println(text);
        assertThat(text, containsString("key130"));

    }

}
