/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.contractinstall;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import org.hyperleder.fabric.shim.integration.util.InvokeHelper;
import org.junit.Test;

/**
 * Basic Java Chaincode Test
 *
 */
public class ContractInstallTest {

   @Test
    public void TestInstall(){

        InvokeHelper helper = InvokeHelper.newHelper("baregradlecc","sachannel");        
        String text = helper.invoke("org1",new String[]{"whoami"});
        assertThat(text, containsString("BareGradle"));
        
        helper = InvokeHelper.newHelper("baremaven","sachannel");        
        text = helper.invoke("org1",new String[]{"whoami"});
        assertThat(text, containsString("BareMaven"));
        
        helper = InvokeHelper.newHelper("wrappermaven","sachannel");        
        text = helper.invoke("org1",new String[]{"whoami"});
        assertThat(text, containsString("WrapperMaven"));        
    }

}