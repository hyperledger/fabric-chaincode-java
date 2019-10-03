/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration;

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

        // create the docker-compose command
        DockerComposeBuilder composebuilder = DockerCompose.newBuilder()
                .file("src/test/resources/first-network/docker-compose-cli.yaml");

        // close down anything running...
        composebuilder.duplicate().down().build().run();

        // ...and bring up
        DockerCompose compose = composebuilder.up().detach().build();
        compose.run();

        // the cli container contains a script that does the channel create, joing
        // and chaincode install/instantiate
        DockerBuilder dockerBuilder = new Docker.DockerBuilder();
        Docker docker = dockerBuilder.exec().container("cli").script("./scripts/script.sh").build();
        docker.run();
    }

    @Test
    public void TestSACCChaincodeInstallInstantiateInvokeQuery() {

        // Need to send a number of 'peer chaincode invoke' commands
        // Setup the core buider command and then duplicate per test
        PeerBuilder coreBuilder = Peer.newBuilder().ccname("javacc").channel("mychannel");
        Result r;
        String text;
        // 2019-10-02 13:05:59.812 UTC [chaincodeCmd] chaincodeInvokeOrQuery -> INFO 004 Chaincode invoke successful. result: status:200 message:"200"

        r = coreBuilder.duplicate().argsTx(new String[] { "set", "b", "200" }).build().run();
        text = r.stderr.stream()
            .filter(line -> line.matches(".*chaincodeInvokeOrQuery.*"))
            .collect(Collectors.joining(System.lineSeparator()));
        assertThat(text, containsString("result: status:200 message:\"200\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "get", "a" }).build().run();
        text = r.stderr.stream()
            .filter(line -> line.matches(".*chaincodeInvokeOrQuery.*"))
            .collect(Collectors.joining(System.lineSeparator()));
        assertThat(text, containsString("result: status:200 message:\"100\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "get", "b" }).build().run();
        text = r.stderr.stream()
            .filter(line -> line.matches(".*chaincodeInvokeOrQuery.*"))
            .collect(Collectors.joining(System.lineSeparator()));
        assertThat(text, containsString("result: status:200 message:\"200\""));

    }

}
