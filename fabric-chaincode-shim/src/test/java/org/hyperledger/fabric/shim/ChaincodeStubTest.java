/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.ProposalPackage.SignedProposal;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;
import org.junit.jupiter.api.Test;

public class ChaincodeStubTest {

    class FakeStub implements ChaincodeStub {

        @Override
        public List<byte[]> getArgs() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<String> getStringArgs() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getFunction() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<String> getParameters() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getTxId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getChannelId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Response invokeChaincode(final String chaincodeName, final List<byte[]> args, final String channel) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getState(final String key) {
            return key.getBytes();
        }

        @Override
        public byte[] getStateValidationParameter(final String key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void putState(final String key, final byte[] value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setStateValidationParameter(final String key, final byte[] value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void delState(final String key) {
            // TODO Auto-generated method stub

        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByRange(final String startKey, final String endKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(final String startKey, final String endKey, final int pageSize,
                final String bookmark) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final String compositeKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final String objectType, final String... attributes) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(final CompositeKey compositeKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(final CompositeKey compositeKey, final int pageSize,
                final String bookmark) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public CompositeKey createCompositeKey(final String objectType, final String... attributes) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public CompositeKey splitCompositeKey(final String compositeKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getQueryResult(final String query) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(final String query, final int pageSize, final String bookmark) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyModification> getHistoryForKey(final String key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getPrivateData(final String collection, final String key) {
            return collection.getBytes();
        }

        @Override
        public byte[] getPrivateDataHash(final String collection, final String key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getPrivateDataValidationParameter(final String collection, final String key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void putPrivateData(final String collection, final String key, final byte[] value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setPrivateDataValidationParameter(final String collection, final String key, final byte[] value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void delPrivateData(final String collection, final String key) {
            // TODO Auto-generated method stub

        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByRange(final String collection, final String startKey, final String endKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection, final String compositeKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection, final CompositeKey compositeKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(final String collection, final String objectType,
                final String... attributes) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(final String collection, final String query) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setEvent(final String name, final byte[] payload) {
            // TODO Auto-generated method stub

        }

        @Override
        public ChaincodeEvent getEvent() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SignedProposal getSignedProposal() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Instant getTxTimestamp() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getCreator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, byte[]> getTransient() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getBinding() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getMspId() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Test
    public void testDefaultMethods() {
        final ChaincodeStub stub = new FakeStub();
        final String chaincodeName = "ACME_SHIPPING";

        stub.invokeChaincode(chaincodeName, new ArrayList<byte[]>());
        stub.invokeChaincodeWithStringArgs(chaincodeName, new ArrayList<String>(), "channel");
        stub.invokeChaincodeWithStringArgs(chaincodeName, new ArrayList<String>());
        stub.invokeChaincodeWithStringArgs(chaincodeName, "anvil", "tnt");

        stub.getStringState("key");
        stub.putPrivateData("collection", "key", "value");
        stub.getPrivateDataUTF8("collection", "key");
        stub.putStringState("key", "value");
    }

}
