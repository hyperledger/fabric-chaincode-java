/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

import static java.lang.String.format;
import static java.util.logging.Level.ALL;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.shim.impl.ChaincodeSupportClient;
import org.hyperledger.fabric.shim.impl.InvocationTaskManager;
import org.hyperledger.fabric.traces.Traces;

import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;

/**
 * Abstract implementation of {@link Chaincode}.
 *
 * <p>
 * All chaincode implementations must extend the abstract class
 * <code>ChaincodeBase</code>. It is possible to implement chaincode by
 * extending <code>ChaincodeBase</code> directly however new projects should
 * implement {@link org.hyperledger.fabric.contract.ContractInterface} and use
 * the contract programming model instead.
 *
 * @see org.hyperledger.fabric.contract
 */
public abstract class ChaincodeBase implements Chaincode {

    /**
     *
     */
    public static final String CORE_CHAINCODE_LOGGING_SHIM = "CORE_CHAINCODE_LOGGING_SHIM";

    /**
     *
     */
    public static final String CORE_CHAINCODE_LOGGING_LEVEL = "CORE_CHAINCODE_LOGGING_LEVEL";

    @Override
    public abstract Response init(ChaincodeStub stub);

    @Override
    public abstract Response invoke(ChaincodeStub stub);

    private static final Logger LOGGER = Logger.getLogger(ChaincodeBase.class.getName());

    /**
     *
     */
    public static final String DEFAULT_HOST = "127.0.0.1";

    /**
     *
     */
    public static final int DEFAULT_PORT = 7051;

    /**
     * Default to 100MB for maximum inbound grpc message size.
     */
    public static final String DEFAULT_MAX_INBOUND_MESSAGE_SIZE = "104857600";

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private boolean tlsEnabled = false;
    private String tlsClientKeyPath;
    private String tlsClientCertPath;
    private String tlsClientKeyFile;
    private String tlsClientCertFile;
    private String tlsClientRootCertPath;

    private String id;
    private String localMspId = "";
    private String chaincodeServerAddress = "";

    private static final String CHAINCODE_SERVER_ADDRESS = "CHAINCODE_SERVER_ADDRESS";
    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";
    private static final String CORE_PEER_ADDRESS = "CORE_PEER_ADDRESS";
    private static final String CORE_PEER_TLS_ENABLED = "CORE_PEER_TLS_ENABLED";
    private static final String CORE_PEER_TLS_ROOTCERT_FILE = "CORE_PEER_TLS_ROOTCERT_FILE";
    private static final String ENV_TLS_CLIENT_KEY_PATH = "CORE_TLS_CLIENT_KEY_PATH";
    private static final String ENV_TLS_CLIENT_CERT_PATH = "CORE_TLS_CLIENT_CERT_PATH";
    private static final String ENV_TLS_CLIENT_KEY_FILE = "CORE_TLS_CLIENT_KEY_FILE";
    private static final String ENV_TLS_CLIENT_CERT_FILE = "CORE_TLS_CLIENT_CERT_FILE";
    private static final String CORE_PEER_LOCALMSPID = "CORE_PEER_LOCALMSPID";
    private static final String MAX_INBOUND_MESSAGE_SIZE = "MAX_INBOUND_MESSAGE_SIZE";
    private Properties props;
    private Level logLevel;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private int getMaxInboundMessageSize() {
        if (this.props == null) {
            throw new IllegalStateException("Chaincode config not available");
        }
        final int maxMsgSize = Integer
                .parseInt(this.props.getProperty(MAX_INBOUND_MESSAGE_SIZE, DEFAULT_MAX_INBOUND_MESSAGE_SIZE));
        final String msgSizeInfo = String.format("Maximum Inbound Message Size [%s] = %d", MAX_INBOUND_MESSAGE_SIZE,
                maxMsgSize);
        LOGGER.info(msgSizeInfo);
        return maxMsgSize;
    }

    /**
     * Start chaincode.
     *
     * @param args command line arguments
     */

    public void start(final String[] args) {
        try {
            initializeLogging();
            processEnvironmentOptions();
            processCommandLineOptions(args);
            validateOptions();

            final Properties props = getChaincodeConfig();
            Metrics.initialize(props);
            Traces.initialize(props);
            connectToPeer();
        } catch (final Exception e) {
            LOGGER.severe(() -> "Chaincode could not start" + Logging.formatError(e));
        }
    }

    protected final void connectToPeer() throws IOException {

        // The ChaincodeSupport Client is a wrapper around the gRPC streams that
        // come from the single 'register' call that is made back to the peer
        //
        // Once this has been created, the InvocationTaskManager that is responsible
        // for the thread management can be created.
        //
        // This is then passed to the ChaincodeSupportClient to be connected to the
        // gRPC streams

        final ChaincodeID chaincodeId = ChaincodeID.newBuilder().setName(this.id).build();
        final ManagedChannelBuilder<?> channelBuilder = newChannelBuilder();
        final ChaincodeSupportClient chaincodeSupportClient = new ChaincodeSupportClient(channelBuilder);

        final InvocationTaskManager itm = InvocationTaskManager.getManager(this, chaincodeId);

        // This is a critical method - it is the one time that a
        // protobuf service is invoked. The single 'register' call
        // is made, and two streams are created.
        //
        // It is confusing how these streams are then used to send messages
        // to and from the peer.
        //
        // the response stream is the message flow FROM the peer
        // the 'request observer' is the message flow TO the peer
        //
        // Messages coming from the peer will be requests to invoke
        // chaincode, or will be the responses to stub APIs, such as getState
        // Message to the peer will be the getState APIs, and the results of
        // transaction invocations

        // The InnvocationTaskManager's way of being told there is a new
        // message, until this is received and processed there is now
        // knowing if this is a new transaction function or the answer to say getState

        LOGGER.info("making the grpc call");
        // for any error - shut everything down
        // as this is long lived (well forever) then any completion means something
        // has stopped in the peer or the network comms, so also shutdown
        final StreamObserver<ChaincodeMessage> requestObserver = chaincodeSupportClient.getStub().register(

                new StreamObserver<ChaincodeMessage>() {
                    @Override
                    public void onNext(final ChaincodeMessage chaincodeMessage) {
                        // message off to the ITM...
                        itm.onChaincodeMessage(chaincodeMessage);
                    }

                    @Override
                    public void onError(final Throwable t) {
                        LOGGER.severe(
                                () -> "An error occured on the chaincode stream. Shutting down the chaincode stream."
                                        + Logging.formatError(t));

                        chaincodeSupportClient.shutdown(itm);
                    }

                    @Override
                    public void onCompleted() {
                        LOGGER.severe("Chaincode stream is complete. Shutting down the chaincode stream.");
                        chaincodeSupportClient.shutdown(itm);
                    }
                }

        );

        chaincodeSupportClient.start(itm, requestObserver);

    }

    /**
     * connect external chaincode to peer for chat.
     *
     * @param requestObserver reqeust from peer
     * @return itm - The InnvocationTask Manager handles the message level
     *         communication with the peer.
     * @throws IOException validation fields exception
     */
    protected StreamObserver<ChaincodeShim.ChaincodeMessage> connectToPeer(
            final StreamObserver<ChaincodeMessage> requestObserver) throws IOException {
        validateOptions();
        if (requestObserver == null) {
            throw new IOException("StreamObserver 'requestObserver' for chat with peer can't be null");
        }
        // The ChaincodeSupport Client is a wrapper around the gRPC streams that
        // come from the single 'register' call that is made back to the peer
        //
        // Once this has been created, the InnvocationTaskManager that is responsible
        // for the thread management can be created.
        //
        // This is then passed to the ChaincodeSupportClient to be connected to the
        // gRPC streams

        final ChaincodeID chaincodeId = ChaincodeID.newBuilder().setName(this.id).build();
        final ManagedChannelBuilder<?> channelBuilder = newChannelBuilder();
        final ChaincodeSupportClient chaincodeSupportClient = new ChaincodeSupportClient(channelBuilder);

        final InvocationTaskManager itm = InvocationTaskManager.getManager(this, chaincodeId);

        chaincodeSupportClient.start(itm, requestObserver);

        return new StreamObserver<ChaincodeMessage>() {
            @Override
            public void onNext(final ChaincodeMessage chaincodeMessage) {
                itm.onChaincodeMessage(chaincodeMessage);
            }

            @Override
            public void onError(final Throwable t) {
                LOGGER.severe(() -> "An error occured on the chaincode stream. Shutting down the chaincode stream."
                        + Logging.formatError(t));

                chaincodeSupportClient.shutdown(itm);
            }

            @Override
            public void onCompleted() {
                LOGGER.severe("Chaincode stream is complete. Shutting down the chaincode stream.");
                chaincodeSupportClient.shutdown(itm);
            }
        };
    }

    protected final void initializeLogging() {
        // the VM wide formatting string.
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tH:%1$tM:%1$tS:%1$tL %4$-7.7s %2$-80.80s %5$s%6$s%n");
        final Logger rootLogger = Logger.getLogger("");

        for (final java.util.logging.Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(ALL);
            handler.setFormatter(new SimpleFormatter() {

                @Override
                public synchronized String format(final LogRecord record) {
                    return Thread.currentThread() + " " + super.format(record);
                }

            });
        }

        rootLogger.info("Updated all handlers the format");
        // set logging level of chaincode logger
        final Level chaincodeLogLevel = mapLevel(System.getenv(CORE_CHAINCODE_LOGGING_LEVEL));

        final Package chaincodePackage = this.getClass().getPackage();
        if (chaincodePackage != null) {
            Logger.getLogger(chaincodePackage.getName()).setLevel(chaincodeLogLevel);
        } else {
            // If chaincode declared without package, i.e. default package, lets set level
            // to root logger
            // Chaincode should never be declared without package
            Logger.getLogger("").setLevel(chaincodeLogLevel);
        }

        // set logging level of shim logger
        final Level shimLogLevel = mapLevel(System.getenv(CORE_CHAINCODE_LOGGING_SHIM));
        Logger.getLogger(ChaincodeBase.class.getPackage().getName()).setLevel(shimLogLevel);
        Logger.getLogger(ContractRouter.class.getPackage().getName()).setLevel(chaincodeLogLevel);

    }

    private Level mapLevel(final String level) {

        if (level != null) {
            switch (level.toUpperCase().trim()) {
            case "CRITICAL":
            case "ERROR":
                return Level.SEVERE;
            case "WARNING":
            case "WARN":
                return Level.WARNING;
            case "INFO":
                return Level.INFO;
            case "NOTICE":
                return Level.CONFIG;
            case "DEBUG":
                return Level.FINEST;
            default:
                break;
            }
        }
        return Level.INFO;
    }


    private SocketAddress parseHostPort(final String hostAddrStr) throws URISyntaxException {

        // WORKAROUND: add any scheme to make the resulting URI valid.
        URI uri = new URI("my://" + hostAddrStr); // may throw URISyntaxException
        String host = uri.getHost();
        int port = uri.getPort();

        if (uri.getHost() == null || uri.getPort() == -1) {
            throw new URISyntaxException(uri.toString(),
            "URI must have host and port parts");
        }

        // validation succeeded
        return new InetSocketAddress(host, port);
    }

    /**
     * Use the CHAINCODE_SERVER_ADDRESS as the key to swap mode.
     *
     * @return true if this should be run as `chaincode-as-a-service`
     */
    public boolean isServer() {
        return !chaincodeServerAddress.isEmpty();
    }

    /**
     * Validate init parameters from env chaincode base.
     */
    public void validateOptions() {
        if (this.id == null || this.id.isEmpty()) {
            throw new IllegalArgumentException(format(
                    "The chaincode id must be specified using either the -i or --i command line options or the %s environment variable.",
                    CORE_CHAINCODE_ID_NAME));
        }
        if (this.tlsEnabled) {
            if (tlsClientCertPath == null) {
                throw new IllegalArgumentException(
                        format("Client key certificate chain (%s) was not specified.", ENV_TLS_CLIENT_CERT_PATH));
            }
            if (tlsClientKeyPath == null) {
                throw new IllegalArgumentException(
                        format("Client key (%s) was not specified.", ENV_TLS_CLIENT_KEY_PATH));
            }
            if (tlsClientRootCertPath == null) {
                throw new IllegalArgumentException(
                        format("Peer certificate trust store (%s) was not specified.", CORE_PEER_TLS_ROOTCERT_FILE));
            }
        }
    }

    protected final void processCommandLineOptions(final String[] args) {
        final Options options = new Options();
        options.addOption("a", "peer.address", true, "Address of peer to connect to");
        options.addOption(null, "peerAddress", true, "Address of peer to connect to");
        options.addOption("i", "id", true, "Identity of chaincode");

        try {
            final CommandLine cl = new DefaultParser().parse(options, args);
            if (cl.hasOption("peerAddress") || cl.hasOption('a')) {
                String hostAddrStr;
                if (cl.hasOption('a')) {
                    hostAddrStr = cl.getOptionValue('a');
                } else {
                    hostAddrStr = cl.getOptionValue("peerAddress");
                }
                final String[] hostArr = hostAddrStr.split(":");
                if (hostArr.length == 2) {
                    port = Integer.valueOf(hostArr[1].trim());
                    host = hostArr[0].trim();
                } else {
                    final String msg = String.format(
                            "peer address argument should be in host:port format, current %s in wrong", hostAddrStr);
                    LOGGER.severe(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
            if (cl.hasOption('i')) {
                id = cl.getOptionValue('i');
            }
        } catch (final Exception e) {
            LOGGER.warning(() -> "cli parsing failed with exception" + Logging.formatError(e));
        }

        LOGGER.info("<<<<<<<<<<<<<CommandLine options>>>>>>>>>>>>");
        LOGGER.info("CORE_CHAINCODE_ID_NAME: " + this.id);
        LOGGER.info("CORE_PEER_ADDRESS: " + this.host + ":" + this.port);

    }

    /**
     * set fields from env.
     */
    public final void processEnvironmentOptions() {

        if (System.getenv().containsKey(CORE_CHAINCODE_ID_NAME)) {
            this.id = System.getenv(CORE_CHAINCODE_ID_NAME);
        }
        if (System.getenv().containsKey(CORE_PEER_ADDRESS)) {
            final String[] hostArr = System.getenv(CORE_PEER_ADDRESS).split(":");
            if (hostArr.length == 2) {
                this.port = Integer.valueOf(hostArr[1].trim());
                this.host = hostArr[0].trim();
            } else {
                final String msg = String.format(
                        "peer address argument should be in host:port format, ignoring current %s",
                        System.getenv(CORE_PEER_ADDRESS));
                LOGGER.severe(msg);
            }
        }

        if (System.getenv().containsKey(CHAINCODE_SERVER_ADDRESS)) {
            this.chaincodeServerAddress = System.getenv(CHAINCODE_SERVER_ADDRESS);
        }

        if (System.getenv().containsKey(CORE_PEER_LOCALMSPID)) {
            this.localMspId = System.getenv(CORE_PEER_LOCALMSPID);
        }

        this.tlsEnabled = Boolean.parseBoolean(System.getenv(CORE_PEER_TLS_ENABLED));
        if (this.tlsEnabled) {
            this.tlsClientRootCertPath = System.getenv(CORE_PEER_TLS_ROOTCERT_FILE);
            this.tlsClientKeyPath = System.getenv(ENV_TLS_CLIENT_KEY_PATH);
            this.tlsClientCertPath = System.getenv(ENV_TLS_CLIENT_CERT_PATH);

            this.tlsClientKeyFile = System.getenv(ENV_TLS_CLIENT_KEY_FILE);
            this.tlsClientCertFile = System.getenv(ENV_TLS_CLIENT_CERT_FILE);
        }

        LOGGER.info("<<<<<<<<<<<<<Environment options>>>>>>>>>>>>");
        LOGGER.info("CORE_CHAINCODE_ID_NAME: " + this.id);
        LOGGER.info("CORE_PEER_ADDRESS: " + this.host);
        LOGGER.info("CORE_PEER_TLS_ENABLED: " + this.tlsEnabled);
        LOGGER.info("CORE_PEER_TLS_ROOTCERT_FILE: " + this.tlsClientRootCertPath);
        LOGGER.info("CORE_TLS_CLIENT_KEY_PATH: " + this.tlsClientKeyPath);
        LOGGER.info("CORE_TLS_CLIENT_CERT_PATH: " + this.tlsClientCertPath);
        LOGGER.info("CORE_TLS_CLIENT_KEY_FILE: " + this.tlsClientKeyFile);
        LOGGER.info("CORE_TLS_CLIENT_CERT_FILE: " + this.tlsClientCertFile);
        LOGGER.info("CORE_PEER_LOCALMSPID: " + this.localMspId);
        LOGGER.info("CHAINCODE_SERVER_ADDRESS: " + this.chaincodeServerAddress);
        LOGGER.info("LOGLEVEL: " + this.logLevel);
    }

    /**
     * Obtains configuration specifically for running the chaincode and settable on
     * a per chaincode basis rather than taking properties from the Peers'
     * configuration.
     *
     * @return Configuration
     */
    public Properties getChaincodeConfig() {
        if (this.props == null) {

            final ClassLoader cl = this.getClass().getClassLoader();
            // determine the location of the properties file to control the metrics etc.

            props = new Properties();

            try (InputStream inStream = cl.getResourceAsStream("config.props")) {
                if (inStream != null) {
                    props.load(inStream);
                }
            } catch (final IOException e) {
                LOGGER.warning(() -> "Can not open the properties file for input " + Logging.formatError(e));
            }

            // will be useful
            props.setProperty(CORE_CHAINCODE_ID_NAME, this.id);
            props.setProperty(CORE_PEER_ADDRESS, this.host);

            LOGGER.info("<<<<<<<<<<<<<Properties options>>>>>>>>>>>>");
            LOGGER.info(() -> this.props.toString());
        }

        return this.props;
    }

    /**
     * The properties for starting as chaincode-as-a-service.
     *
     * @return ChaincodeServerProperties populated
     */
    public final ChaincodeServerProperties getChaincodeServerConfig() throws URISyntaxException {
        ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();

        chaincodeServerProperties.setServerAddress(parseHostPort(chaincodeServerAddress));

        if (tlsEnabled) {

            // set values on the server properties
            chaincodeServerProperties.setTlsEnabled(true);
            chaincodeServerProperties.setKeyFile(this.tlsClientCertFile);
            chaincodeServerProperties.setKeyCertChainFile(this.tlsClientCertFile);
        }
        return chaincodeServerProperties;
    }

    /**
     * create NettyChannel for host:port with tls if tlsEnabled.
     *
     * @return ManagedChannelBuilder
     * @throws IOException while createSSLContext()
     */
    @SuppressWarnings("deprecation")
    public final ManagedChannelBuilder<?> newChannelBuilder() throws IOException {

        // Consider moving this to be pure GRPC
        // This is being reworked in master so leaving this 'as-is'
        final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
        LOGGER.info("Configuring channel connection to peer.");

        builder.maxInboundMessageSize(getMaxInboundMessageSize());

        if (tlsEnabled) {
            builder.negotiationType(NegotiationType.TLS);
            builder.sslContext(createSSLContext());
        } else {
            builder.usePlaintext();
        }

        // there is a optional in GRPC to use 'directExecutor' rather than the inbuilt
        // gRPC thread management
        // not seen to make a marked difference in performance.
        // However if it ever does, then this is where it should be enabled
        return builder;
    }

    final SslContext createSSLContext() throws IOException {
        final byte[] ckb = Files.readAllBytes(Paths.get(this.tlsClientKeyPath));
        final byte[] ccb = Files.readAllBytes(Paths.get(this.tlsClientCertPath));

        return GrpcSslContexts.forClient().trustManager(new File(this.tlsClientRootCertPath))
                .keyManager(new ByteArrayInputStream(Base64.getDecoder().decode(ccb)),
                        new ByteArrayInputStream(Base64.getDecoder().decode(ckb)))
                .build();
    }

    @Deprecated
    protected static Response newSuccessResponse(final String message, final byte[] payload) {
        return ResponseUtils.newSuccessResponse(message, payload);
    }

    @Deprecated
    protected static Response newSuccessResponse() {
        return ResponseUtils.newSuccessResponse();
    }

    @Deprecated
    protected static Response newSuccessResponse(final String message) {
        return ResponseUtils.newSuccessResponse(message);
    }

    @Deprecated
    protected static Response newSuccessResponse(final byte[] payload) {
        return ResponseUtils.newSuccessResponse(payload);
    }

    @Deprecated
    protected static Response newErrorResponse(final String message, final byte[] payload) {
        return ResponseUtils.newErrorResponse(message, payload);
    }

    @Deprecated
    protected static Response newErrorResponse() {
        return ResponseUtils.newErrorResponse();
    }

    @Deprecated
    protected static Response newErrorResponse(final String message) {
        return ResponseUtils.newErrorResponse(message);
    }

    @Deprecated
    protected static Response newErrorResponse(final byte[] payload) {
        return ResponseUtils.newErrorResponse(payload);
    }

    @Deprecated
    protected static Response newErrorResponse(final Throwable throwable) {
        return ResponseUtils.newErrorResponse(throwable);
    }

    final String getHost() {
        return host;
    }

    final int getPort() {
        return port;
    }

    final boolean isTlsEnabled() {
        return tlsEnabled;
    }

    final String getTlsClientKeyPath() {
        return tlsClientKeyPath;
    }

    final String getTlsClientCertPath() {
        return tlsClientCertPath;
    }

    final String getTlsClientRootCertPath() {
        return tlsClientRootCertPath;
    }

    /**
     * Chaincode name / Chaincode id.
     *
     * @return string
     */
    String getId() {
        return id;
    }

    /**
     * Chaincode State.
     */
    public enum CCState {
        /**
         * */
        CREATED,
        /** */
        ESTABLISHED,
        /** */
        READY
    }

    private CCState state = CCState.CREATED;

    /**
     *
     * @return State
     */
    public final CCState getState() {
        return this.state;
    }

    /**
     *
     * @param newState
     */
    public final void setState(final CCState newState) {
        this.state = newState;
    }

    /**
     * Debug Message.
     *
     * @param message
     * @return JSON Form of message
     */
    public static String toJsonString(final ChaincodeMessage message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (final InvalidProtocolBufferException e) {
            return String.format("{ Type: %s, TxId: %s }", message.getType(), message.getTxid());
        }
    }
}
