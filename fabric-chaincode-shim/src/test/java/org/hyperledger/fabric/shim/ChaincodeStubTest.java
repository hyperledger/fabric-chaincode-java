/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
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
		public Response invokeChaincode(String chaincodeName, List<byte[]> args, String channel) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] getState(String key) {
			return key.getBytes();
		}

		@Override
		public byte[] getStateValidationParameter(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putState(String key, byte[] value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStateValidationParameter(String key, byte[] value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delState(String key) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(String startKey, String endKey,
				int pageSize, String bookmark) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String objectType, String... attributes) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(CompositeKey compositeKey) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(
				CompositeKey compositeKey, int pageSize, String bookmark) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CompositeKey createCompositeKey(String objectType, String... attributes) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CompositeKey splitCompositeKey(String compositeKey) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getQueryResult(String query) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(String query, int pageSize,
				String bookmark) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyModification> getHistoryForKey(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] getPrivateData(String collection, String key) {
			return collection.getBytes();
		}

		@Override
		public byte[] getPrivateDataHash(String collection, String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] getPrivateDataValidationParameter(String collection, String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putPrivateData(String collection, String key, byte[] value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delPrivateData(String collection, String key) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public QueryResultsIterator<KeyValue> getPrivateDataByRange(String collection, String startKey, String endKey) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection,
				String compositeKey) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection,
				CompositeKey compositeKey) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String objectType,
				String... attributes) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setEvent(String name, byte[] payload) {
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
		
	}
	
	
	@Test
	public void testDefaultMethods() {
		ChaincodeStub stub = new FakeStub();
		String chaincodeName = "ACME_SHIPPING";
			
		stub.invokeChaincode(chaincodeName, new ArrayList<byte[]>());		
		stub.invokeChaincodeWithStringArgs(chaincodeName, new ArrayList<String>(),"channel");
		stub.invokeChaincodeWithStringArgs(chaincodeName, new ArrayList<String>());
		stub.invokeChaincodeWithStringArgs(chaincodeName, "anvil","tnt");
		
		stub.getStringState("key");
		stub.putPrivateData("collection", "key", "value");
		stub.getPrivateDataUTF8("collection", "key");
		stub.putStringState("key", "value");
	}
	
}
