/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc.ChaincodeSupportStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ChaincodeSupportClient {
    private static Logger logger = Logger.getLogger(ChaincodeSupportClient.class.getName());
    private static Logger perflogger = Logger.getLogger(Logging.PERFLOGGER);

    private final ManagedChannel channel;
    private final ChaincodeSupportStub stub;

    /**
     *
     * @param channelBuilder
     */
    public ChaincodeSupportClient(final ManagedChannelBuilder<?> channelBuilder) {
        this.channel = channelBuilder.build();
        this.stub = ChaincodeSupportGrpc.newStub(channel);
    }

    private static final int DEFAULT_TIMEOUT = 5;

    private void shutdown(final InvocationTaskManager itm) {

        // first shutdown the thread pool
        itm.shutdown();
        try {
            this.channel.shutdown();
            if (!channel.awaitTermination(DEFAULT_TIMEOUT, TimeUnit.SECONDS)) {
                channel.shutdownNow();
                if (!channel.awaitTermination(DEFAULT_TIMEOUT, TimeUnit.SECONDS)) {
                    System.err.println("Channel did not terminate");
                }
            }

        } catch (final InterruptedException e) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    /**
     *
     * @param itm
     */
    public void start(final InvocationTaskManager itm) {

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

        // The InvocationTaskManager's way of being told there is a new
        // message, until this is received and processed there is no
        // knowing if this is a new transaction function or the answer to say getState
        final Consumer<ChaincodeMessage> consumer = itm::onChaincodeMessage;

        logger.info("making the grpc call");
        // for any error - shut everything down
        // as this is long lived (well forever) then any completion means something
        // has stopped in the peer or the network comms, so also shutdown
        final StreamObserver<ChaincodeMessage> requestObserver = this.stub.register(

                new StreamObserver<ChaincodeMessage>() {
                    @Override
                    public void onNext(final ChaincodeMessage chaincodeMessage) {
                        // message off to the ITM...
                        consumer.accept(chaincodeMessage);
                    }

                    @Override
                    public void onError(final Throwable t) {
                        logger.severe(() -> "An error occurred on the chaincode stream. Shutting down the chaincode stream." + Logging.formatError(t));

                        ChaincodeSupportClient.this.shutdown(itm);
                    }

                    @Override
                    public void onCompleted() {
                        logger.severe("Chaincode stream is complete. Shutting down the chaincode stream.");
                        ChaincodeSupportClient.this.shutdown(itm);
                    }
                }

        );

        // Consumer function for response messages (those going back to the peer)
        // gRPC streams need to be accessed by one thread at a time, so
        // use a lock to protect this.
        //
        // Previous implementations used a dedicated thread for this. However this extra
        // thread is not really required. The main thread executing the transaction will
        // not be
        // held up for long, nor can any one transaction invoke more that one stub api
        // at a time.
        final Consumer<ChaincodeMessage> c = new Consumer<ChaincodeMessage>() {

            // create a lock, with fair property
            private final ReentrantLock lock = new ReentrantLock(true);

            @Override
            public void accept(final ChaincodeMessage t) {
                lock.lock();
                perflogger.fine(() -> "> sendToPeer " + t.getTxid());
                requestObserver.onNext(t);
                perflogger.fine(() -> "< sendToPeer " + t.getTxid());
                lock.unlock();
            }
        };

        // Pass a Consumer interface back to the the task manager. This is for tasks to
        // use to respond back to the peer.
        //
        // NOTE the register() - very important - as this triggers the ITM to send the
        // first message to the peer; otherwise the both sides will sit there waiting
        itm.setResponseConsumer(c).register();

    }
}
