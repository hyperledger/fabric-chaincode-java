/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.grpc.ClientInterceptor;
import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc.ChaincodeSupportStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.hyperledger.fabric.traces.Traces;

public class ChaincodeSupportClient {
    private static final int DEFAULT_TIMEOUT = 5;
    private static final Logger LOGGER = Logger.getLogger(ChaincodeSupportClient.class.getName());
    private static Logger perflogger = Logger.getLogger(Logging.PERFLOGGER);
    private final ManagedChannel channel;
    private final ChaincodeSupportStub stub;

    /**
     * @param channelBuilder
     */
    public ChaincodeSupportClient(final ManagedChannelBuilder<?> channelBuilder) {
        ClientInterceptor interceptor = Traces.getProvider().createInterceptor();
        if (interceptor != null) {
            channelBuilder.intercept(interceptor);
        }
        this.channel = channelBuilder.build();
        this.stub = ChaincodeSupportGrpc.newStub(channel);
    }

    /**
     *
     * @param itm
     */
    public void shutdown(final InvocationTaskManager itm) {

        // first shutdown the thread pool
        itm.shutdown();
        try {
            this.channel.shutdown();
            if (!channel.awaitTermination(DEFAULT_TIMEOUT, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }

        } catch (final InterruptedException e) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    /**
     *
     * @param itm
     * @param requestObserver
     * @throws IOException verify parameters error
     */
    public void start(final InvocationTaskManager itm, final StreamObserver<ChaincodeMessage> requestObserver) throws IOException {
        if (requestObserver == null) {
            throw new IOException("StreamObserver 'requestObserver' for chat with peer can't be null");
        }
        if (itm == null) {
            throw new IOException("InnvocationTaskManager 'itm' can't be null");
        }

        // Consumer function for response messages (those going back to the peer)
        // gRPC streams need to be accessed by one thread at a time, so
        // use a lock to protect this.
        //
        // Previous implementations used a dedicated thread for this. However this extra
        // thread is not really required. The main thread executing the transaction will
        // not be
        // held up for long, nor can any one transaction invoke more that one stub api
        // at a time.
        final Consumer<ChaincodeMessage> consumer = new Consumer<ChaincodeMessage>() {

            // create a lock, with fair property
            private final ReentrantLock lock = new ReentrantLock(true);

            @Override
            public void accept(final ChaincodeMessage t) {
                lock.lock();
                perflogger.fine(() -> "> sendToPeer TX::" + t.getTxid());
                requestObserver.onNext(t);
                perflogger.fine(() -> "< sendToPeer TX::" + t.getTxid());
                lock.unlock();
            }
        };

        // Pass a Consumer interface back to the the task manager. This is for tasks to
        // use to respond back to the peer.
        //
        // NOTE the register() - very important - as this triggers the ITM to send the
        // first message to the peer; otherwise the both sides will sit there waiting
        itm.setResponseConsumer(consumer).register();
    }

    /**
     * ChaincodeSupportStub.
     * @return stub
     */
    public ChaincodeSupportStub getStub() {
        return stub;
    }
}
