package org.hyperleder.fabric.shim.integration;

import org.hamcrest.Matchers;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeCollectionConfigurationException;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.io.IOException;


public class SACCIntegrationTest {
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

    @Test
    public void TestSACCChaincodeInstallInstantiateInvokeQuery() throws Exception {

        final CryptoSuite crypto = CryptoSuite.Factory.getCryptoSuite();

        // Create client and set default crypto suite
        System.out.println("Creating client");
        final HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(crypto);

        client.setUserContext(Utils.getAdminUser());

        Channel myChannel = Utils.getMyChannelBasicNetwork(client);

        InstallProposalRequest installProposalRequest = generateSACCInstallRequest(client);
        Utils.sendInstallProposals(client, installProposalRequest, myChannel.getPeers());

        // Instantiating chaincode
        InstantiateProposalRequest instantiateProposalRequest = generateSACCInstantiateRequest(client);
        Utils.sendInstantiateProposal("javacc", instantiateProposalRequest, myChannel, myChannel.getPeers(), myChannel.getOrderers());

        client.setUserContext(Utils.getUser1());

        final TransactionProposalRequest proposalRequest = generateSACCInvokeRequest(client, "b", "200");
        Utils.sendTransactionProposalInvoke(proposalRequest, myChannel, myChannel.getPeers(), myChannel.getOrderers());

        // Creating proposal for query
        final TransactionProposalRequest queryAProposalRequest = generateSACCQueryRequest(client, "a");
        Utils.sendTransactionProposalQuery(queryAProposalRequest, myChannel, myChannel.getPeers(), Matchers.is(200), Matchers.is("100"), null);

        // Creating proposal for query
        final TransactionProposalRequest queryBProposalRequest = generateSACCQueryRequest(client, "b");
        Utils.sendTransactionProposalQuery(queryBProposalRequest, myChannel, myChannel.getPeers(), Matchers.is(200), Matchers.is("200"), null);
    }

    static public InstallProposalRequest generateSACCInstallRequest(HFClient client) throws IOException, InvalidArgumentException {
        return Utils.generateInstallRequest(client, "javacc", "1.0", "../fabric-chaincode-example-sacc");
    }

    static public InstantiateProposalRequest generateSACCInstantiateRequest(HFClient client) throws InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, ChaincodeCollectionConfigurationException {
        return Utils.generateInstantiateRequest(client, "javacc", "1.0", "src/test/resources/chaincodeendorsementpolicy.yaml", null, "init", "a", "100");
    }

    static public TransactionProposalRequest generateSACCInvokeRequest(HFClient client, String key, String value) {
        return Utils.generateTransactionRequest(client, "javacc", "1.0", "set", key, value);
    }

    static public TransactionProposalRequest generateSACCQueryRequest(HFClient client, String key) {
        return Utils.generateTransactionRequest(client, "javacc", "1.0", "get", key);
    }

}
