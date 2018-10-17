/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.mock.peer;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Mock peer implementation
 */
public class ChaincodeMockPeer {
    private static final Logger logger = Logger.getLogger(ChaincodeMockPeer.class.getName());

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
    public ChaincodeMockPeer(List<ScenarioStep> scenario, int port) {
        this.port = port;
        this.service = new ChaincodeMockPeerService(scenario);
        ServerBuilder<?> sb = ServerBuilder.forPort(port);
        this.server = sb.addService(this.service).build();
    }

    /**
     * Start serving requests.
     */
    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
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
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Send message from mock peer to chaincode (to start init, invoke, etc)
     *
     * @param msg
     */
    public void send(ChaincodeShim.ChaincodeMessage msg) {
        this.service.lastMessageSend = msg;
        logger.info("Mock peer => Sending message: " + msg);
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
     * Creates new isntanse of mock peer server, starts it and returns
     *
     * @param scenario
     * @return
     * @throws Exception
     */
    public static ChaincodeMockPeer startServer(List<ScenarioStep> scenario) throws Exception {
        ChaincodeMockPeer server = new ChaincodeMockPeer(scenario, 7052);
        server.start();
        return server;
    }

    private static class ChaincodeMockPeerService extends ChaincodeSupportGrpc.ChaincodeSupportImplBase {
        final List<ScenarioStep> scenario;
        int lastExecutedStepNumber;
        ChaincodeShim.ChaincodeMessage lastMessageRcvd;
        ChaincodeShim.ChaincodeMessage lastMessageSend;
        StreamObserver<ChaincodeShim.ChaincodeMessage> observer;


        public ChaincodeMockPeerService(List<ScenarioStep> scenario) {
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
                 * @param chaincodeMessage
                 */
                @Override
                public void onNext(ChaincodeShim.ChaincodeMessage chaincodeMessage) {
                    logger.info("Mock peer => Got message: " + chaincodeMessage);
                    ChaincodeMockPeerService.this.lastMessageRcvd = chaincodeMessage;
                    if (ChaincodeMockPeerService.this.scenario.size() > 0) {
                        ScenarioStep step = ChaincodeMockPeerService.this.scenario.get(0);
                        ChaincodeMockPeerService.this.scenario.remove(0);
                        if (step.expected(chaincodeMessage)) {
                            List<ChaincodeShim.ChaincodeMessage> nextSteps = step.next();
                            for (ChaincodeShim.ChaincodeMessage m : nextSteps) {
                                ChaincodeMockPeerService.this.lastMessageSend = m;
                                logger.info("Mock peer => Sending response message: " + m);
                                responseObserver.onNext(m);
                            }
                        } else {
                            logger.warning("Non expected message rcvd in step " + step.getClass().getSimpleName());
                        }
                        ChaincodeMockPeerService.this.lastExecutedStepNumber++;
                    }
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {

                }
            };
        }
    }

}
