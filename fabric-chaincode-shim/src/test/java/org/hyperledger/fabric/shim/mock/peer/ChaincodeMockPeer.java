/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import static org.hyperledger.fabric.protos.peer.ChaincodeMessage.Type.PUT_STATE;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;
import org.hyperledger.fabric.shim.utils.TimeoutUtil;

/** Mock peer implementation */
public final class ChaincodeMockPeer {
    private static final Logger LOGGER = Logger.getLogger(ChaincodeMockPeer.class.getName());

    private final int port;
    private final Server server;
    private final ChaincodeMockPeerService service;

    /**
     * Constructor
     *
     * @param scenario list of scenario steps
     * @param port mock peer communication port
     * @throws IOException
     */
    public ChaincodeMockPeer(final List<ScenarioStep> scenario, final int port) {
        this.port = port;
        this.service = new ChaincodeMockPeerService(scenario);
        final ServerBuilder<?> sb = ServerBuilder.forPort(port);
        this.server = sb.addService(this.service).build();
    }

    /** Start serving requests. */
    public void start() throws IOException {
        server.start();
        LOGGER.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may has been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                ChaincodeMockPeer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() {
        if (server != null) {
            server.shutdownNow();
            try {
                server.awaitTermination();
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Send message from mock peer to chaincode (to start init, invoke, etc)
     *
     * @param msg
     */
    public void send(final ChaincodeMessage msg) {
        this.service.lastMessageSend = msg;

        LOGGER.info("Mock peer => Sending message: " + msg);
        this.service.send(msg);
    }

    /**
     * Check last executed step number, to check where in scenario we stopped
     *
     * @return
     */
    public int getLastExecutedStep() {
        return this.service.lastExecutedStepNumber;
    }

    /** @return last received message from chaincode */
    public ChaincodeMessage getLastMessageRcvd() {
        return this.service.lastMessageRcvd;
    }

    public ArrayList<ChaincodeMessage> getAllReceivedMessages() {
        return this.service.allMessages;
    }

    /** @return last message sent by peer to chaincode */
    public ChaincodeMessage getLastMessageSend() {
        return this.service.lastMessageSend;
    }

    /**
     * Creates new instance of mock peer server, starts it and returns
     *
     * @param scenario
     * @return
     * @throws Exception
     */
    public static ChaincodeMockPeer startServer(final List<ScenarioStep> scenario) throws Exception {
        final ChaincodeMockPeer server = new ChaincodeMockPeer(scenario, 7052);
        server.start();
        return server;
    }

    private static class ChaincodeMockPeerService extends ChaincodeSupportGrpc.ChaincodeSupportImplBase {
        private final List<ScenarioStep> scenario;
        private int lastExecutedStepNumber;
        private ChaincodeMessage lastMessageRcvd;
        private ChaincodeMessage lastMessageSend;
        private final ArrayList<ChaincodeMessage> allMessages = new ArrayList<>();
        private StreamObserver<ChaincodeMessage> observer;

        // create a lock, with fair property
        private final ReentrantLock lock = new ReentrantLock(true);

        ChaincodeMockPeerService(final List<ScenarioStep> scenario) {
            this.scenario = scenario;
            this.lastExecutedStepNumber = 0;
        }

        public void send(final ChaincodeMessage msg) {
            lock.lock();

            observer.onNext(msg);
            lock.unlock();
        }

        /**
         * Attaching observer to steams
         *
         * @param responseObserver
         * @return
         */
        @Override
        public StreamObserver<ChaincodeMessage> register(final StreamObserver<ChaincodeMessage> responseObserver) {
            observer = responseObserver;
            return new StreamObserver<ChaincodeMessage>() {

                /**
                 * Handling incoming messages
                 *
                 * @param chaincodeMessage
                 */
                @Override
                public void onNext(final ChaincodeMessage chaincodeMessage) {
                    try {
                        LOGGER.info("Mock peer => Got message: " + chaincodeMessage);
                        ChaincodeMockPeerService.this.lastMessageRcvd = chaincodeMessage;
                        ChaincodeMockPeerService.this.allMessages.add(chaincodeMessage);
                        if (chaincodeMessage.getType().equals(PUT_STATE)) {
                            final ChaincodeMessage m = ChaincodeMessage.newBuilder()
                                    .setType(ChaincodeMessage.Type.RESPONSE)
                                    .setChannelId(chaincodeMessage.getChannelId())
                                    .setTxid(chaincodeMessage.getTxid())
                                    .build();
                            Thread.sleep(500);
                            ChaincodeMockPeerService.this.send(m);
                        } else if (ChaincodeMockPeerService.this.scenario.size() > 0) {
                            final ScenarioStep step = ChaincodeMockPeerService.this.scenario.get(0);
                            ChaincodeMockPeerService.this.scenario.remove(0);
                            if (step.expected(chaincodeMessage)) {
                                final List<ChaincodeMessage> nextSteps = step.next();
                                for (final ChaincodeMessage m : nextSteps) {
                                    ChaincodeMockPeerService.this.lastMessageSend = m;
                                    LOGGER.info("Mock peer => Sending response message: " + m);
                                    ChaincodeMockPeerService.this.send(m);
                                }
                            } else {
                                LOGGER.warning("Non expected message rcvd in step "
                                        + step.getClass().getSimpleName());
                            }
                            ChaincodeMockPeerService.this.lastExecutedStepNumber++;
                        }
                    } catch (final Throwable t) {
                        t.printStackTrace();
                    }
                }

                @Override
                public void onError(final Throwable throwable) {
                    System.out.println(throwable);
                }

                @Override
                public void onCompleted() {}
            };
        }
    }

    public static void checkScenarioStepEnded(
            final ChaincodeMockPeer s, final int step, final int timeout, final TimeUnit units) throws Exception {
        try {
            TimeoutUtil.runWithTimeout(
                    new Thread(() -> {
                        while (true) {
                            if (s.getLastExecutedStep() == step) {
                                return;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (final InterruptedException e) {
                            }
                        }
                    }),
                    timeout,
                    units);
        } catch (final TimeoutException e) {
            System.out.println("Got timeout, step " + step + " not finished");
        }
    }
}
