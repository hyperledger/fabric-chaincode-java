/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;


import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * implementation grpc server with NettyGrpcServer.
 */
public class NettyGrpcServer implements GrpcServer {

    private static final int MAX_INBOUND_METADATA_SIZE = 100 * 1024 * 1024;
    private static final int MAX_INBOUND_MESSAGE_SIZE = 100 * 1024 * 1024;
    private static final int MAX_CONNECTION_AGE_SECONDS = 5;
    private static final int KEEP_ALIVE_TIMEOUT_SECONDS = 20;
    private static final int PERMIT_KEEP_ALIVE_TIME_MINUTES = 1;
    private static final int KEEP_ALIVE_TIME_MINUTES = 1;
    private final Server server;

    private static final String PORT_CHAINCODE_SERVER = "PORT_CHAINCODE_SERVER";
    /**
     * init netty grpc server.
     *
     * @param chaincodeBase       - chaincode implementation (invoke, init)
     * @throws IOException
     */
    NettyGrpcServer(final ChaincodeBase chaincodeBase) throws IOException {
        if (chaincodeBase == null) {
            throw new IOException("chaincode must be specified");
        }

        final String portChaincodeServer = System.getenv(PORT_CHAINCODE_SERVER);
        if (portChaincodeServer == null) {
            throw new IOException("chaincode server port not defined in system env. for example 'PORT_CHAINCODE_SERVER=9999'");
        }
        final int port = Integer.parseInt(portChaincodeServer);

        final NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port)
                .addService(new ChatChaincodeWithPeer(chaincodeBase))
                .keepAliveTime(KEEP_ALIVE_TIME_MINUTES, TimeUnit.MINUTES)
                .keepAliveTimeout(KEEP_ALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .permitKeepAliveTime(PERMIT_KEEP_ALIVE_TIME_MINUTES, TimeUnit.MINUTES)
                .permitKeepAliveWithoutCalls(true)
                .maxConnectionAge(MAX_CONNECTION_AGE_SECONDS, TimeUnit.SECONDS)
                .maxInboundMetadataSize(MAX_INBOUND_METADATA_SIZE)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE);

        if (chaincodeBase.isTlsEnabled()) {
            throw new IOException("not implemented yet");
        }

        server = serverBuilder.build();
    }

    /**
     * start grpc server.
     *
     * @throws IOException
     */
    public void start() throws IOException {
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
     * Await termination on the main thread since the grpc library uses daemon threads.
     *
     * @throws InterruptedException
     */
    public void blockUntilShutdown() throws InterruptedException {
        server.awaitTermination();
    }

    /**
     * shutdown now grpc server.
     */
    public void stop() {
        server.shutdownNow();
    }
}
