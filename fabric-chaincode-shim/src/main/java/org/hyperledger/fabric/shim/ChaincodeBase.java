/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import static java.lang.String.format;
import static java.util.logging.Level.ALL;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.shim.impl.ChaincodeSupportClient;
import org.hyperledger.fabric.shim.impl.InnvocationTaskManager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;

public abstract class ChaincodeBase implements Chaincode {

    public static final String CORE_CHAINCODE_LOGGING_SHIM = "CORE_CHAINCODE_LOGGING_SHIM";
    public static final String CORE_CHAINCODE_LOGGING_LEVEL = "CORE_CHAINCODE_LOGGING_LEVEL";

    @Override
    public abstract Response init(ChaincodeStub stub);

    @Override
    public abstract Response invoke(ChaincodeStub stub);

    private static final Logger logger = Logger.getLogger(ChaincodeBase.class.getName());

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
    private Properties props;
    private Level logLevel;

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

            Properties props = getChaincodeConfig();
            Metrics.initialize(props);
            validateOptions();
            connectToPeer();
        } catch (Exception e) {
        	logger.severe(()-> "Chaincode could not start" + Logging.formatError(e));
        }
    }

    protected void connectToPeer() throws IOException {
        
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
        ChaincodeSupportClient chaincodeSupportClient = new ChaincodeSupportClient(channelBuilder);
        
        InnvocationTaskManager itm = InnvocationTaskManager.getManager(this, chaincodeId);
        chaincodeSupportClient.start(itm);

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

        LogManager logManager = LogManager.getLogManager();

        Formatter f = new Formatter() {

            private final Date dat = new Date();
            private final String format = "%1$tH:%1$tM:%1$tS:%1$tL %4$-7.7s %2$-80.80s %5$s%6$s%n";

            @Override
            public String format(final LogRecord record) {
                dat.setTime(record.getMillis());
                String source;
                if (record.getSourceClassName() != null) {
                    source = record.getSourceClassName();
                    if (record.getSourceMethodName() != null) {
                        source += " " + record.getSourceMethodName();
                    }
                } else {
                    source = record.getLoggerName();
                }
                final String message = formatMessage(record);
                String throwable = "";
                if (record.getThrown() != null) {
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    pw.println();
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    throwable = sw.toString();
                }
                return String.format(format, dat, source, record.getLoggerName(), record.getLevel(), message,
                        throwable);

            }

        };
        	
        	
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
                    logger.severe(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
            if (cl.hasOption('i')) {
                id = cl.getOptionValue('i');
            }
        } catch (Exception e) {
            logger.warning(()->"cli parsing failed with exception" + Logging.formatError(e));
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
                logger.severe(msg);
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
        logger.info("LOGLEVEL: " + this.logLevel);
    }

    /** 
     * Obtains configuration specificially for running the chaincode, and settable on a per chaincode
     * basis, rather than taking properties from the Peers' configuration
     */
    public Properties getChaincodeConfig() {
        if (this.props == null) {

            ClassLoader cl = this.getClass().getClassLoader();
            // determine the location of the properties file to control the metrics etc.

            props = new Properties();

            try (InputStream inStream = cl.getResourceAsStream("config.props")) {
                if (inStream != null) {
                    props.load(inStream);
                }
            } catch (IOException e) {
                logger.warning(() -> "Can not open the properties file for input " + Logging.formatError(e));
            }

            // will be useful
            props.setProperty(CORE_CHAINCODE_ID_NAME, this.id);
            props.setProperty(CORE_PEER_ADDRESS, this.host);

            logger.info("<<<<<<<<<<<<<Properties options>>>>>>>>>>>>");
            logger.info(() -> this.props.toString());
        }

        return this.props;
    }

    @SuppressWarnings("deprecation")
    ManagedChannelBuilder<?> newChannelBuilder() throws IOException {

        // TODO: consider moving this to be pure GRPC
        // This is being reworked in master so leaving this 'as-is'
        final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
        logger.info("Configuring channel connection to peer.");

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

    public enum CCState {
        CREATED, ESTABLISHED, READY
    }

    CCState state = CCState.CREATED;

    public CCState getState() {
        return this.state;
    }

    public void setState(CCState newState) {
        this.state = newState;
    }

    public static String toJsonString(ChaincodeMessage message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            return String.format("{ Type: %s, TxId: %s }", message.getType(), message.getTxid());
        }
    }
}
