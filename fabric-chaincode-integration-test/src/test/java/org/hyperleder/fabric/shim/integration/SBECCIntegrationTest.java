/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.stream.Collectors;

import org.hyperleder.fabric.shim.integration.Command.Result;
import org.hyperleder.fabric.shim.integration.Docker.DockerBuilder;
import org.hyperleder.fabric.shim.integration.Peer.PeerBuilder;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.junit.BeforeClass;
import org.junit.Test;

public class SBECCIntegrationTest {

    @BeforeClass
    public static void setUp() throws Exception {

        // Call the inbuilt script to install/instantiate
        DockerBuilder dockerBuilder = new Docker.DockerBuilder();
        Docker docker = dockerBuilder.exec().container("cli").script("./scripts/script-sbe.sh").build();
        docker.run();
    }

    private String filter(List<String> lines){
        String text =  lines.stream()
        .filter(line -> line.matches(".*chaincodeInvokeOrQuery.*"))
        .collect(Collectors.joining(System.lineSeparator()));

        System.out.println(text);
        return text;
    }

    @Test
    public void RunSBE_pub_setget() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, ProposalException, InvalidArgumentException {
        String mode = "pub";
        // Need to send a number of 'peer chaincode invoke' commands
        // Setup the core buider command and then duplicate per test
        PeerBuilder coreBuilder = Peer.newBuilder().ccname("sbecc").channel("mychannel");
        Result r;

        String text;

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "foo" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"foo\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "addorgs", mode, "Org1MSP" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "listorgs", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"[\\\"Org1MSP\\\"]\" "));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val1" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"val1\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val2" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"val2\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "addorgs", mode, "Org2MSP" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "listorgs", mode }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200 payload:\"[\\\"Org2MSP\\\",\\\"Org1MSP\\\"]\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val3" }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200 payload:\"val3\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val4" }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200 payload:\"val4\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "delorgs", mode, "Org1MSP" }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "listorgs", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(filter(r.stderr), containsString("result: status:200 payload:\"[\\\"Org2MSP\\\"]\""));

    }


    @Test
    public void RunSBE_priv() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, ProposalException, InvalidArgumentException {
        String mode = "priv";

        // Need to send a number of 'peer chaincode invoke' commands
        // Setup the core buider command and then duplicate per test
        PeerBuilder coreBuilder = Peer.newBuilder().ccname("sbecc").channel("mychannel");
        Result r;
        String text;

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "foo" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"foo\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "addorgs", mode, "Org1MSP" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "listorgs", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"[\\\"Org1MSP\\\"]\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val1" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"val1\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val2" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"val2\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "addorgs", mode, "Org2MSP" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "listorgs", mode }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200 payload:\"[\\\"Org2MSP\\\",\\\"Org1MSP\\\"]\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val3" }).build().run(true);
        text = filter(r.stderr);
        assertThat(text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200 payload:\"val3\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "setval", mode, "val4" }).build().run(true);
        assertThat(filter(r.stderr),text, containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "getval", mode }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200 payload:\"val4\""));

        r = coreBuilder.duplicate().argsTx(new String[] { "delorgs", mode, "Org1MSP" }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200"));

        r = coreBuilder.duplicate().argsTx(new String[] { "listorgs", mode }).build().run(true);
        assertThat(filter(r.stderr), containsString("result: status:200 payload:\"[\\\"Org2MSP\\\"]\""));

    }

}
