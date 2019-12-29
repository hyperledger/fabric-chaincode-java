/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;


import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.hyperledger.fabric.protos.peer.ChaincodeGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.impl.InnvocationTaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * implementation grpc server with NettyGrpcServer.
 */
public class NettyGrpcServer implements GrpcServer {

    private static final int TIMEOUT_AWAIT_TIMEOUT_SECONDS = 30;
    private static final int MAX_INBOUND_METADATA_SIZE = 100 * 1024 * 1024;
    private static final int MAX_INBOUND_MESSAGE_SIZE = 100 * 1024 * 1024;
    private static final int MAX_CONNECTION_AGE_SECONDS = 5;
    private static final int KEEP_ALIVE_TIMEOUT_SECONDS = 20;
    private static final int PERMIT_KEEP_ALIVE_TIME_MINUTES = 1;
    private static final int KEEP_ALIVE_TIME_MINUTES = 1;
    private final Server server;

    /**
     * init netty grpc server.
     *
     * @param portChaincodeServer - port chaincode server for example 9999
     * @param tlsConfig           - tls config to connect with peer
     * @param chaincodeBase       - chaincode implementation (invoke, init)
     * @throws IOException
     */
    NettyGrpcServer(final int portChaincodeServer, final TlsConfig tlsConfig, final ChaincodeBase chaincodeBase) throws IOException {
        if (chaincodeBase == null) {
            throw new IOException("chaincode must be specified");
        }

        final NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(portChaincodeServer)
                .addService(new ChatChaincodeWithPeer(chaincodeBase))
                .keepAliveTime(KEEP_ALIVE_TIME_MINUTES, TimeUnit.MINUTES)
                .keepAliveTimeout(KEEP_ALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .permitKeepAliveTime(PERMIT_KEEP_ALIVE_TIME_MINUTES, TimeUnit.MINUTES)
                .permitKeepAliveWithoutCalls(true)
                .maxConnectionAge(MAX_CONNECTION_AGE_SECONDS, TimeUnit.SECONDS)
                .maxInboundMetadataSize(MAX_INBOUND_METADATA_SIZE)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE);

        final SslContext sslContext;
        if (tlsConfig != null && !tlsConfig.isDisabled()) {
            final File certificatePemFile = Paths.get(tlsConfig.getCert()).toFile();
            final File privateKeyPemFile = Paths.get(tlsConfig.getKey()).toFile();

            sslContext = GrpcSslContexts.configure(SslContextBuilder.forServer(certificatePemFile, privateKeyPemFile)).build();
            serverBuilder.sslContext(sslContext);
        }

        server = serverBuilder.build();
    }

    /**
     * run grpc server.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(() -> {
                            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                            System.err.println("*** shutting down gRPC server since JVM is shutting down");
                            try {
                                NettyGrpcServer.this.stop();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.err.println("*** server shut down");
                        }));
        try {
            server.start().awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     *
     * @throws InterruptedException
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * shutdown.
     *
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(TIMEOUT_AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    private static class ChatChaincodeWithPeer extends ChaincodeGrpc.ChaincodeImplBase {

        private ChaincodeBase chaincodeBase;

        ChatChaincodeWithPeer(final ChaincodeBase chaincodeBase) {
            this.chaincodeBase = chaincodeBase;
        }

        @Override
        public StreamObserver<ChaincodeShim.ChaincodeMessage> connect(final StreamObserver<ChaincodeShim.ChaincodeMessage> responseObserver) {
            try {
                final InnvocationTaskManager itm = chaincodeBase.connectToPeer(responseObserver);
                return new StreamObserver<ChaincodeShim.ChaincodeMessage>() {
                    @Override
                    public void onNext(final ChaincodeShim.ChaincodeMessage value) {
                        itm.onChaincodeMessage(value);
                    }

                    @Override
                    public void onError(final Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
                // if we got error return nothing
                return null;
            }
        }
    }
}
