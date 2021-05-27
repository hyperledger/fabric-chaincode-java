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
public class ContractInstallTest {

   @BeforeClass
    public static void setUp() throws Exception {
        FabricState.getState().start();
        FabricState.getState().startChannel("sachannel");
    }

   @Test
    public void TestInstall(){

        InvokeHelper helper = InvokeHelper.newHelper("baregradlecc","sachannel");        
        String text = helper.invoke(new String[]{"whoami"});
        assertThat(text, containsString("BareGradle"));
        
        helper = InvokeHelper.newHelper("baremaven","sachannel");        
        text = helper.invoke(new String[]{"whoami"});
        assertThat(text, containsString("BareMaven"));
        
        helper = InvokeHelper.newHelper("wrappermaven","sachannel");        
        text = helper.invoke(new String[]{"whoami"});
        assertThat(text, containsString("WrapperMaven"));        
    }

}