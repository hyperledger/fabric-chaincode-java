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
    public static void setUp() throws Exception {

        // get current working directory for debug and reference purposes only
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        FabricState.getState().start();

        // the cli container contains a script that does the channel create, joing
        // and chaincode install/instantiate
        DockerBuilder dockerBuilder = new Docker.DockerBuilder();
        Docker docker = dockerBuilder.exec().container("cli").script("./scripts/script.sh").channel("sachannel").build();
        docker.run();
    }

    private String invoke(String... args){
        PeerBuilder coreBuilder = Peer.newBuilder().ccname("javacc").channel("sachannel");
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

        text = invoke(new String[]{"getMetricsProviderName"});
        System.out.println(text);
        assertThat(text, containsString("org.hyperledger.fabric.metrics.impl.DefaultProvider"));
    }

}
