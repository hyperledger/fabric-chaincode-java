/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeCollectionConfigurationException;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


public class NoBuildChaincode {
    @ClassRule
    public static DockerComposeContainer env = new DockerComposeContainer(
            new File("src/test/resources/basic-network/docker-compose.yml")
    )
            .withLocalCompose(false)
            .withPull(true);

    @BeforeClass
    public static void setUp() throws Exception {
        Utils.setUp();
    }

    @AfterClass
    public static void shutDown() throws Exception {
        Utils.removeDevContainerAndImages();
    }

    @Test
    public void TestNoBuildChaincodeInstallInstantiateWithSrc() throws Exception {

        final CryptoSuite crypto = CryptoSuite.Factory.getCryptoSuite();

        // Create client and set default crypto suite
        System.out.println("Creating client");
        final HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(crypto);

        client.setUserContext(Utils.getAdminUser());

        Channel myChannel = Utils.getMyChannelBasicNetwork(client);

        InstallProposalRequest installProposalRequest = generateNoBuildInstallRequest(client, "javacc1", true);
        Utils.sendInstallProposals(client, installProposalRequest, myChannel.getPeers());

        // Instantiating chaincode
        InstantiateProposalRequest instantiateProposalRequest = generateNoBuildInstantiateRequest(client, "javacc1");
        ProposalResponse response = Utils.sendInstantiateProposalReturnFaulureResponse("javacc1", instantiateProposalRequest, myChannel, myChannel.getPeers(), myChannel.getOrderers());

        assertThat(response.getMessage(), containsString("Not build.gralde nor pom.xml found in chaincode source, don't know how to build chaincode"));

        assertThat(response.getMessage(), containsString("/chaincode/input/src/src/main"));
    }

    @Test
    public void TestNoBuildChaincodeInstallInstantiateWithoutSrc() throws Exception {

        final CryptoSuite crypto = CryptoSuite.Factory.getCryptoSuite();

        // Create client and set default crypto suite
        System.out.println("Creating client");
        final HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(crypto);

        client.setUserContext(Utils.getAdminUser());

        Channel myChannel = Utils.getMyChannelBasicNetwork(client);

        InstallProposalRequest installProposalRequest = generateNoBuildInstallRequest(client, "javacc2", false);
        Utils.sendInstallProposals(client, installProposalRequest, myChannel.getPeers());

        // Instantiating chaincode
        InstantiateProposalRequest instantiateProposalRequest = generateNoBuildInstantiateRequest(client, "javacc2");
        ProposalResponse response = Utils.sendInstantiateProposalReturnFaulureResponse("javacc2", instantiateProposalRequest, myChannel, myChannel.getPeers(), myChannel.getOrderers());

        assertThat(response.getMessage(), containsString("Not build.gralde nor pom.xml found in chaincode source, don't know how to build chaincode"));

        assertThat(response.getMessage(), containsString("/chaincode/input/src/main"));
    }

    static public InstallProposalRequest generateNoBuildInstallRequest(HFClient client, String name, boolean useSrcPrefix) throws IOException, InvalidArgumentException {
        return Utils.generateInstallRequest(client, name, "1.0", "src/test/resources/NoBuildCC", useSrcPrefix);
    }

    static public InstantiateProposalRequest generateNoBuildInstantiateRequest(HFClient client, String name) throws InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, ChaincodeCollectionConfigurationException {
        return Utils.generateInstantiateRequest(client, name, "1.0", "src/test/resources/chaincodeendorsementpolicy.yaml", null, "init", "a", "100");
    }

}