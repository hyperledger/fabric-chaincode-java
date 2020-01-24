/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.mock.peer;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;
import org.hyperledger.fabric.shim.utils.TimeoutUtil;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * Mock peer implementation
 */
public class ChaincodeMockPeer {
    private static final Logger LOGGER = Logger.getLogger(ChaincodeMockPeer.class.getName());

    private final int port;
    private final Server server;
    private final ChaincodeMockPeerService service;

    /**
     * Constructor
     *
     * @param scenario list of scenario steps
     * @param port     mock peer communication port
     * @throws IOException
     */
    public ChaincodeMockPeer(final List<ScenarioStep> scenario, final int port) {
        this.port = port;
        this.service = new ChaincodeMockPeerService(scenario);
        final ServerBuilder<?> sb = ServerBuilder.forPort(port);
        this.server = sb.addService(this.service).build();
    }

    /**
     * Start serving requests.
     */
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

    /**
     * Stop serving requests and shutdown resources.
     */
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
    public void send(final ChaincodeShim.ChaincodeMessage msg) {
        this.service.lastMessageSend = msg;
        LOGGER.info("Mock peer => Sending message: " + msg);
        this.service.observer.onNext(msg);
    }

    /**
     * Check last executed step number, to check where in scenario we stopped
     *
     * @return
     */
    public int getLastExecutedStep() {
        return this.service.lastExecutedStepNumber;
    }

    /**
     * @return last received message from chaincode
     */
    public ChaincodeShim.ChaincodeMessage getLastMessageRcvd() {
        return this.service.lastMessageRcvd;
    }

    /**
     * @return last message sent by peer to chaincode
     */
    public ChaincodeShim.ChaincodeMessage getLastMessageSend() {
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
        private ChaincodeShim.ChaincodeMessage lastMessageRcvd;
        private ChaincodeShim.ChaincodeMessage lastMessageSend;
        private StreamObserver<ChaincodeShim.ChaincodeMessage> observer;

        ChaincodeMockPeerService(final List<ScenarioStep> scenario) {
            this.scenario = scenario;
            this.lastExecutedStepNumber = 0;
        }

        /**
         * Attaching observer to steams
         *
         * @param responseObserver
         * @return
         */
        @Override
        public StreamObserver<ChaincodeShim.ChaincodeMessage> register(final StreamObserver<ChaincodeShim.ChaincodeMessage> responseObserver) {
            observer = responseObserver;
            return new StreamObserver<ChaincodeShim.ChaincodeMessage>() {

                /**
                 * Handling incoming messages
                 *
                 * @param chaincodeMessage
                 */
                @Override
                public void onNext(final ChaincodeShim.ChaincodeMessage chaincodeMessage) {
                    LOGGER.info("Mock peer => Got message: " + chaincodeMessage);
                    ChaincodeMockPeerService.this.lastMessageRcvd = chaincodeMessage;
                    if (ChaincodeMockPeerService.this.scenario.size() > 0) {
                        final ScenarioStep step = ChaincodeMockPeerService.this.scenario.get(0);
                        ChaincodeMockPeerService.this.scenario.remove(0);
                        if (step.expected(chaincodeMessage)) {
                            final List<ChaincodeShim.ChaincodeMessage> nextSteps = step.next();
                            for (final ChaincodeShim.ChaincodeMessage m : nextSteps) {
                                ChaincodeMockPeerService.this.lastMessageSend = m;
                                LOGGER.info("Mock peer => Sending response message: " + m);
                                responseObserver.onNext(m);
                            }
                        } else {
                            LOGGER.warning("Non expected message rcvd in step " + step.getClass().getSimpleName());
                        }
                        ChaincodeMockPeerService.this.lastExecutedStepNumber++;
                    }
                }

                @Override
                public void onError(final Throwable throwable) {

                }

                @Override
                public void onCompleted() {

                }
            };
        }
    }

    public static void checkScenarioStepEnded(final ChaincodeMockPeer s, final int step, final int timeout, final TimeUnit units) throws Exception {
        try {
            TimeoutUtil.runWithTimeout(new Thread(() -> {
                while (true) {
                    if (s.getLastExecutedStep() == step) {
                        return;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (final InterruptedException e) {
                    }
                }
            }), timeout, units);
        } catch (final TimeoutException e) {
            fail("Got timeout, step " + step + " not finished");
        }
    }

}
