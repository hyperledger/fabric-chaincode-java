/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.hyperleder.fabric.shim.integration.Command.Result;
import org.hyperleder.fabric.shim.integration.Docker.DockerBuilder;
import org.hyperleder.fabric.shim.integration.DockerCompose.DockerComposeBuilder;
import org.hyperleder.fabric.shim.integration.Peer.PeerBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Basic Java Chaincode Test
 *
 */
public class SACCIntegrationTest {

    @BeforeClass
    public static void setUp() {
        CommandSingleton.setup();
    }

    private String invoke(String... args){
        PeerBuilder coreBuilder = Peer.newBuilder().ccname("javacc").channel("mychannel");
        Result r = coreBuilder.argsTx(args).build().run();
        System.out.println(r.stderr);
        String text = r.stderr.stream()
            .filter(line -> line.matches(".*chaincodeInvokeOrQuery.*"))
            .collect(Collectors.joining(System.lineSeparator()))
            .trim();

        if (!text.contains("result: status:200")){
            throw new RuntimeException(text);
        } 

        int payloadIndex = text.indexOf("payload:");
        if (payloadIndex>1){
            return text.substring(payloadIndex+9,text.length()-1);
        }
        return "status:200";
    }

    @Test
    public void TestQuery(){

        String text = invoke(new String[]{"putBulkStates"});
        assertThat(text, containsString("status:200"));
        
        text = invoke(new String[]{"getByRange","key120","key170"});
        assertThat(text, containsString("50"));

        text = invoke(new String[]{"getByRangePaged","key120","key170","10",""});
        System.out.println(text);
        assertThat(text, containsString("key130"));

    }

}
