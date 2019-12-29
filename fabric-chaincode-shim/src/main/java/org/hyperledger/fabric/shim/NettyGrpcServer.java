/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;


import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.hyperledger.fabric.protos.peer.ChaincodeGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.impl.InnvocationTaskManager;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class NettyGrpcServer implements GrpcServer {

    private final Server server;

    public NettyGrpcServer(String addressChaincodeServer, TlsConfig tlsConfig, ChaincodeBase chaincodeBase) throws IOException {
        if (chaincodeBase == null) {
            throw new IOException("chaincode must be specified");
        }

        if (addressChaincodeServer == null || addressChaincodeServer.isEmpty()) {
            throw new IOException("server listen address not provided");
        }

        final String hostname = addressChaincodeServer.substring(0, addressChaincodeServer.indexOf(":"));
        final int port = Integer.parseInt(addressChaincodeServer.substring(addressChaincodeServer.indexOf(":") + 1));

        final NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress(hostname, port))
                .addService(new ChatChaincodeWithPeer(chaincodeBase))
                .keepAliveTime(1, TimeUnit.MINUTES)
                .keepAliveTimeout(20, TimeUnit.SECONDS)
                .permitKeepAliveTime(1, TimeUnit.MINUTES)
                .permitKeepAliveWithoutCalls(true)
                .maxConnectionAge(5, TimeUnit.SECONDS)
                .maxInboundMetadataSize(100 * 1024 * 1024)
                .maxInboundMessageSize(100 * 1024 * 1024);

        final SslContext sslContext;
        if (tlsConfig != null && !tlsConfig.isDisabled()) {
            final File certificatePemFile = Paths.get(tlsConfig.getCert()).toFile();
            final File privateKeyPemFile = Paths.get(tlsConfig.getKey()).toFile();

            sslContext = GrpcSslContexts.configure(SslContextBuilder.forServer(certificatePemFile, privateKeyPemFile)).build();
            serverBuilder.sslContext(sslContext);
        }

        server = serverBuilder.build();
    }

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
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    class ChatChaincodeWithPeer extends ChaincodeGrpc.ChaincodeImplBase {

        private ChaincodeBase chaincodeBase;

        ChatChaincodeWithPeer(ChaincodeBase chaincodeBase) {
            this.chaincodeBase = chaincodeBase;
        }

        @Override
        public StreamObserver<ChaincodeShim.ChaincodeMessage> connect(StreamObserver<ChaincodeShim.ChaincodeMessage> responseObserver) {

            InnvocationTaskManager itm = null;
            try {
                itm = chaincodeBase.connectToPeer(responseObserver);
            } catch (IOException e) {
                e.printStackTrace();
            }

            InnvocationTaskManager finalItm = itm;
            return new StreamObserver<ChaincodeShim.ChaincodeMessage>() {
                @Override
                public void onNext(ChaincodeShim.ChaincodeMessage value) {
                    finalItm.onChaincodeMessage(value);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
