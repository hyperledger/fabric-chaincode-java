/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeSpec;
import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.*;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage.Response;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage.Response.Builder;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.helper.Channel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.*;

public class Handler {

    private static Logger logger = Logger.getLogger(Handler.class.getName());
    private final Chaincode chaincode;
    private final Map<String, Boolean> isTransaction = new HashMap<>();
    private final Map<String, Channel<ChaincodeMessage>> responseChannel = new HashMap<>();
    private Channel<ChaincodeMessage> outboundChaincodeMessages = new Channel<>();
    private CCState state;

    public Handler(ChaincodeID chaincodeId, Chaincode chaincode) {
        this.chaincode = chaincode;
        this.state = CCState.CREATED;
        queueOutboundChaincodeMessage(newRegisterChaincodeMessage(chaincodeId));
    }

    public ChaincodeMessage nextOutboundChaincodeMessage() {
        try {
            return outboundChaincodeMessages.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Unable to get next outbound ChaincodeMessage");
            }
            return newErrorEventMessage("UNKNOWN", "UNKNOWN", e);
        }
    }

    public void onChaincodeMessage(ChaincodeMessage chaincodeMessage) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(format("[%-8.8s] %s", chaincodeMessage.getTxid(), toJsonString(chaincodeMessage)));
        }
        handleChaincodeMessage(chaincodeMessage);
    }

    private synchronized void handleChaincodeMessage(ChaincodeMessage message) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(format("[%-8.8s] Handling ChaincodeMessage of type: %s, handler state %s", message.getTxid(), message.getType(), this.state));
        }
        if (message.getType() == KEEPALIVE) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("[%-8.8s] Received KEEPALIVE: nothing to do", message.getTxid()));
            }
            return;
        }
        switch (this.state) {
            case CREATED:
                handleCreated(message);
                break;
            case ESTABLISHED:
                handleEstablished(message);
                break;
            case READY:
                handleReady(message);
                break;
            default:
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning(format("[%-8.8s] Received %s: cannot handle", message.getTxid(), message.getType()));
                }
                break;
        }
    }

    private void handleCreated(ChaincodeMessage message) {
        if (message.getType() == REGISTERED) {
            this.state = CCState.ESTABLISHED;
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("[%-8.8s] Received REGISTERED: moving to established state", message.getTxid()));
            }
        } else {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(format("[%-8.8s] Received %s: cannot handle", message.getTxid(), message.getType()));
            }
        }
    }

    private void handleEstablished(ChaincodeMessage message) {
        if (message.getType() == READY) {
            this.state = CCState.READY;
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("[%-8.8s] Received READY: ready for invocations", message.getTxid()));
            }
        } else {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(format("[%-8.8s] Received %s: cannot handle", message.getTxid(), message.getType()));
            }
        }
    }

    private void handleReady(ChaincodeMessage message) {
        switch (message.getType()) {
            case RESPONSE:
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format("[%-8.8s] Received RESPONSE: publishing to channel", message.getTxid()));
                }
                sendChannel(message);
                break;
            case ERROR:
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format("[%-8.8s] Received ERROR: publishing to channel", message.getTxid()));
                }
                sendChannel(message);
                break;
            case INIT:
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format("[%-8.8s] Received INIT: invoking chaincode init", message.getTxid()));
                }
                handleInit(message);
                break;
            case TRANSACTION:
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format("[%-8.8s] Received TRANSACTION: invoking chaincode", message.getTxid()));
                }
                handleTransaction(message);
                break;
            default:
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning(format("[%-8.8s] Received %s: cannot handle", message.getTxid(), message.getType()));
                }
                break;
        }
    }

    private String getTxKey(final String channelId, final String txid) {
        return channelId + txid;
    }

    private void queueOutboundChaincodeMessage(ChaincodeMessage chaincodeMessage) {
        this.outboundChaincodeMessages.add(chaincodeMessage);
    }

    private synchronized Channel<ChaincodeMessage> aquireResponseChannelForTx(final String channelId, final String txId) {
        final Channel<ChaincodeMessage> channel = new Channel<>();
        String key = getTxKey(channelId, txId);
        if (this.responseChannel.putIfAbsent(key, channel) != null) {
            throw new IllegalStateException(format("[%-8.8s] Response channel already exists. Another request must be pending.", txId));
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(format("[%-8.8s] Response channel created.", txId));
        }
        return channel;
    }

    private synchronized void sendChannel(ChaincodeMessage message) {
        String key = getTxKey(message.getChannelId(), message.getTxid());
        if (!responseChannel.containsKey(key)) {
            throw new IllegalStateException(format("[%-8.8s] sendChannel does not exist", message.getTxid()));
        }
        responseChannel.get(key).add(message);
    }

    private ChaincodeMessage receiveChannel(Channel<ChaincodeMessage> channel) {
        try {
            return channel.take();
        } catch (InterruptedException e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("channel.take() failed with InterruptedException");
            }

            // Channel has been closed?
            // TODO
            return null;
        }
    }

    private synchronized void releaseResponseChannelForTx(String channelId, String txId) {
        String key = getTxKey(channelId, txId);
        final Channel<ChaincodeMessage> channel = responseChannel.remove(key);
        if (channel != null) channel.close();
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(format("[%-8.8s] Response channel closed.", txId));
        }
    }

    /**
     * Marks a CHANNELID+UUID as either a transaction or a query
     *
     * @param uuid          ID to be marked
     * @param isTransaction true for transaction, false for query
     * @return whether or not the UUID was successfully marked
     */
    private synchronized boolean markIsTransaction(String channelId, String uuid, boolean isTransaction) {
        if (this.isTransaction == null) {
            return false;
        }

        String key = getTxKey(channelId, uuid);
        this.isTransaction.put(key, isTransaction);
        return true;
    }

    private synchronized void deleteIsTransaction(String channelId, String uuid) {
        String key = getTxKey(channelId, uuid);
        isTransaction.remove(key);
    }

    /**
     * Handles requests to initialize chaincode
     *
     * @param message chaincode to be initialized
     */
    private void handleInit(ChaincodeMessage message) {
        new Thread(() -> {
            try {

                // Get the function and args from Payload
                final ChaincodeInput input = ChaincodeInput.parseFrom(message.getPayload());

                // Mark as a transaction (allow put/del state)
                markIsTransaction(message.getChannelId(), message.getTxid(), true);

                // Create the ChaincodeStub which the chaincode can use to
                // callback
                final ChaincodeStub stub = new ChaincodeStubImpl(message.getChannelId(), message.getTxid(), this, input.getArgsList(), message.getProposal());

                // Call chaincode's init
                final Chaincode.Response result = chaincode.init(stub);

                if (result.getStatus().getCode() >= Chaincode.Response.Status.INTERNAL_SERVER_ERROR.getCode()) {
                    // Send ERROR with entire result.Message as payload
                    logger.severe(format("[%-8.8s] Init failed. Sending %s", message.getTxid(), ERROR));
                    queueOutboundChaincodeMessage(newErrorEventMessage(message.getChannelId(), message.getTxid(), result.getMessage(), stub.getEvent()));
                } else {
                    // Send COMPLETED with entire result as payload
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(format(format("[%-8.8s] Init succeeded. Sending %s", message.getTxid(), COMPLETED)));
                    }
                    queueOutboundChaincodeMessage(newCompletedEventMessage(message.getChannelId(), message.getTxid(), result, stub.getEvent()));
                }
            } catch (InvalidProtocolBufferException | RuntimeException e) {
                logger.severe(format("[%-8.8s] Init failed. Sending %s: %s", message.getTxid(), ERROR, e));
                queueOutboundChaincodeMessage(newErrorEventMessage(message.getChannelId(), message.getTxid(), e));
            } finally {
                // delete isTransaction entry
                deleteIsTransaction(message.getChannelId(), message.getTxid());
            }
        }).start();
    }

    // handleTransaction Handles request to execute a transaction.
    private void handleTransaction(ChaincodeMessage message) {
        new Thread(() -> {
            try {

                // Get the function and args from Payload
                final ChaincodeInput input = ChaincodeInput.parseFrom(message.getPayload());

                // Mark as a transaction (allow put/del state)
                markIsTransaction(message.getChannelId(), message.getTxid(), true);

                // Create the ChaincodeStub which the chaincode can use to
                // callback
                final ChaincodeStub stub = new ChaincodeStubImpl(message.getChannelId(), message.getTxid(), this, input.getArgsList(), message.getProposal());

                // Call chaincode's invoke
                final Chaincode.Response result = chaincode.invoke(stub);

                if (result.getStatus().getCode() >= Chaincode.Response.Status.INTERNAL_SERVER_ERROR.getCode()) {
                    // Send ERROR with entire result.Message as payload
                    logger.severe(format("[%-8.8s] Invoke failed. Sending %s", message.getTxid(), ERROR));
                    queueOutboundChaincodeMessage(newErrorEventMessage(message.getChannelId(), message.getTxid(), result.getMessage(), stub.getEvent()));
                } else {
                    // Send COMPLETED with entire result as payload
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(format(format("[%-8.8s] Invoke succeeded. Sending %s", message.getTxid(), COMPLETED)));
                    }
                    queueOutboundChaincodeMessage(newCompletedEventMessage(message.getChannelId(), message.getTxid(), result, stub.getEvent()));
                }

            } catch (InvalidProtocolBufferException | RuntimeException e) {
                logger.severe(format("[%-8.8s] Invoke failed. Sending %s: %s", message.getTxid(), ERROR, e));
                queueOutboundChaincodeMessage(newErrorEventMessage(message.getChannelId(), message.getTxid(), e));
            } finally {
                // delete isTransaction entry
                deleteIsTransaction(message.getChannelId(), message.getTxid());
            }
        }).start();
    }

    // handleGetState communicates with the validator to fetch the requested state information from the ledger.
    ByteString getState(String channelId, String txId, String collection, String key) {
        return invokeChaincodeSupport(newGetStateEventMessage(channelId, txId, collection, key));
    }

    private boolean isTransaction(String channelId, String uuid) {
        String key = getTxKey(channelId, uuid);
        return isTransaction.containsKey(key) && isTransaction.get(key);
    }

    void putState(String channelId, String txId, String collection, String key, ByteString value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(format("[%-8.8s] Inside putstate (\"%s\":\"%s\":\"%s\"), isTransaction = %s", txId, collection, key, value, isTransaction(channelId, txId)));
        }
        if (!isTransaction(channelId, txId)) throw new IllegalStateException("Cannot put state in query context");
        invokeChaincodeSupport(newPutStateEventMessage(channelId, txId, collection, key, value));
    }

    void deleteState(String channelId, String txId, String collection, String key) {
        if (!isTransaction(channelId, txId)) throw new RuntimeException("Cannot del state in query context");
        invokeChaincodeSupport(newDeleteStateEventMessage(channelId, txId, collection, key));
    }

    QueryResponse getStateByRange(String channelId, String txId, String collection, String startKey, String endKey) {
        return invokeQueryResponseMessage(channelId, txId, GET_STATE_BY_RANGE, GetStateByRange.newBuilder()
                .setCollection(collection)
                .setStartKey(startKey)
                .setEndKey(endKey)
                .build().toByteString());
    }

    QueryResponse queryStateNext(String channelId, String txId, String queryId) {
        return invokeQueryResponseMessage(channelId, txId, QUERY_STATE_NEXT, QueryStateNext.newBuilder()
                .setId(queryId)
                .build().toByteString());
    }

    void queryStateClose(String channelId, String txId, String queryId) {
        invokeQueryResponseMessage(channelId, txId, QUERY_STATE_CLOSE, QueryStateClose.newBuilder()
                .setId(queryId)
                .build().toByteString());
    }

    QueryResponse getQueryResult(String channelId, String txId, String collection, String query) {
        return invokeQueryResponseMessage(channelId, txId, GET_QUERY_RESULT, GetQueryResult.newBuilder()
                .setCollection(collection)
                .setQuery(query)
                .build().toByteString());
    }

    QueryResponse getHistoryForKey(String channelId, String txId, String key) {
        return invokeQueryResponseMessage(channelId, txId, Type.GET_HISTORY_FOR_KEY, GetQueryResult.newBuilder()
                .setQuery(key)
                .build().toByteString());
    }

    private QueryResponse invokeQueryResponseMessage(String channelId, String txId, ChaincodeMessage.Type type, ByteString payload) {
        try {
            return QueryResponse.parseFrom(invokeChaincodeSupport(newEventMessage(type, channelId, txId, payload)));
        } catch (InvalidProtocolBufferException e) {
            logger.severe(String.format("[%-8.8s] unmarshall error", txId));
            throw new RuntimeException("Error unmarshalling QueryResponse.", e);
        }
    }

    private ByteString invokeChaincodeSupport(final ChaincodeMessage message) {
        final String channelId = message.getChannelId();
        final String txId = message.getTxid();

        try {
            // create a new response channel
            Channel<ChaincodeMessage> responseChannel = aquireResponseChannelForTx(channelId, txId);

            // send the message
            queueOutboundChaincodeMessage(message);

            // wait for response
            final ChaincodeMessage response = receiveChannel(responseChannel);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("[%-8.8s] %s response received.", txId, response.getType()));
            }

            // handle response
            switch (response.getType()) {
                case RESPONSE:
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(format("[%-8.8s] Successful response received.", txId));
                    }
                    return response.getPayload();
                case ERROR:
                    logger.severe(format("[%-8.8s] Unsuccessful response received.", txId));
                    throw new RuntimeException(format("[%-8.8s]Unsuccessful response received.", txId));
                default:
                    logger.severe(format("[%-8.8s] Unexpected %s response received. Expected %s or %s.", txId, response.getType(), RESPONSE, ERROR));
                    throw new RuntimeException(format("[%-8.8s]Unexpected %s response received. Expected %s or %s.", txId, response.getType(), RESPONSE, ERROR));
            }
        } finally {
            releaseResponseChannelForTx(channelId, txId);
        }
    }

    Chaincode.Response invokeChaincode(String channelId, String txId, String chaincodeName, List<byte[]> args) {
        try {
            // create invocation specification of the chaincode to invoke
            final ChaincodeSpec invocationSpec = ChaincodeSpec.newBuilder()
                    .setChaincodeId(ChaincodeID.newBuilder()
                            .setName(chaincodeName)
                            .build())
                    .setInput(ChaincodeInput.newBuilder()
                            .addAllArgs(args.stream().map(ByteString::copyFrom).collect(Collectors.toList()))
                            .build())
                    .build();

            // invoke other chaincode
            final ByteString payload = invokeChaincodeSupport(newInvokeChaincodeMessage(channelId, txId, invocationSpec.toByteString()));

            // response message payload should be yet another chaincode
            // message (the actual response message)
            final ChaincodeMessage responseMessage = ChaincodeMessage.parseFrom(payload);
            // the actual response message must be of type COMPLETED
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("[%-8.8s] %s response received from other chaincode.", txId, responseMessage.getType()));
            }
            if (responseMessage.getType() == COMPLETED) {
                // success
                return toChaincodeResponse(Response.parseFrom(responseMessage.getPayload()));
            } else {
                // error
                return newErrorChaincodeResponse(responseMessage.getPayload().toStringUtf8());
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJsonString(ChaincodeMessage message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            return String.format("{ Type: %s, TxId: %s }", message.getType(), message.getTxid());
        }
    }

    private static Chaincode.Response newErrorChaincodeResponse(String message) {
        return new Chaincode.Response(Chaincode.Response.Status.INTERNAL_SERVER_ERROR, message, null);
    }

    private static ChaincodeMessage newGetStateEventMessage(final String channelId, final String txId, final String collection, final String key) {
        return newEventMessage(GET_STATE, channelId, txId, GetState.newBuilder()
                .setCollection(collection)
                .setKey(key)
                .build().toByteString());
    }

    private static ChaincodeMessage newPutStateEventMessage(final String channelId, final String txId, final String collection, final String key, final ByteString value) {
        return newEventMessage(PUT_STATE, channelId, txId, PutState.newBuilder()
                .setCollection(collection)
                .setKey(key)
                .setValue(value)
                .build().toByteString());
    }

    private static ChaincodeMessage newDeleteStateEventMessage(final String channelId, final String txId, final String collection, final String key) {
        return newEventMessage(DEL_STATE, channelId, txId, DelState.newBuilder()
                .setCollection(collection)
                .setKey(key)
                .build().toByteString());
    }

    private static ChaincodeMessage newErrorEventMessage(final String channelId, final String txId, final Throwable throwable) {
        return newErrorEventMessage(channelId, txId, printStackTrace(throwable));
    }

    private static ChaincodeMessage newErrorEventMessage(final String channelId, final String txId, final String message) {
        return newErrorEventMessage(channelId, txId, message, null);
    }

    private static ChaincodeMessage newErrorEventMessage(final String channelId, final String txId, final String message, final ChaincodeEvent event) {
        return newEventMessage(ERROR, channelId, txId, ByteString.copyFromUtf8(message), event);
    }

    private static ChaincodeMessage newCompletedEventMessage(final String channelId, final String txId, final Chaincode.Response response, final ChaincodeEvent event) {
        ChaincodeMessage message = newEventMessage(COMPLETED, channelId, txId, toProtoResponse(response).toByteString(), event);
        return message;
    }

    private static ChaincodeMessage newInvokeChaincodeMessage(final String channelId, final String txId, final ByteString payload) {
        return newEventMessage(INVOKE_CHAINCODE, channelId, txId, payload, null);
    }

    private static ChaincodeMessage newRegisterChaincodeMessage(final ChaincodeID chaincodeId) {
        return ChaincodeMessage.newBuilder()
                .setType(REGISTER)
                .setPayload(chaincodeId.toByteString())
                .build();
    }

    private static ChaincodeMessage newEventMessage(final Type type, final String channelId, final String txId, final ByteString payload) {
        return newEventMessage(type, channelId, txId, payload, null);
    }

    private static ChaincodeMessage newEventMessage(final Type type, final String channelId, final String txId, final ByteString payload, final ChaincodeEvent event) {
        if (event == null) {
            ChaincodeMessage chaincodeMessage = ChaincodeMessage.newBuilder()
                    .setType(type)
                    .setChannelId(channelId)
                    .setTxid(txId)
                    .setPayload(payload)
                    .build();
            return chaincodeMessage;
        } else {
            return ChaincodeMessage.newBuilder()
                    .setType(type)
                    .setChannelId(channelId)
                    .setTxid(txId)
                    .setPayload(payload)
                    .setChaincodeEvent(event)
                    .build();
        }
    }

    private static Response toProtoResponse(Chaincode.Response response) {
        final Builder builder = Response.newBuilder();
        builder.setStatus(response.getStatus().getCode());
        if (response.getMessage() != null) {
            builder.setMessage(response.getMessage());
        }
        if (response.getPayload() != null) {
            builder.setPayload(ByteString.copyFrom(response.getPayload()));
        }
        return builder.build();
    }

    private static Chaincode.Response toChaincodeResponse(Response response) {
        return new Chaincode.Response(
                Chaincode.Response.Status.forCode(response.getStatus()),
                response.getMessage(),
                response.getPayload() == null ? null : response.getPayload().toByteArray()
        );
    }

    private static String printStackTrace(Throwable throwable) {
        if (throwable == null) return null;
        final StringWriter buffer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(buffer));
        return buffer.toString();
    }

    public enum CCState {
        CREATED,
        ESTABLISHED,
        READY
    }

    CCState getState() {
        return this.state;
    }

}
