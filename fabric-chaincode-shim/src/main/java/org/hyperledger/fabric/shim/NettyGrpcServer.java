/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;


import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.execution.impl.ContractInvocationRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * implementation grpc server with NettyGrpcServer.
 */
public final class NettyGrpcServer implements GrpcServer {

    private static Log logger = LogFactory.getLog(NettyGrpcServer.class);

    private final Server server;
    /**
     * init netty grpc server.
     *
     * @param chaincodeBase       - chaincode implementation (invoke, init)
     * @throws IOException
     */
    NettyGrpcServer(final ChaincodeBase chaincodeBase, final GrpcServerSetting grpcServerSetting) throws IOException {
        if (chaincodeBase == null) {
            throw new IOException("chaincode must be specified");
        }
        if (grpcServerSetting == null) {
            throw new IOException("GrpcServerSetting must be specified");
        }
        if (grpcServerSetting.getPortChaincodeServer() <= 0) {
            throw new IOException("GrpcServerSetting.getPortChaincodeServer() must be more then 0");
        }
        if (grpcServerSetting.getKeepAliveTimeMinutes() <= 0) {
            throw new IOException("GrpcServerSetting.getKeepAliveTimeMinutes() must be more then 0");
        }
        if (grpcServerSetting.getKeepAliveTimeoutSeconds() <= 0) {
            throw new IOException("GrpcServerSetting.getKeepAliveTimeoutSeconds() must be more then 0");
        }
        if (grpcServerSetting.getPermitKeepAliveTimeMinutes() <= 0) {
            throw new IOException("GrpcServerSetting.getPermitKeepAliveTimeMinutes() must be more then 0");
        }
        if (grpcServerSetting.getMaxConnectionAgeSeconds() <= 0) {
            throw new IOException("GrpcServerSetting.getMaxConnectionAgeSeconds() must be more then 0");
        }
        if (grpcServerSetting.getMaxInboundMetadataSize() <= 0) {
            throw new IOException("GrpcServerSetting.getMaxInboundMetadataSize() must be more then 0");
        }
        if (grpcServerSetting.getMaxInboundMessageSize() <= 0) {
            throw new IOException("GrpcServerSetting.getMaxInboundMessageSize() must be more then 0");
        }

        if (
                grpcServerSetting.isTlsEnabled() && (
                        grpcServerSetting.getKeyCertChainFile() == null ||
                        grpcServerSetting.getKeyCertChainFile().isEmpty() ||
                        grpcServerSetting.getKeyFile() == null ||
                        grpcServerSetting.getKeyFile().isEmpty()
                )
        ) {
            throw new IOException("if GrpcServerSetting.isTlsEnabled() must be more specified" +
                    " grpcServerSetting.getKeyCertChainFile() and grpcServerSetting.getKeyFile()" +
                    " with optional grpcServerSetting.getKeyPassword()");
        }

        final NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(grpcServerSetting.getPortChaincodeServer())
                .addService(new ChatChaincodeWithPeer(chaincodeBase))
                .keepAliveTime(grpcServerSetting.getKeepAliveTimeMinutes(), TimeUnit.MINUTES)
                .keepAliveTimeout(grpcServerSetting.getKeepAliveTimeoutSeconds(), TimeUnit.SECONDS)
                .permitKeepAliveTime(grpcServerSetting.getPermitKeepAliveTimeMinutes(), TimeUnit.MINUTES)
                .permitKeepAliveWithoutCalls(grpcServerSetting.isPermitKeepAliveWithoutCalls())
                .maxConnectionAge(grpcServerSetting.getMaxConnectionAgeSeconds(), TimeUnit.SECONDS)
                .maxInboundMetadataSize(grpcServerSetting.getMaxInboundMetadataSize())
                .maxInboundMessageSize(grpcServerSetting.getMaxInboundMessageSize());

        if (grpcServerSetting.isTlsEnabled()) {
            final File keyCertChainFile = Paths.get(grpcServerSetting.getKeyCertChainFile()).toFile();
            final File keyFile = Paths.get(grpcServerSetting.getKeyFile()).toFile();

            if (grpcServerSetting.getKeyPassword() == null || grpcServerSetting.getKeyPassword().isEmpty()) {
                serverBuilder.sslContext(SslContextBuilder.forServer(keyCertChainFile, keyFile).build());
            } else {
                serverBuilder.sslContext(SslContextBuilder.forServer(keyCertChainFile, keyFile, grpcServerSetting.getKeyPassword()).build());
            }
        }

        logger.info("<<<<<<<<<<<<<GrpcServerSetting>>>>>>>>>>>>:\n");
        logger.info("PortChaincodeServer:" + grpcServerSetting.getPortChaincodeServer());
        logger.info("MaxInboundMetadataSize:" + grpcServerSetting.getMaxInboundMetadataSize());
        logger.info("MaxInboundMessageSize:" + grpcServerSetting.getMaxInboundMessageSize());
        logger.info("MaxConnectionAgeSeconds:" + grpcServerSetting.getMaxConnectionAgeSeconds());
        logger.info("KeepAliveTimeoutSeconds:" + grpcServerSetting.getKeepAliveTimeoutSeconds());
        logger.info("PermitKeepAliveTimeMinutes:" + grpcServerSetting.getPermitKeepAliveTimeMinutes());
        logger.info("KeepAliveTimeMinutes:" + grpcServerSetting.getKeepAliveTimeMinutes());
        logger.info("PermitKeepAliveWithoutCalls:" + grpcServerSetting.getPermitKeepAliveWithoutCalls());
        logger.info("KeyPassword:" + grpcServerSetting.getKeyPassword());
        logger.info("KeyCertChainFile:" + grpcServerSetting.getKeyCertChainFile());
        logger.info("KeyFile:" + grpcServerSetting.getKeyFile());
        logger.info("isTlsEnabled:" + grpcServerSetting.isTlsEnabled());
        logger.info("\n");

        this.server = serverBuilder.build();
    }

    /**
     * start grpc server.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        logger.info("start grpc server");
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
        logger.info("Waits for the server to become terminated.");
        server.awaitTermination();
    }

    /**
     * shutdown now grpc server.
     */
    public void stop() {
        logger.info("shutdown now grpc server.");
        server.shutdownNow();
    }
}
