package org.hyperleder.fabric.shim.integration;

import com.github.dockerjava.api.exception.ConflictException;
import com.google.protobuf.ByteString;
import org.hamcrest.Matchers;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.shim.Chaincode;
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

public class MavenSimpleChaincodeIntegrationTest {

    private static final String CC_NAME = "SimpleChaincode";
    private static final String CC_VERSION = "1.0";

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

        try {
            Utils.removeDevContainerAndImages();
        } catch (ConflictException e) {
            //not relevant
        }
    }

    @Test
    public void testSimpelChaincodeFirstNetwork() throws IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, TransactionException, ProposalException, ChaincodeEndorsementPolicyParseException, ChaincodeCollectionConfigurationException {
        final CryptoSuite crypto = CryptoSuite.Factory.getCryptoSuite();

        // Create client and set default crypto suite
        System.out.println("Creating client");
        final HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(crypto);

        client.setUserContext(Utils.getAdminUserOrg1TLS());

        Channel myChannel = Utils.getMyChannelFirstNetwork(client);

        System.out.println("Installing chaincode SimpleChaincode, packaged as gzip stream");
        InstallProposalRequest installProposalRequest = generateSimpleChaincodeInstallRequest(client);
        Utils.sendInstallProposals(client, installProposalRequest, myChannel.getPeers().stream().filter(peer -> peer.getName().contains("org1")).collect(Collectors.toList()));

        client.setUserContext(Utils.getAdminUserOrg2TLS());
        installProposalRequest = generateSimpleChaincodeInstallRequest(client);
        Utils.sendInstallProposals(client, installProposalRequest, myChannel.getPeers().stream().filter(peer -> peer.getName().contains("org2")).collect(Collectors.toList()));

        InstantiateProposalRequest instantiateProposal = generateSimpleChaincodeInstantiateRequest(client);
        Utils.sendInstantiateProposal(CC_NAME, instantiateProposal, myChannel, myChannel.getPeers().stream().filter(peer -> peer.getName().contains("peer0.org2")).collect(Collectors.toList()), myChannel.getOrderers());

        runTransfer(client, myChannel);
    }

    void runTransfer(HFClient client, Channel channel) throws
            NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, ProposalException, InvalidArgumentException {
        client.setUserContext(Utils.getUser1Org1TLS());
        TransactionProposalRequest proposal = generateSimpleChaincodeInvokeRequest(client, "a", "b", "10");
        Utils.sendTransactionProposalInvoke(proposal, channel, channel.getPeers().stream().filter(peer -> peer.getName().contains("peer0.org1")).collect(Collectors.toList()), channel.getOrderers());

        executeAndValidateQueryOnAccount(client, channel, "a", "peer0.org1", "90");
        executeAndValidateQueryOnAccount(client, channel, "b", "peer0.org1", "210");
        executeAndValidateQueryOnAccount(client, channel, "a", "peer0.org2", "90");
        executeAndValidateQueryOnAccount(client, channel, "b", "peer0.org2", "210");
    }

    private void executeAndValidateQueryOnAccount(HFClient client, Channel channel, String keyAccount, String
            peerName, String expectedAmount) throws ProposalException, InvalidArgumentException {
        TransactionProposalRequest proposal = generateSimpleChaincodeQueryRequest(client, keyAccount);
        Utils.sendTransactionProposalQuery(
                proposal,
                channel,
                channel.getPeers()
                        .stream()
                        .filter(peer -> peer.getName().contains(peerName))
                        .collect(Collectors.toList()),
                Matchers.is(Chaincode.Response.Status.SUCCESS.getCode()),
                Matchers.anything(),
                Matchers.is(ByteString.copyFrom(expectedAmount, StandardCharsets.UTF_8))
        );
    }

    private InstallProposalRequest generateSimpleChaincodeInstallRequest(HFClient client) throws
            IOException, InvalidArgumentException {
        return Utils.generateInstallRequest(client, CC_NAME, CC_VERSION, "../fabric-chaincode-example-maven");
    }

    static public InstantiateProposalRequest generateSimpleChaincodeInstantiateRequest(HFClient client) throws
            InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, ChaincodeCollectionConfigurationException {
        return Utils.generateInstantiateRequest(
                client,
                CC_NAME,
                CC_VERSION,
                "src/test/resources/chaincodeendorsementpolicy_2orgs.yaml",
                "src/test/resources/collection_config.yaml",
                "init", "a", "100", "b", "200");
    }

    static public TransactionProposalRequest generateSimpleChaincodeTransactionRequest(HFClient client, String
            func, String... args) {
        return Utils.generateTransactionRequest(client, CC_NAME, CC_VERSION, func, args);
    }

    static public TransactionProposalRequest generateSimpleChaincodeInvokeRequest(HFClient client, String
            from, String to, String amount) {
        return Utils.generateTransactionRequest(client, CC_NAME, CC_VERSION, "invoke", from, to, amount);
    }

    static public TransactionProposalRequest generateSimpleChaincodeQueryRequest(HFClient client, String key) {
        return Utils.generateTransactionRequest(client, CC_NAME, CC_VERSION, "query", key);
    }
}
