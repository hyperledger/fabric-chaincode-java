/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration;

import com.google.protobuf.ByteString;
import org.hamcrest.Matchers;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

public class SBECCIntegrationTest {

    @ClassRule
    public static DockerComposeContainer env = new DockerComposeContainer(
            new File("src/test/resources/first-network/docker-compose-cli.yaml")
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
    public void testSBECCFirstNetwork() throws IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, TransactionException, ProposalException, ChaincodeEndorsementPolicyParseException, ChaincodeCollectionConfigurationException {
        final CryptoSuite crypto = CryptoSuite.Factory.getCryptoSuite();

        // Create client and set default crypto suite
        System.out.println("Creating client");
        final HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(crypto);

        client.setUserContext(Utils.getAdminUserOrg1TLS());

        Channel myChannel = Utils.getMyChannelFirstNetwork(client);

        System.out.println("Installing chaincode fabric-chaincode-example-sacc, packaged as gzip stream");
        InstallProposalRequest installProposalRequest = generateSBECCInstallRequest(client);
        Utils.sendInstallProposals(client, installProposalRequest,  myChannel.getPeers().stream().filter(peer -> peer.getName().indexOf("org1") != -1).collect(Collectors.toList()));

        client.setUserContext(Utils.getAdminUserOrg2TLS());
        installProposalRequest = generateSBECCInstallRequest(client);
        Utils.sendInstallProposals(client, installProposalRequest,  myChannel.getPeers().stream().filter(peer -> peer.getName().indexOf("org2") != -1).collect(Collectors.toList()));

        InstantiateProposalRequest instantiateProposal = generateSBECCInstantiateRequest(client);
        Utils.sendInstantiateProposal("sbecc", instantiateProposal, myChannel, myChannel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), myChannel.getOrderers());

        RunSBE(client, myChannel, "pub");
        RunSBE(client, myChannel, "priv");

    }

    void RunSBE(HFClient client, Channel channel,  String mode) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, ProposalException, InvalidArgumentException {
        client.setUserContext(Utils.getUser1Org1TLS());
        TransactionProposalRequest proposal = generateSBECCTransactionRequest(client, "setval", mode, "foo");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), channel.getOrderers());

        proposal = generateSBECCTransactionRequest(client, "getval", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.is(ByteString.copyFrom("foo", StandardCharsets.UTF_8)));

        proposal = generateSBECCTransactionRequest(client, "addorgs", mode, "Org1MSP");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), channel.getOrderers());

        proposal = generateSBECCTransactionRequest(client, "listorgs", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.is(ByteString.copyFrom("[\"Org1MSP\"]", StandardCharsets.UTF_8)));

        proposal = generateSBECCTransactionRequest(client, "setval", mode, "val1");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), channel.getOrderers());

        proposal = generateSBECCTransactionRequest(client, "getval", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.is(ByteString.copyFrom("val1", StandardCharsets.UTF_8)));

        client.setUserContext(Utils.getUser1Org2TLS());
        proposal = generateSBECCTransactionRequest(client, "setval", mode, "val2");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), channel.getOrderers(), true);

        proposal = generateSBECCTransactionRequest(client, "getval", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.is(ByteString.copyFrom("val1", StandardCharsets.UTF_8)));

        client.setUserContext(Utils.getUser1Org1TLS());
        proposal = generateSBECCTransactionRequest(client, "addorgs", mode, "Org2MSP");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), channel.getOrderers());

        proposal = generateSBECCTransactionRequest(client, "listorgs", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.anyOf(Matchers.is(ByteString.copyFrom("[\"Org1MSP\",\"Org2MSP\"]", StandardCharsets.UTF_8)),Matchers.is(ByteString.copyFrom("[\"Org2MSP\",\"Org1MSP\"]", StandardCharsets.UTF_8))));

        client.setUserContext(Utils.getUser1Org2TLS());
        proposal = generateSBECCTransactionRequest(client, "setval", mode, "val3");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), channel.getOrderers(), true);

        proposal = generateSBECCTransactionRequest(client, "getval", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.is(ByteString.copyFrom("val1", StandardCharsets.UTF_8)));

        proposal = generateSBECCTransactionRequest(client, "setval", mode, "val4");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0") != -1).collect(Collectors.toList()), channel.getOrderers());

        client.setUserContext(Utils.getUser1Org1TLS());
        proposal = generateSBECCTransactionRequest(client, "getval", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org1") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.is(ByteString.copyFrom("val4", StandardCharsets.UTF_8)));

        client.setUserContext(Utils.getUser1Org2TLS());
        proposal = generateSBECCTransactionRequest(client, "delorgs", mode, "Org1MSP");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), channel.getOrderers(), true);

        proposal = generateSBECCTransactionRequest(client, "listorgs", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.anyOf(Matchers.is(ByteString.copyFrom("[\"Org1MSP\",\"Org2MSP\"]", StandardCharsets.UTF_8)),Matchers.is(ByteString.copyFrom("[\"Org2MSP\",\"Org1MSP\"]", StandardCharsets.UTF_8))));

        proposal = generateSBECCTransactionRequest(client, "delorgs", mode, "Org1MSP");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0") != -1).collect(Collectors.toList()), channel.getOrderers());

        proposal = generateSBECCTransactionRequest(client, "listorgs", mode);
        Utils.sendTransactionProposalQuery(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().indexOf("peer0.org2") != -1).collect(Collectors.toList()), Matchers.is(200), Matchers.anything(), Matchers.is(ByteString.copyFrom("[\"Org2MSP\"]", StandardCharsets.UTF_8)));

    }

//    private InstallProposalRequest generateSACCInstallRequest(HFClient client) throws IOException, InvalidArgumentException {
//        return Utils.generateInstallRequest(client, "javacc", "1.0", "../fabric-chaincode-example-sacc");
//    }
//
//    static public InstantiateProposalRequest generateSACCInstantiateRequest(HFClient client, String... args) throws InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException {
//        return Utils.generateInstantiateRequest(client, "javacc", "1.0", "src/test/resources/chaincodeendorsementpolicy.yaml", "init", args);
//    }
//
//    static public TransactionProposalRequest generateSACCTransactionRequest(HFClient client, String... args) {
//        return Utils.generateTransactionRequest(client, "javacc", "1.0", "set", args);
//    }
//
    private InstallProposalRequest generateSBECCInstallRequest(HFClient client) throws IOException, InvalidArgumentException {
        return Utils.generateInstallRequest(client, "sbecc", "1.0", "../fabric-chaincode-example-sbe");
    }

    static public InstantiateProposalRequest generateSBECCInstantiateRequest(HFClient client) throws InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, ChaincodeCollectionConfigurationException {
            return Utils.generateInstantiateRequest(client, "sbecc", "1.0", "src/test/resources/chaincodeendorsementpolicy_2orgs.yaml", "src/test/resources/collection_config.yaml", "init", new String[]{});
    }

    static public TransactionProposalRequest generateSBECCTransactionRequest(HFClient client, String func, String... args) {
        return Utils.generateTransactionRequest(client, "sbecc", "1.0", func, args);
    }
}
