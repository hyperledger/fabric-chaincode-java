/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;


import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import java.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * implementation grpc server with NettyGrpcServer.
 */
public final class NettyGrpcServer implements GrpcServer {

    private static final Logger LOGGER = Logger.getLogger(NettyGrpcServer.class.getName());

    private final Server server;

    /**
     * init netty grpc server.
     *
     * @param chaincodeBase             - chaincode implementation (invoke, init)
     * @param chaincodeServerProperties - setting for grpc server
     * @throws IOException
     */
    public NettyGrpcServer(final ChaincodeBase chaincodeBase, final ChaincodeServerProperties chaincodeServerProperties) throws IOException {
        if (chaincodeBase == null) {
            throw new IllegalArgumentException("chaincode must be specified");
        }
        if (chaincodeServerProperties == null) {
            throw new IllegalArgumentException("chaincodeServerProperties must be specified");
        }
        chaincodeServerProperties.validate();

        final NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(chaincodeServerProperties.getServerAddress())
                .addService(new ChatChaincodeWithPeer(chaincodeBase))
                .keepAliveTime(chaincodeServerProperties.getKeepAliveTimeMinutes(), TimeUnit.MINUTES)
                .keepAliveTimeout(chaincodeServerProperties.getKeepAliveTimeoutSeconds(), TimeUnit.SECONDS)
                .permitKeepAliveTime(chaincodeServerProperties.getPermitKeepAliveTimeMinutes(), TimeUnit.MINUTES)
                .permitKeepAliveWithoutCalls(chaincodeServerProperties.isPermitKeepAliveWithoutCalls())
                .maxConnectionAge(chaincodeServerProperties.getMaxConnectionAgeSeconds(), TimeUnit.SECONDS)
                .maxInboundMetadataSize(chaincodeServerProperties.getMaxInboundMetadataSize())
                .maxInboundMessageSize(chaincodeServerProperties.getMaxInboundMessageSize());

        if (chaincodeServerProperties.isTlsEnabled()) {
            final File keyCertChainFile = Paths.get(chaincodeServerProperties.getKeyCertChainFile()).toFile();
            final File keyFile = Paths.get(chaincodeServerProperties.getKeyFile()).toFile();

            SslContextBuilder sslContextBuilder;
            if (chaincodeServerProperties.getKeyPassword() == null || chaincodeServerProperties.getKeyPassword().isEmpty()) {
                sslContextBuilder = SslContextBuilder.forServer(keyCertChainFile, keyFile);
            } else {
                sslContextBuilder = SslContextBuilder.forServer(keyCertChainFile, keyFile, chaincodeServerProperties.getKeyPassword());
            }

            ApplicationProtocolConfig apn = new ApplicationProtocolConfig(
                    ApplicationProtocolConfig.Protocol.ALPN,
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                    ApplicationProtocolNames.HTTP_2);
            sslContextBuilder.applicationProtocolConfig(apn);

            if (chaincodeServerProperties.getTrustCertCollectionFile() != null) {
                final File trustCertCollectionFile = Paths.get(chaincodeServerProperties.getTrustCertCollectionFile()).toFile();
                sslContextBuilder.clientAuth(ClientAuth.REQUIRE);
                sslContextBuilder.trustManager(trustCertCollectionFile);
            }

            serverBuilder.sslContext(sslContextBuilder.build());
        }

        LOGGER.info("<<<<<<<<<<<<<chaincodeServerProperties>>>>>>>>>>>>:\n");
        LOGGER.info("ServerAddress:" + chaincodeServerProperties.getServerAddress().toString());
        LOGGER.info("MaxInboundMetadataSize:" + chaincodeServerProperties.getMaxInboundMetadataSize());
        LOGGER.info("MaxInboundMessageSize:" + chaincodeServerProperties.getMaxInboundMessageSize());
        LOGGER.info("MaxConnectionAgeSeconds:" + chaincodeServerProperties.getMaxConnectionAgeSeconds());
        LOGGER.info("KeepAliveTimeoutSeconds:" + chaincodeServerProperties.getKeepAliveTimeoutSeconds());
        LOGGER.info("PermitKeepAliveTimeMinutes:" + chaincodeServerProperties.getPermitKeepAliveTimeMinutes());
        LOGGER.info("KeepAliveTimeMinutes:" + chaincodeServerProperties.getKeepAliveTimeMinutes());
        LOGGER.info("PermitKeepAliveWithoutCalls:" + chaincodeServerProperties.getPermitKeepAliveWithoutCalls());
        LOGGER.info("KeyPassword:" + chaincodeServerProperties.getKeyPassword());
        LOGGER.info("KeyCertChainFile:" + chaincodeServerProperties.getKeyCertChainFile());
        LOGGER.info("KeyFile:" + chaincodeServerProperties.getKeyFile());
        LOGGER.info("isTlsEnabled:" + chaincodeServerProperties.isTlsEnabled());
        LOGGER.info("\n");

        this.server = serverBuilder.build();
    }

    /**
     * start grpc server.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        LOGGER.info("start grpc server");
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(() -> {
                            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                            System.err.println("*** shutting down gRPC server since JVM is shutting down");
                            NettyGrpcServer.this.stop();
                            System.err.println("*** server shut down");
                        }));
        server.start();
    }

    /**
     * Waits for the server to become terminated.
     *
     * @throws InterruptedException
     */
    public void blockUntilShutdown() throws InterruptedException {
        LOGGER.info("Waits for the server to become terminated.");
        server.awaitTermination();
    }

    /**
     * shutdown now grpc server.
     */
    public void stop() {
        LOGGER.info("shutdown now grpc server.");
        server.shutdownNow();
    }
}
