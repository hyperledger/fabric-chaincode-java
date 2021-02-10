/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hyperledger.fabric.TestUtil;
import org.hyperledger.fabric.protos.msp.Identities.SerializedIdentity;
import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage;
import org.hyperledger.fabric.protos.peer.ProposalPackage;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import com.google.protobuf.ByteString;

public final class ChaincodeStubNaiveImpl implements ChaincodeStub {
    private List<String> args;
    private List<byte[]> argsAsByte;
    private final Map<String, ByteString> state;
    private final Chaincode.Response resp;
    private String certificate = TestUtil.CERT_WITHOUT_ATTRS;

    public ChaincodeStubNaiveImpl() {
        args = new ArrayList<>();
        args.add("func1");
        args.add("param1");
        args.add("param2");

        state = new HashMap<>();
        state.put("a", ByteString.copyFrom("asdf", StandardCharsets.UTF_8));

        argsAsByte = null;

        resp = new Chaincode.Response(404, "Wrong cc name", new byte[] {});
    }

    ChaincodeStubNaiveImpl(final List<String> args) {
        this.args = args;
        state = new HashMap<>();
        state.put("a", ByteString.copyFrom("asdf", StandardCharsets.UTF_8));

        argsAsByte = null;

        resp = new Chaincode.Response(404, "Wrong cc name", new byte[] {});
    }

    @Override
    public List<byte[]> getArgs() {
        if (argsAsByte == null) {
            argsAsByte = args.stream().map(i -> i.getBytes()).collect(Collectors.toList());
        }
        return argsAsByte;
    }

    @Override
    public List<String> getStringArgs() {
        return args;
    }

    @Override
    public String getFunction() {
        return args.get(0);
    }

    @Override
    public List<String> getParameters() {
        return args.subList(1, args.size());
    }

    @Override
    public String getTxId() {
        return "tx0";
    }

    @Override
    public String getChannelId() {
        return "ch0";
    }

    @Override
    public Chaincode.Response invokeChaincode(final String chaincodeName, final List<byte[]> args, final String channel) {
        return resp;
    }

    @Override
    public byte[] getState(final String key) {
        return state.get(key).toByteArray();
    }

    @Override
    public byte[] getStateValidationParameter(final String key) {
        return new byte[0];
    }

    @Override
    public void putState(final String key, final byte[] value) {
        state.put(key, ByteString.copyFrom(value));

    }

    @Override
    public void setStateValidationParameter(final String key, final byte[] value) {

    }

    @Override
    public void delState(final String key) {
        state.remove(key);
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByRange(final String startKey, final String endKey) {
        return null;
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(final String startKey, final String endKey, final int pageSize,
            final String bookmark) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final String compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final String objectType, final String... attributes) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final CompositeKey compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(final CompositeKey compositeKey, final int pageSize,
            final String bookmark) {
        return null;
    }

    @Override
    public CompositeKey createCompositeKey(final String objectType, final String... attributes) {
        return null;
    }

    @Override
    public CompositeKey splitCompositeKey(final String compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getQueryResult(final String query) {
        return null;
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(final String query, final int pageSize, final String bookmark) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyModification> getHistoryForKey(final String key) {
        return null;
    }

    @Override
    public byte[] getPrivateData(final String collection, final String key) {
        return new byte[0];
    }

    @Override
    public byte[] getPrivateDataHash(final String collection, final String key) {
        return new byte[0];
    }

    @Override
    public byte[] getPrivateDataValidationParameter(final String collection, final String key) {
        return new byte[0];
    }

    @Override
    public void putPrivateData(final String collection, final String key, final byte[] value) {

    }

    @Override
    public void setPrivateDataValidationParameter(final String collection, final String key, final byte[] value) {

    }

    @Override
    public void delPrivateData(final String collection, final String key) {

    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByRange(final String collection, final String startKey, final String endKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection, final String compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection, final CompositeKey compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection, final String objectType, final String... attributes) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(final String collection, final String query) {
        return null;
    }

    @Override
    public void setEvent(final String name, final byte[] payload) {

    }

    @Override
    public ChaincodeEventPackage.ChaincodeEvent getEvent() {
        return null;
    }

    @Override
    public ProposalPackage.SignedProposal getSignedProposal() {
        return null;
    }

    @Override
    public Instant getTxTimestamp() {
        return null;
    }

    @Override
    public byte[] getCreator() {
        return buildSerializedIdentity();
    }

    @Override
    public Map<String, byte[]> getTransient() {
        return null;
    }

    @Override
    public byte[] getBinding() {
        return new byte[0];
    }

    void setStringArgs(final List<String> args) {
        this.args = args;
        this.argsAsByte = args.stream().map(i -> i.getBytes()).collect(Collectors.toList());
    }

    public byte[] buildSerializedIdentity() {
        final SerializedIdentity.Builder identity = SerializedIdentity.newBuilder();
        identity.setMspid("testMSPID");
        final byte[] decodedCert = Base64.getDecoder().decode(this.certificate);
        identity.setIdBytes(ByteString.copyFrom(decodedCert));
        final SerializedIdentity builtIdentity = identity.build();
        return builtIdentity.toByteArray();
    }

    // Used by tests to control which serialized identity is returned by
    // buildSerializedIdentity
    public void setCertificate(final String certificateToTest) {
        this.certificate = certificateToTest;
    }

    @Override
    public String getMspId() {
        return "fakemspid";
    }
}
