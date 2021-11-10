/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.COMPLETED;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.ERROR;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.RESPONSE;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.fabric.traces.Traces;

/**
 * A 'Callable' implementation the has the job of invoking the chaincode, and
 * matching the response and requests.
 */
public class ChaincodeInvocationTask implements Callable<ChaincodeMessage> {

    private static Logger logger = Logger.getLogger(ChaincodeInvocationTask.class.getName());
    private static Logger perfLogger = Logger.getLogger(Logging.PERFLOGGER);

    private final String key;
    private final Type type;
    private final String txId;
    private final Consumer<ChaincodeMessage> outgoingMessageConsumer;
    // default to size 2: Based on the protocol there *should* only ever be one thing within this
    // ArrayBlockingQueue provides the correct semantics out-of-the-box.
    // We want consumers to be block waiting on a message to be 'posted' but for the putter to not be held
    // up if there's no body waiting.
    //
    // Usual case should be the main thread is waiting for something to come back
    private final ArrayBlockingQueue<ChaincodeMessage> postbox = new ArrayBlockingQueue<>(2, true);

    private final ChaincodeMessage message;
    private final Chaincode chaincode;

    /**
     *
     * @param message         The incoming message that has triggered this task into
     *                        execution
     * @param type            Is this init or invoke? (v2 Fabric deprecates init)
     * @param outgoingMessage The Consumer functional interface to send any requests
     *                        for ledger state
     * @param chaincode       A instance of the end users chaincode
     *
     */
    public ChaincodeInvocationTask(final ChaincodeMessage message, final Type type,
            final Consumer<ChaincodeMessage> outgoingMessage, final Chaincode chaincode) {

        this.key = message.getChannelId() + message.getTxid();
        this.type = type;
        this.outgoingMessageConsumer = outgoingMessage;
        this.txId = message.getTxid();
        this.chaincode = chaincode;
        this.message = message;
    }

    /**
     * Main method to power the invocation of the chaincode.
     */
    @Override
    public ChaincodeMessage call() {
        ChaincodeMessage finalResponseMessage;

        Span span = null;
        try {
            try {
                perfLogger.fine(() -> "> task:start TX::" + this.txId);

                // A key interface for the chaincode's invoke() method implementation
                // is the 'ChaincodeStub' interface. An instance of this is created
                // per transaction invocation.
                //
                // This needs to be passed the message triggering the invoke, as well
                // as the interface to be used for sending any requests to the peer
                final ChaincodeStub stub = new InvocationStubImpl(message, this);

                span = Traces.getProvider().createSpan(stub);
                // result is what will be sent to the peer as a response to this invocation
                final Chaincode.Response result;


                perfLogger.fine(() -> "> task:invoke TX::" + this.txId);

                // Call chaincode's invoke
                // Note in Fabric v2, there won't be any INIT
                if (this.type.equals(Type.INIT)) {
                    result = chaincode.init(stub);
                } else {
                    result = chaincode.invoke(stub);
                }

                perfLogger.fine(() -> "< task:invoke TX::" + this.txId);

                if (result.getStatus().getCode() >= Chaincode.Response.Status.INTERNAL_SERVER_ERROR.getCode()) {
                    // Send ERROR with entire result.Message as payload
                    logger.severe(() -> String.format("[%-8.8s] Invoke failed with error code %d. Sending %s",
                            message.getTxid(), result.getStatus().getCode(), ERROR));
                    finalResponseMessage = ChaincodeMessageFactory.newCompletedEventMessage(message.getChannelId(),
                            message.getTxid(), result, stub.getEvent());
                    if (span != null) {
                        span.setStatus(StatusCode.ERROR, result.getMessage());
                    }
                } else {
                    // Send COMPLETED with entire result as payload
                    logger.fine(() -> String.format("[%-8.8s] Invoke succeeded. Sending %s", message.getTxid(), COMPLETED));
                    finalResponseMessage = ChaincodeMessageFactory.newCompletedEventMessage(message.getChannelId(),
                            message.getTxid(), result, stub.getEvent());
                }

            } catch (InvalidProtocolBufferException | RuntimeException e) {
                logger.severe(() -> String.format("[%-8.8s] Invoke failed. Sending %s: %s", message.getTxid(), ERROR, e));
                finalResponseMessage = ChaincodeMessageFactory.newErrorEventMessage(message.getChannelId(),
                        message.getTxid(), e);
                if (span != null) {
                    span.setStatus(StatusCode.ERROR, e.getMessage());
                }
            }

            // send the final response message to the peer
            outgoingMessageConsumer.accept(finalResponseMessage);
            perfLogger.fine(() -> "< task:end TX::" + this.txId);
        } finally {
            if (span != null) {
                span.end();
            }
        }

        return null;
    }

    /**
     * Identifier of this task, channel id and transaction id.
     *
     * @return String
     */
    public String getTxKey() {
        return this.key;
    }

    /**
     * Use the Key as to determine equality.
     *
     * @param task
     * @return equality
     */
    public boolean equals(final ChaincodeInvocationTask task) {
        return key.equals(task.getTxKey());
    }

    /**
     * Posts the message that the peer has responded with to this task's request
     * Uses an 'ArrayBlockingQueue'.  This lets the producer post messages without waiting
     * for the consumer. And the consumer can block until a message is posted.
     *
     * In this case the data is only passed to the executing tasks.
     *
     * @param msg Chaincode message to pass pack
     * @throws InterruptedException should something really really go wrong
     */
    public void postMessage(final ChaincodeMessage msg) throws InterruptedException {
        // put to the postbox waiting for space to become available if needed
        postbox.put(msg);
    }

    /**
     * Send the chaincode message back to the peer.
     *
     * Implementation of the Functional interface 'InvokeChaincodeSupport'
     *
     * It will send the message, via the outgoingMessageConsumer, and then block on
     * the 'Exchanger' to wait for the response to come.
     *
     * This Exchange is an atomic operation between the thread that is running this
     * task, and the thread that is handling the communication from the peer.
     *
     * @param message The chaincode message from the peer
     * @return ByteString to be parsed by the caller
     *
     */
    protected ByteString invoke(final ChaincodeMessage message) {

        // send the message
        logger.fine(() -> "Task Sending message to the peer " + message.getTxid());
        outgoingMessageConsumer.accept(message);

        // wait for response
        ChaincodeMessage response;
        try {
            perfLogger.fine(() -> "> task:answer TX::" + message.getTxid());
            response = postbox.take();
            perfLogger.fine(() -> "< task:answer TX::" + message.getTxid());
        } catch (final InterruptedException e) {
            logger.severe(() -> "Interrupted exchanging messages ");
            throw new RuntimeException(String.format("[%-8.8s]InterruptedException received.", txId), e);
        }

        // handle response
        switch (response.getType()) {
            case RESPONSE:
                logger.fine(() -> String.format("[%-8.8s] Successful response received.", txId));
                return response.getPayload();
            case ERROR:
                logger.severe(() -> String.format("[%-8.8s] Unsuccessful response received.", txId));
                throw new RuntimeException(String.format("[%-8.8s]Unsuccessful response received.", txId));
            default:
                logger.severe(() -> String.format("[%-8.8s] Unexpected %s response received. Expected %s or %s.", txId,
                        response.getType(), RESPONSE, ERROR));
                throw new RuntimeException(String.format("[%-8.8s] Unexpected %s response received. Expected %s or %s.",
                        txId, response.getType(), RESPONSE, ERROR));
        }

    }

}
