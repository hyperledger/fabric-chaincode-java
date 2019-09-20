/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import static java.lang.String.format;
import static java.util.logging.Level.ALL;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.shim.impl.ChaincodeSupportStream;
import org.hyperledger.fabric.shim.impl.Handler;

import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;

public abstract class ChaincodeBase implements Chaincode {

    public static final String CORE_CHAINCODE_LOGGING_SHIM = "CORE_CHAINCODE_LOGGING_SHIM";
    public static final String CORE_CHAINCODE_LOGGING_LEVEL = "CORE_CHAINCODE_LOGGING_LEVEL";

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

    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";
    private static final String CORE_PEER_ADDRESS = "CORE_PEER_ADDRESS";
    private static final String CORE_PEER_TLS_ENABLED = "CORE_PEER_TLS_ENABLED";
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
        try {
            processEnvironmentOptions();
            processCommandLineOptions(args);
            initializeLogging();
            validateOptions();
            connectToPeer();
        } catch (Exception e) {
            logger.fatal("Chaincode could not start", e);
        }
    }

    protected void connectToPeer() throws IOException  {
        final ChaincodeID chaincodeId = ChaincodeID.newBuilder().setName(this.id).build();
        final ManagedChannelBuilder<?> channelBuilder = newChannelBuilder();
        final Handler handler = new Handler(chaincodeId, this);
        new ChaincodeSupportStream(channelBuilder, handler::onChaincodeMessage, handler::nextOutboundChaincodeMessage);
    }


    protected void initializeLogging() {
        System.setProperty("java.util.logging.SimpleFormatter.format","%1$tH:%1$tM:%1$tS:%1$tL %4$-7.7s %2$-80.80s %5$s%6$s%n");
        final Logger rootLogger = Logger.getLogger("");

        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(ALL);
            handler.setFormatter(new SimpleFormatter() {

                @Override
                public synchronized String format(LogRecord record) {
                    return super.format(record)
                            .replaceFirst(".*SEVERE\\s*\\S*\\s*\\S*", "\u001B[1;31m$0\u001B[0m")
                            .replaceFirst(".*WARNING\\s*\\S*\\s*\\S*", "\u001B[1;33m$0\u001B[0m")
                            .replaceFirst(".*CONFIG\\s*\\S*\\s*\\S*", "\u001B[35m$0\u001B[0m")
                            .replaceFirst(".*FINE\\s*\\S*\\s*\\S*", "\u001B[36m$0\u001B[0m")
                            .replaceFirst(".*FINER\\s*\\S*\\s*\\S*", "\u001B[36m$0\u001B[0m")
                            .replaceFirst(".*FINEST\\s*\\S*\\s*\\S*", "\u001B[36m$0\u001B[0m");
               }

            });
        }



        rootLogger.info("Updated all handlers the format");
        // set logging level of chaincode logger
        Level chaincodeLogLevel = mapLevel(System.getenv(CORE_CHAINCODE_LOGGING_LEVEL));

        Package chaincodePackage = this.getClass().getPackage();
        if (chaincodePackage != null) {
            Logger.getLogger(chaincodePackage.getName()).setLevel(chaincodeLogLevel);
        } else {
            // If chaincode declared without package, i.e. default package, lets set level to root logger
            // Chaincode should never be declared without package
            Logger.getLogger("").setLevel(chaincodeLogLevel);
        }

        // set logging level of shim logger
        Level shimLogLevel = mapLevel(System.getenv(CORE_CHAINCODE_LOGGING_SHIM));
        Logger.getLogger(ChaincodeBase.class.getPackage().getName()).setLevel(shimLogLevel);
        Logger.getLogger(ContractRouter.class.getPackage().getName()).setLevel(chaincodeLogLevel);

        List<?> loggers = Collections.list(LogManager.getLogManager().getLoggerNames());
        loggers.forEach(x -> {
        	Logger l = LogManager.getLogManager().getLogger((String) x);
        	//TODO:  err what is the code supposed to do?
        });

    }

    private Level mapLevel(String level) {

        if (level != null) {
            switch (level.toUpperCase().trim()) {
                case "CRITICAL":
                case "ERROR":
                    return Level.SEVERE;
                case "WARNING":
                    return Level.WARNING;
                case "INFO":
                    return Level.INFO;
                case "NOTICE":
                    return Level.CONFIG;
                case "DEBUG":
                    return Level.FINEST;
            }
        }
        return Level.INFO;
    }

    protected void validateOptions() {
        if (this.id == null) {
            throw new IllegalArgumentException(format("The chaincode id must be specified using either the -i or --i command line options or the %s environment variable.", CORE_CHAINCODE_ID_NAME));
        }
        if (this.tlsEnabled) {
            if (tlsClientCertPath == null) {
                throw new IllegalArgumentException(format("Client key certificate chain (%s) was not specified.", ENV_TLS_CLIENT_CERT_PATH));
            }
            if (tlsClientKeyPath == null) {
                throw new IllegalArgumentException(format("Client key (%s) was not specified.", ENV_TLS_CLIENT_KEY_PATH));
            }
            if (tlsClientRootCertPath == null) {
                throw new IllegalArgumentException(format("Peer certificate trust store (%s) was not specified.", CORE_PEER_TLS_ROOTCERT_FILE));
            }
        }
    }

    protected void processCommandLineOptions(String[] args) {
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
        logger.info("CORE_PEER_TLS_ROOTCERT_FILE: " + this.tlsClientRootCertPath);
        logger.info("CORE_TLS_CLIENT_KEY_PATH: " + this.tlsClientKeyPath);
        logger.info("CORE_TLS_CLIENT_CERT_PATH: " + this.tlsClientCertPath);
    }

    protected void processEnvironmentOptions() {


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
        logger.info("CORE_PEER_TLS_ROOTCERT_FILE: " + this.tlsClientRootCertPath);
        logger.info("CORE_TLS_CLIENT_KEY_PATH: " + this.tlsClientKeyPath);
        logger.info("CORE_TLS_CLIENT_CERT_PATH: " + this.tlsClientCertPath);
    }

    ManagedChannelBuilder<?> newChannelBuilder() throws IOException {
        final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
        logger.info("Configuring channel connection to peer.");

        if (tlsEnabled) {
            builder.negotiationType(NegotiationType.TLS);
            builder.sslContext(createSSLContext());
        } else {
            builder.usePlaintext();
        }
        return builder;
    }

    SslContext createSSLContext() throws IOException {
        byte[] ckb = Files.readAllBytes(Paths.get(this.tlsClientKeyPath));
        byte[] ccb = Files.readAllBytes(Paths.get(this.tlsClientCertPath));

        return GrpcSslContexts.forClient()
                .trustManager(new File(this.tlsClientRootCertPath))
                .keyManager(
                        new ByteArrayInputStream(Base64.getDecoder().decode(ccb)),
                        new ByteArrayInputStream(Base64.getDecoder().decode(ckb)))
                .build();
    }

    @Deprecated
    protected static Response newSuccessResponse(String message, byte[] payload) {
        return ResponseUtils.newSuccessResponse(message, payload);
    }

    @Deprecated
    protected static Response newSuccessResponse() {
        return ResponseUtils.newSuccessResponse();
    }

    @Deprecated
    protected static Response newSuccessResponse(String message) {
        return ResponseUtils.newSuccessResponse(message);
    }

    @Deprecated
    protected static Response newSuccessResponse(byte[] payload) {
        return ResponseUtils.newSuccessResponse(payload);
    }

    @Deprecated
    protected static Response newErrorResponse(String message, byte[] payload) {
        return ResponseUtils.newErrorResponse(message, payload);
    }

    @Deprecated
    protected static Response newErrorResponse() {
        return ResponseUtils.newErrorResponse();
    }

    @Deprecated
    protected static Response newErrorResponse(String message) {
        return ResponseUtils.newErrorResponse(message);
    }

    @Deprecated
    protected static Response newErrorResponse(byte[] payload) {
        return ResponseUtils.newErrorResponse(payload);
    }

    @Deprecated
    protected static Response newErrorResponse(Throwable throwable) {
        return ResponseUtils.newErrorResponse(throwable);
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    boolean isTlsEnabled() {
        return tlsEnabled;
    }

    String getTlsClientKeyPath() {
        return tlsClientKeyPath;
    }

    String getTlsClientCertPath() {
        return tlsClientCertPath;
    }

    String getTlsClientRootCertPath() {
        return tlsClientRootCertPath;
    }

    String getId() {
        return id;
    }
}
