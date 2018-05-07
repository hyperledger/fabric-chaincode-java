/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type;
import org.hyperledger.fabric.shim.impl.ChatStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Base64;

import static org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS;

public abstract class ChaincodeBase implements Chaincode {

    @Override
    public abstract Response init(ChaincodeStub stub);

    @Override
    public abstract Response invoke(ChaincodeStub stub);

    private static Log logger = LogFactory.getLog(ChaincodeBase.class);

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 7051;

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private boolean tlsEnabled = false;
    private String tlsClientKeyPath;
    private String tlsClientCertPath;
    private String tlsClientRootCertPath;

    private String id;

    private final static String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";
    private final static String CORE_PEER_ADDRESS = "CORE_PEER_ADDRESS";
    private final static String CORE_PEER_TLS_ENABLED = "CORE_PEER_TLS_ENABLED";
    private static final String CORE_PEER_TLS_ROOTCERT_FILE = "CORE_PEER_TLS_ROOTCERT_FILE";
    private static final String ENV_TLS_CLIENT_KEY_PATH = "CORE_TLS_CLIENT_KEY_PATH";
    private static final String ENV_TLS_CLIENT_CERT_PATH = "CORE_TLS_CLIENT_CERT_PATH";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Start chaincode
     *
     * @param args command line arguments
     */
    public void start(String[] args) {
        processEnvironmentOptions();
        processCommandLineOptions(args);
        try {
            validateOptions();
            new Thread(() -> {
                logger.trace("chaincode started");
                final ManagedChannel connection = newPeerClientConnection();
                logger.trace("connection created");
                chatWithPeer(connection);
                logger.trace("chatWithPeer DONE");
            }).start();
        } catch (IllegalArgumentException e) {
            logger.fatal("Chaincode could not start", e);
        }
    }

    private void validateOptions() {
        if (this.id == null) {
            throw new IllegalArgumentException(String.format("The chaincode id must be specified using either the -i or --i command line options or the %s environment variable.", CORE_CHAINCODE_ID_NAME));
        }
        if (this.tlsEnabled) {
            if (tlsClientCertPath == null) {
                throw new IllegalArgumentException(String.format("Client key certificate chain (%s) was not specified.", ENV_TLS_CLIENT_CERT_PATH));
            }
            if (tlsClientKeyPath == null) {
                throw new IllegalArgumentException(String.format("Client key (%s) was not specified.", ENV_TLS_CLIENT_KEY_PATH));
            }
            if (tlsClientRootCertPath == null) {
                throw new IllegalArgumentException(String.format("Peer certificate trust store (%s) was not specified.", CORE_PEER_TLS_ROOTCERT_FILE));
            }
        }
    }

    private void processCommandLineOptions(String[] args) {
        Options options = new Options();
        options.addOption("a", "peer.address", true, "Address of peer to connect to");
        options.addOption(null, "peerAddress", true, "Address of peer to connect to");
        options.addOption("i", "id", true, "Identity of chaincode");

        try {
            CommandLine cl = new DefaultParser().parse(options, args);
            if (cl.hasOption("peerAddress") || cl.hasOption('a')) {
                String hostAddrStr;
                if (cl.hasOption('a')) {
                    hostAddrStr = cl.getOptionValue('a');
                } else {
                    hostAddrStr = cl.getOptionValue("peerAddress");
                }
                String[] hostArr = hostAddrStr.split(":");
                if (hostArr.length == 2) {
                    port = Integer.valueOf(hostArr[1].trim());
                    host = hostArr[0].trim();
                } else {
                    String msg = String.format("peer address argument should be in host:port format, current %s in wrong", hostAddrStr);
                    logger.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
            if (cl.hasOption('i')) {
                id = cl.getOptionValue('i');
            }
        } catch (Exception e) {
            logger.warn("cli parsing failed with exception", e);

        }

        logger.info("<<<<<<<<<<<<<CommandLine options>>>>>>>>>>>>");
        logger.info("CORE_CHAINCODE_ID_NAME: " + this.id);
        logger.info("CORE_PEER_ADDRESS: " + this.host + ":" + this.port);
        logger.info("CORE_PEER_TLS_ENABLED: " + this.tlsEnabled);
        logger.info("CORE_PEER_TLS_ROOTCERT_FILE" + this.tlsClientRootCertPath);
        logger.info("CORE_TLS_CLIENT_KEY_PATH" + this.tlsClientKeyPath);
        logger.info("CORE_TLS_CLIENT_CERT_PATH" + this.tlsClientCertPath);
    }

    private void processEnvironmentOptions() {
        if (System.getenv().containsKey(CORE_CHAINCODE_ID_NAME)) {
            this.id = System.getenv(CORE_CHAINCODE_ID_NAME);
        }
        if (System.getenv().containsKey(CORE_PEER_ADDRESS)) {
            String[] hostArr = System.getenv(CORE_PEER_ADDRESS).split(":");
            if (hostArr.length == 2) {
                this.port = Integer.valueOf(hostArr[1].trim());
                this.host = hostArr[0].trim();
            } else {
                String msg = String.format("peer address argument should be in host:port format, ignoring current %s", System.getenv(CORE_PEER_ADDRESS));
                logger.error(msg);
            }
        }
        this.tlsEnabled = Boolean.parseBoolean(System.getenv(CORE_PEER_TLS_ENABLED));
        if (this.tlsEnabled) {
            this.tlsClientRootCertPath = System.getenv(CORE_PEER_TLS_ROOTCERT_FILE);
            this.tlsClientKeyPath = System.getenv(ENV_TLS_CLIENT_KEY_PATH);
            this.tlsClientCertPath = System.getenv(ENV_TLS_CLIENT_CERT_PATH);
        }

        logger.info("<<<<<<<<<<<<<Enviromental options>>>>>>>>>>>>");
        logger.info("CORE_CHAINCODE_ID_NAME: " + this.id);
        logger.info("CORE_PEER_ADDRESS: " + this.host);
        logger.info("CORE_PEER_TLS_ENABLED: " + this.tlsEnabled);
        logger.info("CORE_PEER_TLS_ROOTCERT_FILE" + this.tlsClientRootCertPath);
        logger.info("CORE_TLS_CLIENT_KEY_PATH" + this.tlsClientKeyPath);
        logger.info("CORE_TLS_CLIENT_CERT_PATH" + this.tlsClientCertPath);
    }

    public ManagedChannel newPeerClientConnection() {
        final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
        logger.info("Configuring channel connection to peer.");

        if (tlsEnabled) {
            logger.info("TLS is enabled");
            try {
                byte ckb[] = Files.readAllBytes(Paths.get(this.tlsClientKeyPath));
                byte ccb[] = Files.readAllBytes(Paths.get(this.tlsClientCertPath));


                final SslContext sslContext = GrpcSslContexts.forClient()
                        .trustManager(new File(this.tlsClientRootCertPath))
                        .keyManager(
                                new ByteArrayInputStream(Base64.getDecoder().decode(ccb)),
                                new ByteArrayInputStream(Base64.getDecoder().decode(ckb)))
                        .build();
                builder.negotiationType(NegotiationType.TLS);
                builder.sslContext(sslContext);
                logger.info("TLS context built: " + sslContext);
            } catch (IOException e) {
                logger.error("failed connect to peer with IOException", e);
            }
        } else {
            builder.usePlaintext(true);
        }
        return builder.build();
    }

    public void chatWithPeer(ManagedChannel connection) {
        ChatStream chatStream = new ChatStream(connection, this);

        // Send the ChaincodeID during register.
        ChaincodeID chaincodeID = ChaincodeID.newBuilder()
                .setName(id)
                .build();

        ChaincodeMessage payload = ChaincodeMessage.newBuilder()
                .setPayload(chaincodeID.toByteString())
                .setType(Type.REGISTER)
                .build();

        // Register on the stream
        logger.info(String.format("Registering as '%s' ... sending %s", id, Type.REGISTER));
        chatStream.serialSend(payload);

        while (true) {
            try {
                chatStream.receive();
            } catch (Exception e) {
                logger.error("Receiving message error", e);
                break;
            }
        }
    }

    protected static Response newSuccessResponse(String message, byte[] payload) {
        return new Response(SUCCESS, message, payload);
    }

    protected static Response newSuccessResponse() {
        return newSuccessResponse(null, null);
    }

    protected static Response newSuccessResponse(String message) {
        return newSuccessResponse(message, null);
    }

    protected static Response newSuccessResponse(byte[] payload) {
        return newSuccessResponse(null, payload);
    }

    protected static Response newErrorResponse(String message, byte[] payload) {
        return new Response(INTERNAL_SERVER_ERROR, message, payload);
    }

    protected static Response newErrorResponse() {
        return newErrorResponse(null, null);
    }

    protected static Response newErrorResponse(String message) {
        return newErrorResponse(message, null);
    }

    protected static Response newErrorResponse(byte[] payload) {
        return newErrorResponse(null, payload);
    }

    protected static Response newErrorResponse(Throwable throwable) {
        return newErrorResponse(throwable.getMessage(), printStackTrace(throwable));
    }

    private static byte[] printStackTrace(Throwable throwable) {
        if (throwable == null) return null;
        final StringWriter buffer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(buffer));
        return buffer.toString().getBytes(StandardCharsets.UTF_8);
    }
}
