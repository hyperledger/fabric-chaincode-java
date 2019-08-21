/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
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

import org.hyperledger.fabric.Logging;
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

class InnvocationStubImpl implements ChaincodeStub {

	private static final String UNSPECIFIED_KEY = new String(Character.toChars(0x000001));
	private static final Logger logger = Logging.getLogger(InnvocationStubImpl.class);

	public static final String MAX_UNICODE_RUNE = "\udbff\udfff";
	private final String channelId;
	private final String txId;
	private final ChaincodeInnvocationTask.InvokeChaincodeSupport handler;
	private final List<ByteString> args;
	private final SignedProposal signedProposal;
	private final Instant txTimestamp;
	private final ByteString creator;
	private final Map<String, ByteString> transientMap;
	private final byte[] binding;
	private ChaincodeEvent event;

	public InnvocationStubImpl(ChaincodeMessage message, ChaincodeInnvocationTask.InvokeChaincodeSupport handler)
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

	private void validateProposalType(ChannelHeader channelHeader) {
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
	public void setEvent(String name, byte[] payload) {
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
	public byte[] getState(String key) {
		return this.handler.invoke(ChaincodeMessageFactory.newGetStateEventMessage(channelId, txId, "", key))
				.toByteArray();
	}

	@Override
	public byte[] getStateValidationParameter(String key) {

		ByteString payload = handler
				.invoke(ChaincodeMessageFactory.newGetStateMetadataEventMessage(channelId, txId, "", key));
		try {
			StateMetadataResult stateMetadataResult = StateMetadataResult.parseFrom(payload);
			Map<String, ByteString> stateMetadataMap = new HashMap<>();
			stateMetadataResult.getEntriesList()
					.forEach(entry -> stateMetadataMap.put(entry.getMetakey(), entry.getValue()));

			if (stateMetadataMap.containsKey(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())) {
				return stateMetadataMap.get(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())
						.toByteArray();
			}
		} catch (InvalidProtocolBufferException e) {
			logger.severe(String.format("[%-8.8s] unmarshall error", txId));
			throw new RuntimeException("Error unmarshalling StateMetadataResult.", e);
		}

		return null;

	}

	@Override
	public void putState(String key, byte[] value) {
		validateKey(key);
		this.handler.invoke(
				ChaincodeMessageFactory.newPutStateEventMessage(channelId, txId, "", key, ByteString.copyFrom(value)));
	}

	@Override
	public void setStateValidationParameter(String key, byte[] value) {
		validateKey(key);
		ChaincodeMessage msg = ChaincodeMessageFactory.newPutStateMatadateEventMessage(channelId, txId, "", key,
				TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(), ByteString.copyFrom(value));
		this.handler.invoke(msg);
	}

	@Override
	public void delState(String key) {
		ChaincodeMessage msg = ChaincodeMessageFactory.newDeleteStateEventMessage(channelId, txId, "", key);
		this.handler.invoke(msg);
	}

	@Override
	public QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey) {
		if (startKey == null || startKey.isEmpty()) {
			startKey = UNSPECIFIED_KEY;
		}
		if (endKey == null || endKey.isEmpty()) {
			endKey = UNSPECIFIED_KEY;
		}
		CompositeKey.validateSimpleKeys(startKey, endKey);

		return executeGetStateByRange("", startKey, endKey);
	}

	private QueryResultsIterator<KeyValue> executeGetStateByRange(String collection, String startKey, String endKey) {

		ByteString requestPayload = GetStateByRange.newBuilder().setCollection(collection).setStartKey(startKey)
				.setEndKey(endKey).build().toByteString();

		ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_STATE_BY_RANGE, channelId, txId,
				requestPayload);
		ByteString response = handler.invoke(requestMessage);

		return new QueryResultsIteratorImpl<KeyValue>(this.handler, channelId, txId, response,
				queryResultBytesToKv.andThen(KeyValueImpl::new));

	}

	private Function<QueryResultBytes, KV> queryResultBytesToKv = new Function<QueryResultBytes, KV>() {
		public KV apply(QueryResultBytes queryResultBytes) {
			try {
				return KV.parseFrom(queryResultBytes.getResultBytes());
			} catch (InvalidProtocolBufferException e) {
				throw new RuntimeException(e);
			}
		}

	};

	@Override
	public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(String startKey, String endKey,
			int pageSize, String bookmark) {
		if (startKey == null || startKey.isEmpty()) {
			startKey = UNSPECIFIED_KEY;
		}
		if (endKey == null || endKey.isEmpty()) {
			endKey = UNSPECIFIED_KEY;
		}

		CompositeKey.validateSimpleKeys(startKey, endKey);

		ChaincodeShim.QueryMetadata queryMetadata = ChaincodeShim.QueryMetadata.newBuilder().setBookmark(bookmark)
				.setPageSize(pageSize).build();

		return executeGetStateByRangeWithMetadata("", startKey, endKey, queryMetadata.toByteString());
	}

	private QueryResultsIteratorWithMetadataImpl<KeyValue> executeGetStateByRangeWithMetadata(String collection,
			String startKey, String endKey, ByteString metadata) {

		ByteString payload = GetStateByRange.newBuilder().setCollection(collection).setStartKey(startKey)
				.setEndKey(endKey).setMetadata(metadata).build().toByteString();

		ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_STATE_BY_RANGE, startKey, endKey,
				payload);

		ByteString response = this.handler.invoke(requestMessage);

		return new QueryResultsIteratorWithMetadataImpl<>(this.handler, getChannelId(), getTxId(), response,
				queryResultBytesToKv.andThen(KeyValueImpl::new));

	}

	@Override
	public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey) {

		CompositeKey key;

		if (compositeKey.startsWith(CompositeKey.NAMESPACE)) {
			key = CompositeKey.parseCompositeKey(compositeKey);
		} else {
			key = new CompositeKey(compositeKey);
		}

		return getStateByPartialCompositeKey(key);
	}

	@Override
	public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String objectType, String... attributes) {
		return getStateByPartialCompositeKey(new CompositeKey(objectType, attributes));
	}

	@Override
	public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(CompositeKey compositeKey) {
		if (compositeKey == null) {
			compositeKey = new CompositeKey(UNSPECIFIED_KEY);
		}

		String cKeyAsString = compositeKey.toString();

		return executeGetStateByRange("", cKeyAsString, cKeyAsString + MAX_UNICODE_RUNE);
	}

	@Override
	public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(
			CompositeKey compositeKey, int pageSize, String bookmark) {
		if (compositeKey == null) {
			compositeKey = new CompositeKey(UNSPECIFIED_KEY);
		}

		String cKeyAsString = compositeKey.toString();

		ChaincodeShim.QueryMetadata queryMetadata = ChaincodeShim.QueryMetadata.newBuilder().setBookmark(bookmark)
				.setPageSize(pageSize).build();

		return executeGetStateByRangeWithMetadata("", cKeyAsString, cKeyAsString + MAX_UNICODE_RUNE,
				queryMetadata.toByteString());
	}

	@Override
	public CompositeKey createCompositeKey(String objectType, String... attributes) {
		return new CompositeKey(objectType, attributes);
	}

	@Override
	public CompositeKey splitCompositeKey(String compositeKey) {
		return CompositeKey.parseCompositeKey(compositeKey);
	}

	@Override
	public QueryResultsIterator<KeyValue> getQueryResult(String query) {

		ByteString requestPayload = GetQueryResult.newBuilder().setCollection("").setQuery(query).build()
				.toByteString();
		ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_QUERY_RESULT, channelId, txId,
				requestPayload);
		ByteString response = handler.invoke(requestMessage);

		return new QueryResultsIteratorImpl<KeyValue>(this.handler, channelId, txId, response,
				queryResultBytesToKv.andThen(KeyValueImpl::new));
	}

	@Override
	public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(String query, int pageSize,
			String bookmark) {

		ByteString queryMetadataPayload = ChaincodeShim.QueryMetadata.newBuilder().setBookmark(bookmark)
				.setPageSize(pageSize).build().toByteString();
		ByteString requestPayload = GetQueryResult.newBuilder().setCollection("").setQuery(query)
				.setMetadata(queryMetadataPayload).build().toByteString();
		ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_QUERY_RESULT, channelId, txId,
				requestPayload);
		ByteString response = handler.invoke(requestMessage);

		return new QueryResultsIteratorWithMetadataImpl<KeyValue>(this.handler, channelId, txId, response,
				queryResultBytesToKv.andThen(KeyValueImpl::new));

	}

	@Override
	public QueryResultsIterator<KeyModification> getHistoryForKey(String key) {

		ByteString requestPayload = GetQueryResult.newBuilder().setCollection("").setQuery(key).build().toByteString();
		ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_HISTORY_FOR_KEY, channelId, txId,
				requestPayload);
		ByteString response = handler.invoke(requestMessage);

		return new QueryResultsIteratorImpl<KeyModification>(this.handler, channelId, txId, response,
				queryResultBytesToKeyModification.andThen(KeyModificationImpl::new));

	}

	private Function<QueryResultBytes, KvQueryResult.KeyModification> queryResultBytesToKeyModification = new Function<QueryResultBytes, KvQueryResult.KeyModification>() {
		public KvQueryResult.KeyModification apply(QueryResultBytes queryResultBytes) {
			try {
				return KvQueryResult.KeyModification.parseFrom(queryResultBytes.getResultBytes());
			} catch (InvalidProtocolBufferException e) {
				throw new RuntimeException(e);
			}
		}
	};

	@Override
	public byte[] getPrivateData(String collection, String key) {
		validateCollection(collection);
		return this.handler.invoke(ChaincodeMessageFactory.newGetStateEventMessage(channelId, txId, collection, key))
				.toByteArray();
	}

	@Override
	public byte[] getPrivateDataHash(String collection, String key) {

		validateCollection(collection);

		ByteString requestPayload = GetState.newBuilder().setCollection(collection).setKey(key).build().toByteString();
		ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_PRIVATE_DATA_HASH, channelId,
				txId, requestPayload);

		return handler.invoke(requestMessage).toByteArray();
	}

	@Override
	public byte[] getPrivateDataValidationParameter(String collection, String key) {
		validateCollection(collection);

		ByteString payload = handler
				.invoke(ChaincodeMessageFactory.newGetStateMetadataEventMessage(channelId, txId, collection, key));
		try {
			StateMetadataResult stateMetadataResult = StateMetadataResult.parseFrom(payload);
			Map<String, ByteString> stateMetadataMap = new HashMap<>();
			stateMetadataResult.getEntriesList()
					.forEach(entry -> stateMetadataMap.put(entry.getMetakey(), entry.getValue()));

			if (stateMetadataMap.containsKey(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())) {
				return stateMetadataMap.get(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString())
						.toByteArray();
			}
		} catch (InvalidProtocolBufferException e) {
			logger.severe(String.format("[%-8.8s] unmarshall error", txId));
			throw new RuntimeException("Error unmarshalling StateMetadataResult.", e);
		}

		return null;
	}

	@Override
	public void putPrivateData(String collection, String key, byte[] value) {
		validateKey(key);
		validateCollection(collection);
		this.handler.invoke(
				ChaincodeMessageFactory.newPutStateEventMessage(channelId, txId, "", key, ByteString.copyFrom(value)));
	}

	@Override
	public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {
		validateKey(key);
		validateCollection(collection);
		ChaincodeMessage msg = ChaincodeMessageFactory.newPutStateMatadateEventMessage(channelId, txId, collection, key,
				TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(), ByteString.copyFrom(value));
		this.handler.invoke(msg);
	}

	@Override
	public void delPrivateData(String collection, String key) {
		validateCollection(collection);
		ChaincodeMessage msg = ChaincodeMessageFactory.newDeleteStateEventMessage(channelId, txId, collection, key);
		this.handler.invoke(msg);
	}

	@Override
	public QueryResultsIterator<KeyValue> getPrivateDataByRange(String collection, String startKey, String endKey) {
		validateCollection(collection);
		if (startKey == null || startKey.isEmpty()) {
			startKey = UNSPECIFIED_KEY;
		}
		if (endKey == null || endKey.isEmpty()) {
			endKey = UNSPECIFIED_KEY;
		}
		CompositeKey.validateSimpleKeys(startKey, endKey);

		return executeGetStateByRange(collection, startKey, endKey);
	}

	@Override
	public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String compositeKey) {

		CompositeKey key;

		if (compositeKey == null) {
			compositeKey = "";
		}

		if (compositeKey.startsWith(CompositeKey.NAMESPACE)) {
			key = CompositeKey.parseCompositeKey(compositeKey);
		} else {
			key = new CompositeKey(compositeKey);
		}

		return getPrivateDataByPartialCompositeKey(collection, key);
	}

	@Override
	public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection,
			CompositeKey compositeKey) {

		if (compositeKey == null) {
			compositeKey = new CompositeKey(UNSPECIFIED_KEY);
		}

		String cKeyAsString = compositeKey.toString();

		return executeGetStateByRange(collection, cKeyAsString, cKeyAsString + MAX_UNICODE_RUNE);
	}

	@Override
	public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String objectType,
			String... attributes) {
		return getPrivateDataByPartialCompositeKey(collection, new CompositeKey(objectType, attributes));
	}

	@Override
	public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query) {
		validateCollection(collection);
		ByteString requestPayload = GetQueryResult.newBuilder().setCollection(collection).setQuery(query).build()
				.toByteString();
		ChaincodeMessage requestMessage = ChaincodeMessageFactory.newEventMessage(GET_QUERY_RESULT, channelId, txId,
				requestPayload);
		ByteString response = handler.invoke(requestMessage);

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

		ChaincodeMessage invokeChaincodeMessage = ChaincodeMessageFactory.newInvokeChaincodeMessage(this.channelId,
				this.txId, invocationSpecPayload);
		ByteString response = this.handler.invoke(invokeChaincodeMessage);

		try {
			// response message payload should be yet another chaincode
			// message (the actual response message)
			final ChaincodeMessage responseMessage = ChaincodeMessage.parseFrom(response);
			// the actual response message must be of type COMPLETED

			logger.fine(String.format("[%-8.8s] %s response received from other chaincode.", txId,
					responseMessage.getType()));

			if (responseMessage.getType() == COMPLETED) {
				// success
				ProposalResponsePackage.Response r = ProposalResponsePackage.Response
						.parseFrom(responseMessage.getPayload());
				return new Chaincode.Response(Chaincode.Response.Status.forCode(r.getStatus()), r.getMessage(),
						r.getPayload() == null ? null : r.getPayload().toByteArray());
			} else {
				// error
				String message = responseMessage.getPayload().toStringUtf8();
				return new Chaincode.Response(Chaincode.Response.Status.INTERNAL_SERVER_ERROR, message, null);
			}
		} catch (InvalidProtocolBufferException e) {
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
		if (creator == null)
			return null;
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

	private void validateKey(String key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (key.length() == 0) {
			throw new IllegalArgumentException("key cannot not be an empty string");
		}
	}

	private void validateCollection(String collection) {
		if (collection == null) {
			throw new NullPointerException("collection cannot be null");
		}
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("collection must not be an empty string");
		}
	}
}
