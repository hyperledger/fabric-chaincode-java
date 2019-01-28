/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage;
import org.hyperledger.fabric.protos.peer.ProposalPackage;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChaincodeStubNaiveImpl implements ChaincodeStub {
    private List<String> args;
    private List<byte[]> argsAsByte;
    private Map<String, ByteString> state;
    private Chaincode.Response resp;

    ChaincodeStubNaiveImpl() {
        args = new ArrayList<>();
        args.add("func1");
        args.add("param1");
        args.add("param2");

        state = new HashMap<>();
        state.put("a", ByteString.copyFrom("asdf", StandardCharsets.UTF_8));

        argsAsByte = null;

        resp = new Chaincode.Response(404, "Wrong cc name", new byte[]{});
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
    public Chaincode.Response invokeChaincode(String chaincodeName, List<byte[]> args, String channel) {
        return resp;
    }

    @Override
    public byte[] getState(String key) {
        return state.get(key).toByteArray();
    }

    @Override
    public byte[] getStateValidationParameter(String key) {
        return new byte[0];
    }

    @Override
    public void putState(String key, byte[] value) {
        state.put(key, ByteString.copyFrom(value));

    }

    @Override
    public void setStateValidationParameter(String key, byte[] value) {

    }

    @Override
    public void delState(String key) {
        state.remove(key);
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey) {
        return null;
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(String startKey, String endKey, int pageSize, String bookmark) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String objectType, String... attributes) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(CompositeKey compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(CompositeKey compositeKey, int pageSize, String bookmark) {
        return null;
    }

    @Override
    public CompositeKey createCompositeKey(String objectType, String... attributes) {
        return null;
    }

    @Override
    public CompositeKey splitCompositeKey(String compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getQueryResult(String query) {
        return null;
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(String query, int pageSize, String bookmark) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyModification> getHistoryForKey(String key) {
        return null;
    }

    @Override
    public byte[] getPrivateData(String collection, String key) {
        return new byte[0];
    }

    @Override
    public byte[] getPrivateDataValidationParameter(String collection, String key) {
        return new byte[0];
    }

    @Override
    public void putPrivateData(String collection, String key, byte[] value) {

    }

    @Override
    public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {

    }

    @Override
    public void delPrivateData(String collection, String key) {

    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByRange(String collection, String startKey, String endKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, CompositeKey compositeKey) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String objectType, String... attributes) {
        return null;
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query) {
        return null;
    }

    @Override
    public void setEvent(String name, byte[] payload) {

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
        return new byte[0];
    }

    @Override
    public Map<String, byte[]> getTransient() {
        return null;
    }

    @Override
    public byte[] getBinding() {
        return new byte[0];
    }

    void setStringArgs(List<String> args){
        this.args = args;
        this.argsAsByte = args.stream().map(i -> i.getBytes()).collect(Collectors.toList());
    }
}
