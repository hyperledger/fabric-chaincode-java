/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.testcontainers.DockerClientFactory;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class Utils {

    static public User getUser(String name, String mspId, File privateKeyFile, File certificateFile)
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

        try {

            final String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");

            final PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));

            User user = new User() {

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public Set<String> getRoles() {
                    return null;
                }

                @Override
                public String getAccount() {
                    return null;
                }

                @Override
                public String getAffiliation() {
                    return null;
                }

                @Override
                public Enrollment getEnrollment() {
                    return new Enrollment() {

                        @Override
                        public PrivateKey getKey() {
                            return privateKey;
                        }

                        @Override
                        public String getCert() {
                            return certificate;
                        }
                    };
                }

                @Override
                public String getMspId() {
                    return mspId;
                }

            };

            return user;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw e;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw e;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw e;
        }

    }

    static PrivateKey getPrivateKeyFromBytes(byte[] data)
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        final Reader pemReader = new StringReader(new String(data));

        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }

        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getPrivateKey(pemPair);

        return privateKey;
    }

    public static InputStream generateTarGzInputStream(File src, String pathPrefix) throws IOException {
        File sourceDirectory = src;

        ByteArrayOutputStream bos = new ByteArrayOutputStream(500000);

        String sourcePath = sourceDirectory.getAbsolutePath();

        TarArchiveOutputStream archiveOutputStream = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(bos)));
        archiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        try {
            Collection<File> childrenFiles = org.apache.commons.io.FileUtils.listFiles(sourceDirectory, null, true);

            ArchiveEntry archiveEntry;
            FileInputStream fileInputStream;
            for (File childFile : childrenFiles) {
                String childPath = childFile.getAbsolutePath();
                String relativePath = childPath.substring((sourcePath.length() + 1), childPath.length());

                if (pathPrefix != null) {
                    relativePath = org.hyperledger.fabric.sdk.helper.Utils.combinePaths(pathPrefix, relativePath);
                }

                relativePath = FilenameUtils.separatorsToUnix(relativePath);

                archiveEntry = new TarArchiveEntry(childFile, relativePath);
                fileInputStream = new FileInputStream(childFile);
                archiveOutputStream.putArchiveEntry(archiveEntry);

                try {
                    IOUtils.copy(fileInputStream, archiveOutputStream);
                } finally {
                    IOUtils.closeQuietly(fileInputStream);
                    archiveOutputStream.closeArchiveEntry();
                }
            }
        } finally {
            IOUtils.closeQuietly(archiveOutputStream);
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }

    public static void runWithTimeout(Runnable callable, long timeout, TimeUnit timeUnit) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                callable.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            executor.execute(t);
            if (!latch.await(timeout, timeUnit)) {
                throw new TimeoutException();
            }
        } finally {
            executor.shutdown();
            t.interrupt();
        }
    }


    static public void waitForCliContainerExecution() throws InterruptedException {
        for (int i = 0; i < 60; i++) {
            AtomicBoolean foundCliContainer = new AtomicBoolean(false);
            List<Container> containers = DockerClientFactory.instance().client().listContainersCmd().withShowAll(true).exec();
            containers.forEach(container -> {
                for (String name : container.getNames()) {
                    if (name.indexOf("cli") != -1) {
                        if (container.getStatus().indexOf("Exited (0)") != -1) {
                            foundCliContainer.getAndSet(true);
                            break;
                        }
                    }
                }
            });
            if (foundCliContainer.get()) {
                return;
            }
            TimeUnit.SECONDS.sleep(10);
        }
    }

    public static void setUp() throws Exception {
        try {
            runWithTimeout(new Thread(() -> {
                try {
                    waitForCliContainerExecution();
                } catch (InterruptedException e) {

                }
                return;
            }
            ), 300, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Got timeout, while waiting for cli execution");
        }

        Security.addProvider(new BouncyCastleProvider());
    }


    static public InstantiateProposalRequest generateInstantiateRequest(HFClient client, String chaincode, String version, String policyLocation, String collectionLocation, String function, String... args) throws InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, ChaincodeCollectionConfigurationException {
        System.out.println("Instantiating chaincode: " + function + "(" + String.join(", ", args) + ")");
        final ChaincodeID chaincodeID = ChaincodeID.newBuilder()
                .setName(chaincode)
                .setVersion(version)
                .build();

        // Building proposal
        System.out.println("Building instantiate proposal");
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setProposalWaitTime(120000);
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setFcn(function);
        instantiateProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
        instantiateProposalRequest.setArgs(args);
        if (collectionLocation != null) {
            instantiateProposalRequest.setChaincodeCollectionConfiguration(ChaincodeCollectionConfiguration.fromYamlFile(new File(collectionLocation)));
        }
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(tm);

        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(policyLocation));
        instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        return instantiateProposalRequest;
    }

    static public InstallProposalRequest generateInstallRequest(HFClient client, String chaincode, String version, String chaincodeLocation) throws IOException, InvalidArgumentException {
        return Utils.generateInstallRequest(client, chaincode, version, chaincodeLocation, true);
    }

    static public InstallProposalRequest generateInstallRequest(HFClient client, String chaincode, String version, String chaincodeLocation, boolean addSrcPrefix) throws IOException, InvalidArgumentException {
        System.out.println("Creating install proposal for " + chaincode + " located at: " + chaincodeLocation);
        final InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        final ChaincodeID chaincodeID = ChaincodeID.newBuilder()
                .setName(chaincode)
                .setVersion(version)
                .build();

        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
        installProposalRequest.setChaincodeInputStream(generateTarGzInputStream(new File(chaincodeLocation), addSrcPrefix ? "src" : null));
        installProposalRequest.setChaincodeVersion(version);

        return installProposalRequest;
    }

    static public TransactionProposalRequest generateTransactionRequest(HFClient client, String chaincode, String version, String function, String... args) {
        System.out.println("Creating proposal for " + function + "(" + String.join(", ", args) + ")");
        final TransactionProposalRequest proposalRequest = client.newTransactionProposalRequest();

        final ChaincodeID chaincodeID = ChaincodeID.newBuilder()
                .setName(chaincode)
                .setVersion(version)
                .build();

        proposalRequest.setChaincodeID(chaincodeID);
        proposalRequest.setFcn(function);
        proposalRequest.setProposalWaitTime(TimeUnit.SECONDS.toMillis(120));
        proposalRequest.setArgs(args);

        return proposalRequest;
    }

    static public void sendInstallProposals(HFClient client, InstallProposalRequest installProposalRequest, Collection<Peer> peers) throws InvalidArgumentException, ProposalException {
        System.out.println("Sending install to peers: " + String.join(", ", peers.stream().map(p -> p.getName()).collect(Collectors.toList())));
        Collection<ProposalResponse> installResponces = client.sendInstallProposal(installProposalRequest, peers);

        for (ProposalResponse response : installResponces) {
            if (response.getStatus() != ProposalResponse.Status.SUCCESS) {
                System.out.println("We have a problem, chaicode not installed: " + response.getMessage());
                fail("We have a problem, chaicode not installed: " + response.getMessage());
            }
        }

    }

    static public void sendInstantiateProposal(String chaincode, InstantiateProposalRequest proposal, Channel channel, Collection<Peer> peers, Collection<Orderer> orderers) throws ProposalException, InvalidArgumentException {
        // Sending proposal
        System.out.println("Sending instantiate proposal " + proposal.getFcn() + "(" + String.join(",", proposal.getArgs()) + ")" + " to peers: " + String.join(", ", peers.stream().map(p -> p.getName()).collect(Collectors.toList())));
        Collection<ProposalResponse> instantiationResponces = channel.sendInstantiationProposal(proposal, peers);
        if (instantiationResponces == null || instantiationResponces.isEmpty()) {
            System.out.println("We have a problem, no responses to instantiate request");
            fail("We have a problem, no responses to instantiate request");
        }
        for (ProposalResponse response : instantiationResponces) {
            if (response.getStatus() != ProposalResponse.Status.SUCCESS) {
                System.out.println("We have a problem, chaicode not instantiated: " + response.getMessage());
                fail("We have a problem, chaicode not instantiated: " + response.getMessage());
            }
        }

        // Sending result transaction to orderers
        System.out.println("Sending instantiate transaction to orderers");

        Channel.NOfEvents nofEvents = Channel.NOfEvents.createNofEvents();
        if (!peers.stream().filter(peer -> channel.getPeersOptions(peer).getPeerRoles().contains(Peer.PeerRole.EVENT_SOURCE)).collect(Collectors.toList()).isEmpty()) {
            nofEvents.addPeers(peers.stream().filter(peer -> channel.getPeersOptions(peer).getPeerRoles().contains(Peer.PeerRole.EVENT_SOURCE)).collect(Collectors.toList()));
        }

        CompletableFuture<BlockEvent.TransactionEvent> instantiateFuture = channel.sendTransaction(instantiationResponces,
                Channel.TransactionOptions.createTransactionOptions()
                        .orderers(orderers)
                        .shuffleOrders(false)
                        .nOfEvents(nofEvents));
        try {
            instantiateFuture.get(240000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println("We have problem waiting for transaction");
            fail("We have problem waiting for transaction send to orderers");
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        peers.forEach(peer -> {
            try {
                List<String> installedChaincodes = channel.queryInstantiatedChaincodes(peer).stream().map(ccInfo -> ccInfo.getName()).collect(Collectors.toList());
                assertThat("Peer " + peer.getName() + " doesn't have chaincode " + chaincode +  " installed and instantiated (" + installedChaincodes + ")", installedChaincodes, hasItem(chaincode));
            } catch (Exception e) {
                fail("Accessing instantiate chaincodes on peer " + peer.getName() + " resulted in exception " + e);
            }
        });
    }

    static public ProposalResponse sendInstantiateProposalReturnFaulureResponse(String chaincode, InstantiateProposalRequest proposal, Channel channel, Collection<Peer> peers, Collection<Orderer> orderers) throws ProposalException, InvalidArgumentException {
        // Sending proposal
        System.out.println("Sending instantiate to peers: " + String.join(", ", peers.stream().map(p -> p.getName()).collect(Collectors.toList())));
        Collection<ProposalResponse> instantiationResponces = channel.sendInstantiationProposal(proposal, peers);
        if (instantiationResponces == null || instantiationResponces.isEmpty()) {
            System.out.println("We have a problem, no responses to instantiate request");
            fail("We have a problem, no responses to instantiate request");
        }
        for (ProposalResponse response : instantiationResponces) {
            if (response.getStatus() != ProposalResponse.Status.SUCCESS) {
                return response;
            }
        }

        for (ProposalResponse response : instantiationResponces) {
            System.out.println("We have a problem, chaicode instantiated, although shouldn't: " + response.getMessage());
        }
        fail("We have a problem, chaicode instantiated, although shouldn't");
        return null;
    }

    static public void sendTransactionProposalInvoke(TransactionProposalRequest proposal, Channel channel, Collection<Peer> peers, Collection<Orderer> orderers) throws InvalidArgumentException, ProposalException {
        sendTransactionProposalInvoke(proposal, channel, peers, orderers, false);
    }

    static public void sendTransactionProposalInvoke(TransactionProposalRequest proposal, Channel channel, Collection<Peer> peers, Collection<Orderer> orderers, boolean ignoreFailure) throws InvalidArgumentException, ProposalException {
        // Send proposal and wait for responses
        System.out.println("Sending proposal for " + proposal.getFcn() + "(" + String.join(", ", proposal.getArgs()) + ") to peers: " + String.join(", ", peers.stream().map(p -> p.getName()).collect(Collectors.toList())));
        final Collection<ProposalResponse> responses = channel.sendTransactionProposal(proposal, peers);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Sending transaction to orderers
        System.out.println("Sending transaction to orderes");

        Channel.NOfEvents nofEvents = Channel.NOfEvents.createNofEvents();
        if (!peers.stream().filter(peer -> channel.getPeersOptions(peer).getPeerRoles().contains(Peer.PeerRole.EVENT_SOURCE)).collect(Collectors.toList()).isEmpty()) {
            nofEvents.addPeers(peers.stream().filter(peer -> channel.getPeersOptions(peer).getPeerRoles().contains(Peer.PeerRole.EVENT_SOURCE)).collect(Collectors.toList()));
        }

        CompletableFuture<BlockEvent.TransactionEvent> txFuture = channel.sendTransaction(responses,
                Channel.TransactionOptions.createTransactionOptions()
                        .orderers(orderers)
                        .shuffleOrders(false)
                        .nOfEvents(nofEvents));

        BlockEvent.TransactionEvent event = null;
        try {
            event = txFuture.get(50000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println("Exception " + e + " during wait");
            e.printStackTrace();
            if (!ignoreFailure) {
                fail("Exception " + e + " during wait");
            }
        }

        if (event == null) {
            System.out.println("Something wrong, event is null");
            if (!ignoreFailure) {
                fail("Something wrong, event is null");
            }
        }
    }

    static public void sendTransactionProposalQuery(TransactionProposalRequest proposal, Channel channel, Collection<Peer> peers, Matcher statusMatcher, Matcher messageMatcher, Matcher payloadMatcher) throws InvalidArgumentException, ProposalException {
        // Send proposal and wait for responses
        System.out.println("Sending proposal for " + proposal.getFcn() + "(" + String.join(", ", proposal.getArgs()) + ") to peers: " + String.join(", ", peers.stream().map(p -> p.getName()).collect(Collectors.toList())));
        final Collection<ProposalResponse> queryAResponses = channel.sendTransactionProposal(proposal, peers);

        for (ProposalResponse resp : queryAResponses) {
            System.out.println("Response from peer " + resp.getPeer().getName() + " is: " + resp.getProposalResponse().getResponse().getStatus() + ": " + resp.getProposalResponse().getResponse().getMessage() + ": " + resp.getProposalResponse().getResponse().getPayload().toStringUtf8());
            if (statusMatcher != null) {
                assertThat(resp.getProposalResponse().getResponse().getStatus(), statusMatcher);
                // Matchers.is(200)
            }
            if (messageMatcher != null) {
                assertThat(resp.getProposalResponse().getResponse().getMessage(), messageMatcher);
                // Matchers.is("100")
            }

            if (payloadMatcher != null) {
                assertThat(resp.getProposalResponse().getResponse().getPayload(), payloadMatcher);
            }
        }

    }


    static public User getAdminUserOrg1TLS() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        // Loading admin user
        System.out.println("Loading org1 admin from disk");

        File userPrivateKeyFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/23acbdca52b60346fb189be8846f5799b379fcd582bdde8230641ff2eb2ae883_sk");
        File userCertificateFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem");
        return getUser("org1admin", "Org1MSP", userPrivateKeyFile, userCertificateFile);
    }

    static public User getAdminUserOrg2TLS() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        // Loading admin user
        System.out.println("Loading org2 admin from disk");

        File userPrivateKeyFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/keystore/73b84d0e0bb0c31d6eb40d6054811b2c7764059e95831bb0ed160f79f3a3794e_sk");
        File userCertificateFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/signcerts/Admin@org2.example.com-cert.pem");
        return getUser("org2admin", "Org2MSP", userPrivateKeyFile, userCertificateFile);
    }

    static public User getUser1Org1TLS() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        // Loading admin user
        System.out.println("Loading org1 user1 from disk");

        File userPrivateKeyFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore/06598da2a5c8268d5e09ff090136c411392fe5af949354b05a5bd8041ec1f8a5_sk");
        File userCertificateFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem");
        return getUser("org1user1", "Org1MSP", userPrivateKeyFile, userCertificateFile);
    }

    static public User getUser1Org2TLS() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        // Loading admin user
        System.out.println("Loading org1 user1 from disk");

        File userPrivateKeyFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org2.example.com/users/User1@org2.example.com/msp/keystore/f0cbbe6fb24330132e15fd0f56296d497f9a450341b3d39a42d1d86ba3e285c4_sk");
        File userCertificateFile = new File("src/test/resources/first-network/crypto-config/peerOrganizations/org2.example.com/users/User1@org2.example.com/msp/signcerts/User1@org2.example.com-cert.pem");
        return getUser("org2user1", "Org2MSP", userPrivateKeyFile, userCertificateFile);
    }

    static public Channel getMyChannelFirstNetwork(HFClient client) throws InvalidArgumentException, TransactionException, IOException {
        // Accessing channel, should already exist
        System.out.println("Accessing channel");
        Channel myChannel = client.newChannel("mychannel");

        System.out.println("Setting channel configuration");
        final List<Peer> peers = new LinkedList<>();
        Properties peer01Properties = new Properties();
        peer01Properties.setProperty("pemFile", getPEMCertFromFile("src/test/resources/first-network/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/server.crt"));
        peer01Properties.setProperty("hostnameOverride", "peer0.org1.example.com");
        peer01Properties.setProperty("sslProvider", "openSSL");
        peer01Properties.setProperty("negotiationType", "TLS");
        peers.add(client.newPeer("peer0.org1.example.com", "grpcs://localhost:7051", peer01Properties));

        Properties peer11Properties = new Properties();
        peer11Properties.setProperty("pemFile", getPEMCertFromFile("src/test/resources/first-network/crypto-config/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls/server.crt"));
        peer11Properties.setProperty("hostnameOverride", "peer1.org1.example.com");
        peer11Properties.setProperty("sslProvider", "openSSL");
        peer11Properties.setProperty("negotiationType", "TLS");
        peers.add(client.newPeer("peer1.org1.example.com", "grpcs://localhost:8051", peer11Properties));

        Properties peer02Properties = new Properties();
        peer02Properties.setProperty("pemFile", getPEMCertFromFile("src/test/resources/first-network/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/server.crt"));
        peer02Properties.setProperty("hostnameOverride", "peer0.org2.example.com");
        peer02Properties.setProperty("sslProvider", "openSSL");
        peer02Properties.setProperty("negotiationType", "TLS");
        peers.add(client.newPeer("peer0.org2.example.com", "grpcs://localhost:9051", peer02Properties));

        Properties peer12Properties = new Properties();
        peer12Properties.setProperty("pemFile", getPEMCertFromFile("src/test/resources/first-network/crypto-config/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls/server.crt"));
        peer12Properties.setProperty("hostnameOverride", "peer1.org2.example.com");
        peer12Properties.setProperty("sslProvider", "openSSL");
        peer12Properties.setProperty("negotiationType", "TLS");
        peers.add(client.newPeer("peer1.org2.example.com", "grpcs://localhost:10051", peer12Properties));

        final List<Orderer> orderers = new LinkedList<>();
        Properties ordererProperties = new Properties();
        ordererProperties.setProperty("pemFile", getPEMCertFromFile("src/test/resources/first-network/crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt"));
        ordererProperties.setProperty("hostnameOverride", "orderer.example.com");
        ordererProperties.setProperty("sslProvider", "openSSL");
        ordererProperties.setProperty("negotiationType", "TLS");
        orderers.add(client.newOrderer("orderer.example.com", "grpcs://localhost:7050", ordererProperties));

        for (Orderer orderer : orderers) {
            myChannel.addOrderer(orderer);
        }

        for (Peer peer : peers) {
            myChannel.addPeer(peer);
        }
        myChannel.initialize();

        return myChannel;
    }

    private static String getPEMCertFromFile(String location) throws IOException {
        File f = new File(location);
        if (!f.exists()) {
            f = new File(Utils.class.getClassLoader().getResource(location).getFile());
            if (!f.exists()) {
                fail();
            }
        }

        return f.getCanonicalPath();
    }

    static public void removeDevContainerAndImages() throws Exception {
        List<Container> containers = DockerClientFactory.instance().client().listContainersCmd().withShowAll(true).exec();
        containers.forEach(container -> {
            for (String name : container.getNames()) {
                if (name.indexOf("dev-peer") != -1) {
                    if (DockerClientFactory.instance().client().inspectContainerCmd(container.getId()).exec().getState().getRunning()) {
                        DockerClientFactory.instance().client().killContainerCmd(container.getId()).exec();
                    }
                    break;
                }
            }
        });
        TimeUnit.SECONDS.sleep(10);
        containers = DockerClientFactory.instance().client().listContainersCmd().withShowAll(true).exec();
        containers.forEach(container -> {
            for (String name : container.getNames()) {
                if (name.indexOf("dev-peer") != -1) {
                    DockerClientFactory.instance().client().removeContainerCmd(container.getId()).exec();
                    break;
                }
            }
        });
        TimeUnit.SECONDS.sleep(10);
        List<Image> images = DockerClientFactory.instance().client().listImagesCmd().exec();

        images.forEach(image -> {
            String names[] = image.getRepoTags();
            if (names != null) {
                for (String name : names) {
                    if (name != null && name.indexOf("dev-peer") != -1) {
                        DockerClientFactory.instance().client().removeImageCmd(image.getId()).exec();
                    }
                }
            }
        });
        TimeUnit.SECONDS.sleep(10);
    }

    static public List<Peer> getPeersFromChannel(Channel ch, String filter) {
        return ch.getPeers().stream().filter(peer -> peer.getName().contains(filter)).collect(Collectors.toList());
    }


}
