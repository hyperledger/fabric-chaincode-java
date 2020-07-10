/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.impl;

import static java.util.stream.Collectors.toList;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.COMPLETED;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.GET_HISTORY_FOR_KEY;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.GET_PRIVATE_DATA_HASH;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.GET_QUERY_RESULT;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.GET_STATE_BY_RANGE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.common.Common.ChannelHeader;
import org.hyperledger.fabric.protos.common.Common.Header;
import org.hyperledger.fabric.protos.common.Common.HeaderType;
import org.hyperledger.fabric.protos.common.Common.SignatureHeader;
import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult;
import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult.KV;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeSpec;
import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.GetQueryResult;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.GetState;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.GetStateByRange;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResultBytes;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.StateMetadataResult;
import org.hyperledger.fabric.protos.peer.ProposalPackage.ChaincodeProposalPayload;
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal;
import org.hyperledger.fabric.protos.peer.ProposalPackage.SignedProposal;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage;
import org.hyperledger.fabric.protos.peer.TransactionPackage;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

class InvocationStubImpl implements ChaincodeStub {

    private static final String UNSPECIFIED_START_KEY = new String(Character.toChars(0x000001));
    private static final String UNSPECIFIED_END_KEY = "";
    private static final Logger LOGGER = Logger.getLogger(InvocationStubImpl.class.getName());

    public static final String MAX_UNICODE_RUNE = "\udbff\udfff";
    private static final String CORE_PEER_LOCALMSPID = "CORE_PEER_LOCALMSPID";
    private final String channelId;
    private final String txId;
    private final ChaincodeInvocationTask handler;
    private final List<ByteString> args;
    private final SignedProposal signedProposal;
    private final Instant txTimestamp;
    private final ByteString creator;
    private final Map<String, ByteString> transientMap;
    private final byte[] binding;
    private ChaincodeEvent event;

    /**
     *
     * @param message
     * @param handler
     * @throws InvalidProtocolBufferException
     */
    InvocationStubImpl(final ChaincodeMessage message, final ChaincodeInvocationTask handler)
            throws InvalidProtocolBufferException {
        this.channelId = message.getChannelId();
        this.txId = message.getTxid();
        this.handler = handler;
        final ChaincodeInput input = ChaincodeInput.parseFrom(message.getPayload());

        this.args = Collections.unmodifiableList(input.getArgsList());
        this.signedProposal = message.getProposal();
        if (this.signedProposal == null || this.signedProposal.getProposalBytes().isEmpty()) {
            this.creator = null;
            this.txTimestamp = null;
            this.transientMap = Collections.emptyMap();
            this.binding = null;
        } else {
            try {
                final Proposal proposal = Proposal.parseFrom(signedProposal.getProposalBytes());
                final Header header = Header.parseFrom(proposal.getHeader());
                final ChannelHeader channelHeader = ChannelHeader.parseFrom(header.getChannelHeader());
                validateProposalType(channelHeader);
                final SignatureHeader signatureHeader = SignatureHeader.parseFrom(header.getSignatureHeader());
                final ChaincodeProposalPayload chaincodeProposalPayload = ChaincodeProposalPayload
                        .parseFrom(proposal.getPayload());
                final Timestamp timestamp = channelHeader.getTimestamp();

                this.txTimestamp = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
                this.creator = signatureHeader.getCreator();
                this.transientMap = chaincodeProposalPayload.getTransientMapMap();
                this.binding = computeBinding(channelHeader, signatureHeader);
            } catch (InvalidProtocolBufferException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private byte[] computeBinding(final ChannelHeader channelHeader, final SignatureHeader signatureHeader)
            throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(signatureHeader.getNonce().asReadOnlyByteBuffer());
        messageDigest.update(this.creator.asReadOnlyByteBuffer());
        final ByteBuffer epochBytes = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                .putLong(channelHeader.getEpoch());
        epochBytes.flip();
        messageDigest.update(epochBytes);
        return messageDigest.digest();
    }

    private void validateProposalType(final ChannelHeader channelHeader) {
        switch (Common.HeaderType.forNumber(channelHeader.getType())) {
        case ENDORSER_TRANSACTION:
        case CONFIG:
            return;
        default:
            throw new RuntimeException(
                    String.format("Unexpected transaction type: %s", HeaderType.forNumber(channelHeader.getType())));
        }
    }

    @Override
    public List<byte[]> getArgs() {
        return args.stream().map(x -> x.toByteArray()).collect(Collectors.toList());
    }

    @Override
    public List<String> getStringArgs() {
        return args.stream().map(x -> x.toStringUtf8()).collect(Collectors.toList());
    }

    @Override
    public String getFunction() {
        return getStringArgs().size() > 0 ? getStringArgs().get(0) : null;
    }

    @Override
    public List<String> getParameters() {
        return getStringArgs().stream().skip(1).collect(toList());
    }

    @Override
    public void setEvent(final String name, final byte[] payload) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("event name can not be nil string");
        }
        if (payload != null) {
            this.event = ChaincodeEvent.newBuilder().setEventName(name).setPayload(ByteString.copyFrom(payload))
                    .build();
        } else {
            this.event = ChaincodeEvent.newBuilder().setEventName(name).build();
        }
    }

    @Override
    public ChaincodeEvent getEvent() {
        return event;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getTxId() {
        return txId;
    }

    @Override
    public byte[] getState(final String key) {
        return this.handler.invoke(ChaincodeMessageFactory.newGetStateEventMessage(channelId, txId, "", key))
                .toByteArray();
    }

    @Override
    public byte[] getStateValidationParameter(final String key) {

        final ByteString payload = handler
                .invoke(ChaincodeMessageFactory.newGetStateMetadataEventMessage(channelId, txId, "", key));
        try {
            final StateMetadataResult stateMetadataResult = StateMetadataResult.parseFrom(payload);
            final Map<String, ByteString> stateMetadataMap = new HashMap<>();
            stateMetadataResult.getEntriesList()
                    .forEach(entry -> stateMetadataMap.put(entry.getMetakey(), entry.getValue()));

            if (stateMetadataMap.containsKey(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())) {
                return stateMetadataMap.get(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())
                        .toByteArray();
            }
        } catch (final InvalidProtocolBufferException e) {
            LOGGER.severe(String.format("[%-8.8s] unmarshalling error", txId));
            throw new RuntimeException("Error unmarshalling StateMetadataResult.", e);
        }

        return null;

    }

    @Override
    public void putState(final String key, final byte[] value) {
        validateKey(key);
        this.handler.invoke(
                ChaincodeMessageFactory.newPutStateEventMessage(channelId, txId, "", key, ByteString.copyFrom(value)));
    }

    @Override
    public void setStateValidationParameter(final String key, final byte[] value) {
        validateKey(key);
        final ChaincodeMessage msg = ChaincodeMessageFactory.newPutStateMetadataEventMessage(channelId, txId, "", key,
                TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(), ByteString.copyFrom(value));
        this.handler.invoke(msg);
    }

    @Override
    public void delState(final String key) {
        final ChaincodeMessage msg = ChaincodeMessageFactory.newDeleteStateEventMessage(channelId, txId, "", key);
        this.handler.invoke(msg);
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByRange(final String startKey, final String endKey) {
        String start = startKey;
        String end = endKey;

        if (startKey == null || startKey.isEmpty()) {
            start = UNSPECIFIED_START_KEY;
        }
        if (endKey == null) {
            end = UNSPECIFIED_END_KEY;
        }
        CompositeKey.validateSimpleKeys(start, end);

        return executeGetStateByRange("", start, end);
    }

    private QueryResultsIterator<KeyValue> executeGetStateByRange(final String collection, final String startKey,
            final String endKey) {

        final ByteString requestPayload = GetStateByRange.newBuilder().setCollection(collection).setStartKey(startKey)
                .setEndKey(endKey).build().toByteString();

        final ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_STATE_BY_RANGE, channelId,
                txId, requestPayload);
        final ByteString response = handler.invoke(requestMessage);

        return new QueryResultsIteratorImpl<KeyValue>(this.handler, channelId, txId, response,
                queryResultBytesToKv.andThen(KeyValueImpl::new));

    }

    private final Function<QueryResultBytes, KV> queryResultBytesToKv = new Function<QueryResultBytes, KV>() {
        @Override
        public KV apply(final QueryResultBytes queryResultBytes) {
            try {
                return KV.parseFrom(queryResultBytes.getResultBytes());
            } catch (final InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }

    };

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(final String startKey,
            final String endKey, final int pageSize, final String bookmark) {

        String start = startKey;
        String end = endKey;

        if (startKey == null || startKey.isEmpty()) {
            start = UNSPECIFIED_START_KEY;
        }
        if (endKey == null) {
            end = UNSPECIFIED_END_KEY;
        }

        CompositeKey.validateSimpleKeys(start, end);

        final ChaincodeShim.QueryMetadata queryMetadata = ChaincodeShim.QueryMetadata.newBuilder().setBookmark(bookmark)
                .setPageSize(pageSize).build();

        return executeGetStateByRangeWithMetadata("", start, end, queryMetadata.toByteString());
    }

    private QueryResultsIteratorWithMetadataImpl<KeyValue> executeGetStateByRangeWithMetadata(final String collection,
            final String startKey, final String endKey, final ByteString metadata) {

        final ByteString payload = GetStateByRange.newBuilder().setCollection(collection).setStartKey(startKey)
                .setEndKey(endKey).setMetadata(metadata).build().toByteString();

        final ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_STATE_BY_RANGE, channelId,
                txId, payload);

        final ByteString response = this.handler.invoke(requestMessage);

        return new QueryResultsIteratorWithMetadataImpl<>(this.handler, getChannelId(), getTxId(), response,
                queryResultBytesToKv.andThen(KeyValueImpl::new));

    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final String compositeKey) {

        CompositeKey key;

        if (compositeKey.startsWith(CompositeKey.NAMESPACE)) {
            key = CompositeKey.parseCompositeKey(compositeKey);
        } else {
            key = new CompositeKey(compositeKey);
        }

        return getStateByPartialCompositeKey(key);
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final String objectType,
            final String... attributes) {
        return getStateByPartialCompositeKey(new CompositeKey(objectType, attributes));
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final CompositeKey compositeKey) {

        String cKeyAsString;

        if (compositeKey == null) {
            cKeyAsString = new CompositeKey(UNSPECIFIED_START_KEY).toString();
        } else {
            cKeyAsString = compositeKey.toString();
        }

        return executeGetStateByRange("", cKeyAsString, cKeyAsString + MAX_UNICODE_RUNE);
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(
            final CompositeKey compositeKey, final int pageSize, final String bookmark) {

        String cKeyAsString;

        if (compositeKey == null) {
            cKeyAsString = new CompositeKey(UNSPECIFIED_START_KEY).toString();
        } else {
            cKeyAsString = compositeKey.toString();
        }

        final ChaincodeShim.QueryMetadata queryMetadata = ChaincodeShim.QueryMetadata.newBuilder().setBookmark(bookmark)
                .setPageSize(pageSize).build();

        return executeGetStateByRangeWithMetadata("", cKeyAsString, cKeyAsString + MAX_UNICODE_RUNE,
                queryMetadata.toByteString());
    }

    @Override
    public CompositeKey createCompositeKey(final String objectType, final String... attributes) {
        return new CompositeKey(objectType, attributes);
    }

    @Override
    public CompositeKey splitCompositeKey(final String compositeKey) {
        return CompositeKey.parseCompositeKey(compositeKey);
    }

    @Override
    public QueryResultsIterator<KeyValue> getQueryResult(final String query) {

        final ByteString requestPayload = GetQueryResult.newBuilder().setCollection("").setQuery(query).build()
                .toByteString();
        final ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_QUERY_RESULT, channelId,
                txId, requestPayload);
        final ByteString response = handler.invoke(requestMessage);

        return new QueryResultsIteratorImpl<KeyValue>(this.handler, channelId, txId, response,
                queryResultBytesToKv.andThen(KeyValueImpl::new));
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(final String query,
            final int pageSize, final String bookmark) {

        final ByteString queryMetadataPayload = ChaincodeShim.QueryMetadata.newBuilder().setBookmark(bookmark)
                .setPageSize(pageSize).build().toByteString();
        final ByteString requestPayload = GetQueryResult.newBuilder().setCollection("").setQuery(query)
                .setMetadata(queryMetadataPayload).build().toByteString();
        final ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_QUERY_RESULT, channelId,
                txId, requestPayload);
        final ByteString response = handler.invoke(requestMessage);

        return new QueryResultsIteratorWithMetadataImpl<KeyValue>(this.handler, channelId, txId, response,
                queryResultBytesToKv.andThen(KeyValueImpl::new));

    }

    @Override
    public QueryResultsIterator<KeyModification> getHistoryForKey(final String key) {

        final ByteString requestPayload = GetQueryResult.newBuilder().setCollection("").setQuery(key).build()
                .toByteString();
        final ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_HISTORY_FOR_KEY, channelId,
                txId, requestPayload);
        final ByteString response = handler.invoke(requestMessage);

        return new QueryResultsIteratorImpl<KeyModification>(this.handler, channelId, txId, response,
                queryResultBytesToKeyModification.andThen(KeyModificationImpl::new));

    }

    private final Function<QueryResultBytes, KvQueryResult.KeyModification> queryResultBytesToKeyModification =
            new Function<QueryResultBytes, KvQueryResult.KeyModification>() {
        @Override
        public KvQueryResult.KeyModification apply(final QueryResultBytes queryResultBytes) {
            try {
                return KvQueryResult.KeyModification.parseFrom(queryResultBytes.getResultBytes());
            } catch (final InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @Override
    public byte[] getPrivateData(final String collection, final String key) {
        validateCollection(collection);
        return this.handler.invoke(ChaincodeMessageFactory.newGetStateEventMessage(channelId, txId, collection, key))
                .toByteArray();
    }

    @Override
    public byte[] getPrivateDataHash(final String collection, final String key) {

        validateCollection(collection);

        final ByteString requestPayload = GetState.newBuilder().setCollection(collection).setKey(key).build()
                .toByteString();
        final ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_PRIVATE_DATA_HASH,
                channelId, txId, requestPayload);

        return handler.invoke(requestMessage).toByteArray();
    }

    @Override
    public byte[] getPrivateDataValidationParameter(final String collection, final String key) {
        validateCollection(collection);

        final ByteString payload = handler
                .invoke(ChaincodeMessageFactory.newGetStateMetadataEventMessage(channelId, txId, collection, key));
        try {
            final StateMetadataResult stateMetadataResult = StateMetadataResult.parseFrom(payload);
            final Map<String, ByteString> stateMetadataMap = new HashMap<>();
            stateMetadataResult.getEntriesList()
                    .forEach(entry -> stateMetadataMap.put(entry.getMetakey(), entry.getValue()));

            if (stateMetadataMap.containsKey(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())) {
                return stateMetadataMap.get(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())
                        .toByteArray();
            }
        } catch (final InvalidProtocolBufferException e) {
            LOGGER.severe(String.format("[%-8.8s] unmarshalling error", txId));
            throw new RuntimeException("Error unmarshalling StateMetadataResult.", e);
        }

        return null;
    }

    @Override
    public void putPrivateData(final String collection, final String key, final byte[] value) {
        validateKey(key);
        validateCollection(collection);
        this.handler.invoke(ChaincodeMessageFactory.newPutStateEventMessage(channelId, txId, collection, key,
                ByteString.copyFrom(value)));
    }

    @Override
    public void setPrivateDataValidationParameter(final String collection, final String key, final byte[] value) {
        validateKey(key);
        validateCollection(collection);
        final ChaincodeMessage msg = ChaincodeMessageFactory.newPutStateMetadataEventMessage(channelId, txId,
                collection, key, TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(),
                ByteString.copyFrom(value));
        this.handler.invoke(msg);
    }

    @Override
    public void delPrivateData(final String collection, final String key) {
        validateCollection(collection);
        final ChaincodeMessage msg = ChaincodeMessageFactory.newDeleteStateEventMessage(channelId, txId, collection,
                key);
        this.handler.invoke(msg);
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByRange(final String collection, final String startKey,
            final String endKey) {
        String start = startKey;
        String end = endKey;

        validateCollection(collection);
        if (startKey == null || startKey.isEmpty()) {
            start = UNSPECIFIED_START_KEY;
        }
        if (endKey == null) {
            end = UNSPECIFIED_END_KEY;
        }
        CompositeKey.validateSimpleKeys(start, end);

        return executeGetStateByRange(collection, start, end);
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection,
            final String compositeKey) {

        CompositeKey key;

        if (compositeKey == null) {
            key = new CompositeKey("");
        } else if (compositeKey.startsWith(CompositeKey.NAMESPACE)) {
            key = CompositeKey.parseCompositeKey(compositeKey);
        } else {
            key = new CompositeKey(compositeKey);
        }

        return getPrivateDataByPartialCompositeKey(collection, key);
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection,
            final CompositeKey compositeKey) {
        String cKeyAsString;
        if (compositeKey == null) {
            cKeyAsString = new CompositeKey(UNSPECIFIED_START_KEY).toString();
        } else {
            cKeyAsString = compositeKey.toString();
        }

        return executeGetStateByRange(collection, cKeyAsString, cKeyAsString + MAX_UNICODE_RUNE);
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection,
            final String objectType, final String... attributes) {
        return getPrivateDataByPartialCompositeKey(collection, new CompositeKey(objectType, attributes));
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(final String collection, final String query) {
        validateCollection(collection);
        final ByteString requestPayload = GetQueryResult.newBuilder().setCollection(collection).setQuery(query).build()
                .toByteString();
        final ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_QUERY_RESULT, channelId,
                txId, requestPayload);
        final ByteString response = handler.invoke(requestMessage);

        return new QueryResultsIteratorImpl<KeyValue>(this.handler, channelId, txId, response,
                queryResultBytesToKv.andThen(KeyValueImpl::new));
    }

    @Override
    public Response invokeChaincode(final String chaincodeName, final List<byte[]> args, final String channel) {
        // internally we handle chaincode name as a composite name
        final String compositeName;
        if (channel != null && !channel.trim().isEmpty()) {
            compositeName = chaincodeName + "/" + channel;
        } else {
            compositeName = chaincodeName;
        }

        // create invocation specification of the chaincode to invoke
        final ByteString invocationSpecPayload = ChaincodeSpec.newBuilder()
                .setChaincodeId(ChaincodeID.newBuilder().setName(compositeName).build())
                .setInput(ChaincodeInput.newBuilder()
                        .addAllArgs(args.stream().map(ByteString::copyFrom).collect(Collectors.toList())).build())
                .build().toByteString();

        final ChaincodeMessage invokeChaincodeMessage = ChaincodeMessageFactory
                .newInvokeChaincodeMessage(this.channelId, this.txId, invocationSpecPayload);
        final ByteString response = this.handler.invoke(invokeChaincodeMessage);

        try {
            // response message payload should be yet another chaincode
            // message (the actual response message)
            final ChaincodeMessage responseMessage = ChaincodeMessage.parseFrom(response);
            // the actual response message must be of type COMPLETED

            LOGGER.fine(String.format("[%-8.8s] %s response received from other chaincode.", txId,
                    responseMessage.getType()));

            if (responseMessage.getType() == COMPLETED) {
                // success
                final ProposalResponsePackage.Response r = ProposalResponsePackage.Response
                        .parseFrom(responseMessage.getPayload());
                return new Chaincode.Response(Chaincode.Response.Status.forCode(r.getStatus()), r.getMessage(),
                        r.getPayload() == null ? null : r.getPayload().toByteArray());
            } else {
                // error
                final String message = responseMessage.getPayload().toStringUtf8();
                return new Chaincode.Response(Chaincode.Response.Status.INTERNAL_SERVER_ERROR, message, null);
            }
        } catch (final InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public SignedProposal getSignedProposal() {
        return signedProposal;
    }

    @Override
    public Instant getTxTimestamp() {
        return txTimestamp;
    }

    @Override
    public byte[] getCreator() {
        if (creator == null) {
            return null;
        }
        return creator.toByteArray();
    }

    @Override
    public Map<String, byte[]> getTransient() {
        return transientMap.entrySet().stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().toByteArray()));
    }

    @Override
    public byte[] getBinding() {
        return this.binding;
    }

    private void validateKey(final String key) {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("key cannot not be an empty string");
        }
    }

    private void validateCollection(final String collection) {
        if (collection == null) {
            throw new NullPointerException("collection cannot be null");
        }
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("collection must not be an empty string");
        }
    }

    @Override
    public String getMspId() {
        if (System.getenv().containsKey(CORE_PEER_LOCALMSPID)) {
            return System.getenv(CORE_PEER_LOCALMSPID);
        }
        throw new RuntimeException("CORE_PEER_LOCALMSPID is unset in chaincode process");
    }
}
