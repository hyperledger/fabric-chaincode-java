/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hyperledger.fabric.protos.common.Common.HeaderType.ENDORSER_TRANSACTION_VALUE;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.hamcrest.Matchers;
import org.hyperledger.fabric.protos.common.Common.ChannelHeader;
import org.hyperledger.fabric.protos.common.Common.Header;
import org.hyperledger.fabric.protos.common.Common.SignatureHeader;
import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult;
import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult.KV;
import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResponse;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResultBytes;
import org.hyperledger.fabric.protos.peer.ProposalPackage.ChaincodeProposalPayload;
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal;
import org.hyperledger.fabric.protos.peer.ProposalPackage.SignedProposal;
import org.hyperledger.fabric.protos.peer.TransactionPackage;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

public class ChaincodeStubImplTest {

    private static final String TEST_COLLECTION = "testcoll";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private Handler handler;

    @Test
    public void testGetArgs() {
        List<ByteString> args = Arrays.asList(
                ByteString.copyFromUtf8("arg0"),
                ByteString.copyFromUtf8("arg1"),
                ByteString.copyFromUtf8("arg2"));
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, args, null);
        assertThat(stub.getArgs(), contains(args.stream().map(ByteString::toByteArray).toArray()));
    }

    @Test
    public void testGetStringArgs() {
        List<ByteString> args = Arrays.asList(
                ByteString.copyFromUtf8("arg0"),
                ByteString.copyFromUtf8("arg1"),
                ByteString.copyFromUtf8("arg2"));
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, args, null);
        assertThat(stub.getStringArgs(), contains(args.stream().map(ByteString::toStringUtf8).toArray()));
    }

    @Test
    public void testGetFunction() {
        List<ByteString> args = Arrays.asList(
                ByteString.copyFromUtf8("function"),
                ByteString.copyFromUtf8("arg0"),
                ByteString.copyFromUtf8("arg1"));
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, args, null);
        assertThat(stub.getFunction(), is("function"));
    }

    @Test
    public void testGetParameters() {
        List<ByteString> args = Arrays.asList(
                ByteString.copyFromUtf8("function"),
                ByteString.copyFromUtf8("arg0"),
                ByteString.copyFromUtf8("arg1"));
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, args, null);
        assertThat(stub.getParameters(), contains("arg0", "arg1"));
    }

    @Test
    public void testSetGetEvent() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] payload = new byte[]{0x10, 0x20, 0x20};
        final String eventName = "event_name";
        stub.setEvent(eventName, payload);
        ChaincodeEvent event = stub.getEvent();
        assertThat(event, hasProperty("eventName", equalTo(eventName)));
        assertThat(event, hasProperty("payload", equalTo(ByteString.copyFrom(payload))));

        stub.setEvent(eventName, null);
        event = stub.getEvent();
        assertNotNull(event);
        assertThat(event, hasProperty("eventName", equalTo(eventName)));
        assertThat(event, hasProperty("payload", equalTo(ByteString.copyFrom(new byte[0]))));
    }

    @Test
    public void testSetEventEmptyName() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        thrown.expect(Matchers.isA(IllegalArgumentException.class));
        stub.setEvent("", new byte[0]);
    }

    @Test
    public void testSetEventNullName() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        thrown.expect(Matchers.isA(IllegalArgumentException.class));
        stub.setEvent(null, new byte[0]);
    }

    @Test
    public void testGetTxId() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        assertThat(stub.getTxId(), is("txId"));
    }

    @Test
    public void testGetState() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        when(handler.getState("myc", "txId", "", "key")).thenReturn(ByteString.copyFrom(value));
        assertThat(stub.getState("key"), is(value));
    }

    @Test
    public void testGetStateValidationParameter() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        Map<String, ByteString> metaMap = new HashMap<>();
        metaMap.put(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(), ByteString.copyFrom(value));
        when(handler.getStateMetadata("myc", "txId", "", "key")).thenReturn(metaMap);
        assertThat(stub.getStateValidationParameter("key"), is(value));

        when(handler.getStateMetadata("myc", "txId", "", "key2")).thenReturn(new HashMap<>());
        assertThat(stub.getStateValidationParameter("key2"), is(nullValue()));

    }

    @Test
    public void testGetStringState() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final String value = "TEST";
        when(handler.getState("myc", "txId", "", "key")).thenReturn(ByteString.copyFromUtf8(value));
        assertThat(stub.getStringState("key"), is(value));
    }

    @Test
    public void testPutState() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        stub.putState("key", value);
        verify(handler).putState("myc", "txId", "", "key", ByteString.copyFrom(value));
        try {
            stub.putState(null, value);
            Assert.fail("Null key check fails");
        } catch (NullPointerException e) {
        }

        try {
            stub.putState("", value);
            Assert.fail("Empty key check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetStateValidationParameter() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        stub.setStateValidationParameter("key", value);
        verify(handler).putStateMetadata("myc", "txId", "", "key", TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(), ByteString.copyFrom(value));
        try {
            stub.setStateValidationParameter(null, value);
            Assert.fail("Null key check fails");
        } catch (NullPointerException e) {
        }

        try {
            stub.setStateValidationParameter("", value);
            Assert.fail("Empty key check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testPutStringState() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final String value = "TEST";
        stub.putStringState("key", value);
        verify(handler).putState("myc", "txId", "", "key", ByteString.copyFromUtf8(value));
    }

    @Test
    public void testDelState() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        stub.delState("key");
        verify(handler).deleteState("myc", "txId", "", "key");
    }

    @Test
    public void testGetStateByRange() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final String startKey = "START";
        final String endKey = "END";
        KV[] keyValues = prepareKeyValuePairs(2);
        final QueryResponse value = prepareQueryResponseForRange(startKey, endKey, keyValues, false);
        when(handler.getStateByRange("myc", "txId", "", startKey, endKey, null)).thenReturn(value);
        QueryResultsIterator<KeyValue> queryResultsIterator = stub.getStateByRange(startKey, endKey);
        assertThat(queryResultsIterator, contains(Arrays.stream(keyValues).map(KeyValueImpl::new).toArray()));
    }

    @Test
    public void testGetStateByRangeWithPagination() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final String startKey = "START";
        final String endKey = "END";
        KV[] keyValues = prepareKeyValuePairs(2);
        final QueryResponse value = prepareQueryResponseForRange(startKey, endKey, keyValues, true);

        ChaincodeShim.QueryMetadata queryMetadata = ChaincodeShim.QueryMetadata.newBuilder()
                .setBookmark("aaaa")
                .setPageSize(1)
                .build();

        when(handler.getStateByRange("myc", "txId", "", startKey, endKey, queryMetadata.toByteString())).thenReturn(value);
        QueryResultsIteratorWithMetadata<KeyValue> queryResultsIterator = stub.getStateByRangeWithPagination(startKey, endKey, 1, "aaaa");
        assertThat(queryResultsIterator, contains(Arrays.stream(keyValues).map(KeyValueImpl::new).toArray()));
        assertThat(queryResultsIterator.getMetadata().getFetchedRecordsCount(), is(2));
        assertThat(queryResultsIterator.getMetadata().getBookmark(), is("bbbb"));
    }

    private KV[] prepareKeyValuePairs(int count) {
        final KV[] keyValue = new KV[count];
        for(int i = 0; i < count; i++) {
            keyValue[i] = KV.newBuilder()
                    .setKey("Key" + i)
                    .setValue(ByteString.copyFromUtf8("Value of Key" + i))
                    .build();
        }
        return keyValue;
    }

    private QueryResponse prepareQueryResponseForRange(String startKey, String endKey, KV[] keyValues, boolean createMetadata) {
        QueryResponse.Builder builder = QueryResponse.newBuilder()
                .setHasMore(false);
        Arrays.stream(keyValues).forEach(kv -> builder.addResults(QueryResultBytes.newBuilder().setResultBytes(kv.toByteString())));
        if (createMetadata) {
            ChaincodeShim.QueryResponseMetadata qrm = ChaincodeShim.QueryResponseMetadata.newBuilder()
                    .setBookmark("bbbb")
                    .setFetchedRecordsCount(2)
                    .build();
            builder.setMetadata(qrm.toByteString());
        }
        return builder.build();
    }

    @Test
    public void testGetStateByPartialCompositeKey() {

        ChaincodeStubImpl stub = prepareStubAndMockHandler();

        stub.getStateByPartialCompositeKey("KEY");
        String key = new CompositeKey("KEY").toString();
        verify(handler).getStateByRange("myc", "txId", "", key, key + "\udbff\udfff", null);

        stub.getStateByPartialCompositeKey("");
        key = new CompositeKey("").toString();
        verify(handler).getStateByRange("myc", "txId", "", key, key + "\udbff\udfff", null);
    }

    @Test
    public void testGetStateByPartialCompositeKey_withAttributesAsString() {

        ChaincodeStubImpl stub = prepareStubAndMockHandler();
        CompositeKey cKey = new CompositeKey("KEY", "attr1", "attr2");
        stub.getStateByPartialCompositeKey(cKey.toString());
        verify(handler).getStateByRange("myc", "txId", "", cKey.toString(), cKey.toString() + "\udbff\udfff", null);

    }

    @Test
    public void testGetStateByPartialCompositeKey_withAttributesWithSplittedParams() {

        ChaincodeStubImpl stub = prepareStubAndMockHandler();
        CompositeKey cKey = new CompositeKey("KEY", "attr1", "attr2", "attr3");
        stub.getStateByPartialCompositeKey("KEY", "attr1", "attr2", "attr3");
        verify(handler).getStateByRange("myc", "txId", "", cKey.toString(), cKey.toString() + "\udbff\udfff", null);

    }

    @Test
    public void testGetStateByPartialCompositeKey_withCompositeKey() {

        ChaincodeStubImpl stub = prepareStubAndMockHandler();

        CompositeKey key = new CompositeKey("KEY");
        stub.getStateByPartialCompositeKey(key);
        verify(handler).getStateByRange("myc", "txId", "", key.toString(), key.toString() + "\udbff\udfff", null);

        key = new CompositeKey("");
        stub.getStateByPartialCompositeKey(key);
        verify(handler).getStateByRange("myc", "txId", "", key.toString(), key.toString() + "\udbff\udfff", null);
    }

    @Test
    public void testGetStateByPartialCompositeKeyWithPagination() {
        ChaincodeShim.QueryMetadata queryMetadata = ChaincodeShim.QueryMetadata.newBuilder()
                .setBookmark("aaaa")
                .setPageSize(1)
                .build();

        ChaincodeStubImpl stub = prepareStubAndMockHandler(true, queryMetadata.toByteString());

        CompositeKey key = new CompositeKey("KEY");
        QueryResultsIteratorWithMetadata<KeyValue> queryResultsIterator = stub.getStateByPartialCompositeKeyWithPagination(key, 1, "aaaa");
        verify(handler).getStateByRange("myc", "txId", "", key.toString(), key.toString() + "\udbff\udfff", queryMetadata.toByteString());
        assertThat(queryResultsIterator.getMetadata().getFetchedRecordsCount(), is(2));
        assertThat(queryResultsIterator.getMetadata().getBookmark(), is("bbbb"));


        key = new CompositeKey("");
        queryResultsIterator = stub.getStateByPartialCompositeKeyWithPagination(key,1, "aaaa");
        verify(handler).getStateByRange("myc", "txId", "", key.toString(), key.toString() + "\udbff\udfff", queryMetadata.toByteString());
        assertThat(queryResultsIterator.getMetadata().getFetchedRecordsCount(), is(2));
        assertThat(queryResultsIterator.getMetadata().getBookmark(), is("bbbb"));
    }

    private ChaincodeStubImpl prepareStubAndMockHandler() {
        return prepareStubAndMockHandler(false, null);
    }

    private ChaincodeStubImpl prepareStubAndMockHandler(boolean createMetadata, ByteString metadata) {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final KV[] keyValues = prepareKeyValuePairs(2);

        QueryResponse.Builder builder = QueryResponse.newBuilder()
                .setHasMore(false);
        Arrays.stream(keyValues).forEach(kv -> builder.addResults(QueryResultBytes.newBuilder().setResultBytes(kv.toByteString())));
        if (createMetadata) {
            ChaincodeShim.QueryResponseMetadata qrm = ChaincodeShim.QueryResponseMetadata.newBuilder()
                    .setBookmark("bbbb")
                    .setFetchedRecordsCount(2)
                    .build();
            builder.setMetadata(qrm.toByteString());
        }
        final QueryResponse value = builder.build();
        when(handler.getStateByRange(anyString(), anyString(), anyString(), anyString(), anyString(), eq(metadata))).thenReturn(value);

        return stub;
    }

    @Test
    public void testCreateCompositeKey() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final CompositeKey key = stub.createCompositeKey("abc", "def", "ghi", "jkl", "mno");
        assertThat(key, hasProperty("objectType", equalTo("abc")));
        assertThat(key, hasProperty("attributes", hasSize(4)));
        assertThat(key, Matchers.hasToString(equalTo("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000")));
    }

    @Test
    public void testSplitCompositeKey() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final CompositeKey key = stub.splitCompositeKey("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000");
        assertThat(key, hasProperty("objectType", equalTo("abc")));
        assertThat(key, hasProperty("attributes", contains("def", "ghi", "jkl", "mno")));
        assertThat(key, Matchers.hasToString(equalTo("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000")));
    }

    @Test
    public void testGetQueryResult() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final KV[] keyValues = new KV[]{
                KV.newBuilder()
                        .setKey("A")
                        .setValue(ByteString.copyFromUtf8("Value of A"))
                        .build(),
                KV.newBuilder()
                        .setKey("B")
                        .setValue(ByteString.copyFromUtf8("Value of B"))
                        .build()
        };
        final QueryResponse value = QueryResponse.newBuilder()
                .setHasMore(false)
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyValues[0].toByteString()))
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyValues[1].toByteString()))
                .build();
        when(handler.getQueryResult("myc", "txId", "", "QUERY", null)).thenReturn(value);
        assertThat(stub.getQueryResult("QUERY"), contains(Arrays.stream(keyValues).map(KeyValueImpl::new).toArray()));
    }

    @Test(expected = InvalidProtocolBufferException.class)
    public void testGetQueryResultWithException() throws Throwable {
        final String txId = "txId", query = "QUERY", channelId = "myc";
        final ChaincodeStubImpl stub = new ChaincodeStubImpl(channelId, txId, handler, Collections.emptyList(), null);
        final QueryResponse value = QueryResponse.newBuilder()
                .setHasMore(false)
                .addResults(QueryResultBytes.newBuilder().setResultBytes(ByteString.copyFromUtf8("exception")))
                .build();
        when(handler.getQueryResult(channelId, txId, "", query, null)).thenReturn(value);
        try {
            stub.getQueryResult(query).iterator().next();
        } catch (RuntimeException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetHistoryForKey() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final KvQueryResult.KeyModification[] keyModifications = new KvQueryResult.KeyModification[]{
                KvQueryResult.KeyModification.newBuilder()
                        .setTxId("tx0")
                        .setTimestamp(Timestamp.getDefaultInstance())
                        .setValue(ByteString.copyFromUtf8("Value A"))
                        .build(),
                KvQueryResult.KeyModification.newBuilder()
                        .setTxId("tx1")
                        .setTimestamp(Timestamp.getDefaultInstance())
                        .setValue(ByteString.copyFromUtf8("Value B"))
                        .build()
        };
        final QueryResponse value = QueryResponse.newBuilder()
                .setHasMore(false)
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyModifications[0].toByteString()))
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyModifications[1].toByteString()))
                .build();
        when(handler.getHistoryForKey("myc", "txId", "KEY")).thenReturn(value);
        assertThat(stub.getHistoryForKey("KEY"), contains(Arrays.stream(keyModifications).map(KeyModificationImpl::new).toArray()));
    }

    @Test
    public void testGetPrivateData() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        when(handler.getState("myc", "txId", "testcoll", "key")).thenReturn(ByteString.copyFrom(value));
        assertThat(stub.getPrivateData("testcoll", "key"), is(value));
        try {
            stub.getPrivateData(null, "key");
            Assert.fail("Null collection check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.getPrivateData("", "key");
            Assert.fail("Empty collection check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetStringPrivateData() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final String value = "TEST";
        when(handler.getState("myc", "txId", "testcoll", "key")).thenReturn(ByteString.copyFromUtf8(value));
        assertThat(stub.getPrivateDataUTF8("testcoll", "key"), is(value));
    }

    @Test
    public void testGetPrivateDataValidationParameter() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        Map<String, ByteString> metaMap = new HashMap<>();
        metaMap.put(TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(), ByteString.copyFrom(value));
        when(handler.getStateMetadata("myc", "txId", "testcoll", "key")).thenReturn(metaMap);
        assertThat(stub.getPrivateDataValidationParameter("testcoll", "key"), is(value));

        when(handler.getStateMetadata("myc", "txId", "testcoll", "key2")).thenReturn(new HashMap<>());
        assertThat(stub.getPrivateDataValidationParameter("testcoll", "key2"), is(nullValue()));

        try {
            stub.getPrivateDataValidationParameter(null, "key");
            Assert.fail("Null collection check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.getPrivateDataValidationParameter("", "key");
            Assert.fail("Empty collection check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testPutPrivateData() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        stub.putPrivateData("testcoll", "key", value);
        verify(handler).putState("myc", "txId", "testcoll", "key", ByteString.copyFrom(value));
        try {
            stub.putPrivateData(null, "key", value);
            Assert.fail("Null collection check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.putPrivateData("", "key", value);
            Assert.fail("Empty collection check fails");
        } catch (IllegalArgumentException e) {
        }
        try {
            stub.putPrivateData("testcoll", null, value);
            Assert.fail("Null key check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.putPrivateData("testcoll", "", value);
            Assert.fail("Empty key check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testPutStringPrivateData() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final String value = "TEST";
        stub.putPrivateData("testcoll", "key", value);
        verify(handler).putState("myc", "txId", "testcoll", "key", ByteString.copyFromUtf8(value));
    }

    @Test
    public void testSetPrivateDataValidationParameter() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final byte[] value = new byte[]{0x10, 0x20, 0x30};
        stub.setPrivateDataValidationParameter("testcoll", "key", value);
        verify(handler).putStateMetadata("myc", "txId", "testcoll", "key", TransactionPackage.MetaDataKeys.VALIDATION_PARAMETER.toString(), ByteString.copyFrom(value));
        try {
            stub.setPrivateDataValidationParameter(null, "key", value);
            Assert.fail("Null collection check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.setPrivateDataValidationParameter("", "key", value);
            Assert.fail("Empty collection check fails");
        } catch (IllegalArgumentException e) {
        }
        try {
            stub.setPrivateDataValidationParameter("testcoll", null, value);
            Assert.fail("Null key check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.setPrivateDataValidationParameter("testcoll", "", value);
            Assert.fail("Empty key check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDelPrivateState() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        stub.delPrivateData("testcoll", "key");
        verify(handler).deleteState("myc", "txId", "testcoll", "key");
        try {
            stub.delPrivateData(null, "key");
            Assert.fail("Null collection check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.delPrivateData("", "key");
            Assert.fail("Empty collection check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetPrivateDataByRange() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final String startKey = "START";
        final String endKey = "END";
        final KV[] keyValues = new KV[]{
                KV.newBuilder()
                        .setKey("A")
                        .setValue(ByteString.copyFromUtf8("Value of A"))
                        .build(),
                KV.newBuilder()
                        .setKey("B")
                        .setValue(ByteString.copyFromUtf8("Value of B"))
                        .build()
        };
        final QueryResponse value = QueryResponse.newBuilder()
                .setHasMore(false)
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyValues[0].toByteString()))
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyValues[1].toByteString()))
                .build();
        when(handler.getStateByRange("myc", "txId", "testcoll", startKey, endKey, null)).thenReturn(value);
        assertThat(stub.getPrivateDataByRange("testcoll", startKey, endKey), contains(Arrays.stream(keyValues).map(KeyValueImpl::new).toArray()));

        try {
            stub.getPrivateDataByRange(null, startKey, endKey);
            Assert.fail("Null collection check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.getPrivateDataByRange("", startKey, endKey);
            Assert.fail("Empty collection check fails");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGetPrivateDataByPartialCompositeKey() {
        final ChaincodeStubImpl stub = prepareStubAndMockHandler();

        CompositeKey key = new CompositeKey("KEY");
        stub.getPrivateDataByPartialCompositeKey(TEST_COLLECTION, "KEY");
        verify(handler).getStateByRange("myc", "txId", TEST_COLLECTION, key.toString(), key.toString() + "\udbff\udfff", null);

        key = new CompositeKey("");
        stub.getPrivateDataByPartialCompositeKey(TEST_COLLECTION, (String) null);
        stub.getPrivateDataByPartialCompositeKey(TEST_COLLECTION, "");
        verify(handler, times(2)).getStateByRange("myc", "txId", TEST_COLLECTION, key.toString(), key.toString() + "\udbff\udfff", null);
    }

    @Test
    public void testGetPrivateDataByPartialCompositeKey_withAttributesAsString() {

        ChaincodeStubImpl stub = prepareStubAndMockHandler();
        CompositeKey cKey = new CompositeKey("KEY", "attr1", "attr2");
        stub.getPrivateDataByPartialCompositeKey(TEST_COLLECTION, cKey.toString());

        verify(handler).getStateByRange("myc", "txId", TEST_COLLECTION, cKey.toString(), cKey.toString() + "\udbff\udfff", null);
    }

    @Test
    public void testGetPrivateDataByPartialCompositeKey_withAttributesWithSplittedParams() {

        ChaincodeStubImpl stub = prepareStubAndMockHandler();
        CompositeKey cKey = new CompositeKey("KEY", "attr1", "attr2", "attr3");
        stub.getPrivateDataByPartialCompositeKey(TEST_COLLECTION, "KEY", "attr1", "attr2", "attr3");
        verify(handler).getStateByRange("myc", "txId", TEST_COLLECTION, cKey.toString(), cKey.toString() + "\udbff\udfff", null);

    }

    @Test
    public void testGetPrivateDataByPartialCompositeKey_withCompositeKey() {

        ChaincodeStubImpl stub = prepareStubAndMockHandler();

        CompositeKey key = new CompositeKey("KEY");
        stub.getPrivateDataByPartialCompositeKey(TEST_COLLECTION, key);
        verify(handler).getStateByRange("myc", "txId", TEST_COLLECTION, key.toString(), key.toString() + "\udbff\udfff", null);

        key = new CompositeKey("");
        stub.getPrivateDataByPartialCompositeKey(TEST_COLLECTION, key);
        verify(handler).getStateByRange("myc", "txId", TEST_COLLECTION, key.toString(), key.toString() + "\udbff\udfff", null);
    }

    @Test
    public void testGetPrivateDataQueryResult() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), null);
        final KV[] keyValues = new KV[]{
                KV.newBuilder()
                        .setKey("A")
                        .setValue(ByteString.copyFromUtf8("Value of A"))
                        .build(),
                KV.newBuilder()
                        .setKey("B")
                        .setValue(ByteString.copyFromUtf8("Value of B"))
                        .build()
        };
        final QueryResponse value = QueryResponse.newBuilder()
                .setHasMore(false)
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyValues[0].toByteString()))
                .addResults(QueryResultBytes.newBuilder().setResultBytes(keyValues[1].toByteString()))
                .build();
        when(handler.getQueryResult("myc", "txId", "testcoll", "QUERY", null)).thenReturn(value);
        assertThat(stub.getPrivateDataQueryResult("testcoll", "QUERY"), contains(Arrays.stream(keyValues).map(KeyValueImpl::new).toArray()));

        try {
            stub.getPrivateDataQueryResult(null, "QUERY");
            Assert.fail("Null collection check fails");
        } catch (NullPointerException e) {
        }
        try {
            stub.getPrivateDataQueryResult("", "QUERY");
            Assert.fail("Empty collection check fails");
        } catch (IllegalArgumentException e) {
        }

    }

    @Test(expected = InvalidProtocolBufferException.class)
    public void testGetPrivateDataQueryResultWithException() throws Throwable {
        final String txId = "txId", query = "QUERY", channelId = "myc";
        final ChaincodeStubImpl stub = new ChaincodeStubImpl(channelId, txId, handler, Collections.emptyList(), null);
        final QueryResponse value = QueryResponse.newBuilder()
                .setHasMore(false)
                .addResults(QueryResultBytes.newBuilder().setResultBytes(ByteString.copyFromUtf8("exception")))
                .build();
        when(handler.getQueryResult(channelId, txId, "testcoll", query, null)).thenReturn(value);
        try {
            stub.getPrivateDataQueryResult("testcoll", query).iterator().next();
        } catch (RuntimeException e) {
            throw e.getCause();
        }
    }

    @Test(expected = InvalidProtocolBufferException.class)
    public void testGetHistoryForKeyWithException() throws Throwable {
        final String txId = "txId", key = "KEY", channelId = "myc";
        final ChaincodeStubImpl stub = new ChaincodeStubImpl(channelId, txId, handler, Collections.emptyList(), null);
        final QueryResponse value = QueryResponse.newBuilder()
                .setHasMore(false)
                .addResults(QueryResultBytes.newBuilder().setResultBytes(ByteString.copyFromUtf8("exception")))
                .build();
        when(handler.getHistoryForKey(channelId, txId, key)).thenReturn(value);
        try {
            stub.getHistoryForKey(key).iterator().next();
        } catch (RuntimeException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testInvokeChaincode() {
        final String txId = "txId", chaincodeName = "CHAINCODE_ID", channel = "CHAINCODE_CHANNEL";
        final ChaincodeStubImpl stub = new ChaincodeStubImpl(channel, txId, handler, Collections.emptyList(), null);
        final Chaincode.Response expectedResponse = new Chaincode.Response(Status.SUCCESS, "MESSAGE", "PAYLOAD".getBytes(UTF_8));
        when(handler.invokeChaincode(channel, txId, chaincodeName, Collections.emptyList())).thenReturn(expectedResponse);
        assertThat(stub.invokeChaincode(chaincodeName, Collections.emptyList()), is(expectedResponse));

        when(handler.invokeChaincode(eq(channel), eq(txId), eq(chaincodeName + "/" + channel), anyList())).thenReturn(expectedResponse);
        assertThat(stub.invokeChaincode(chaincodeName, Collections.emptyList(), channel), is(expectedResponse));
    }

    @Test
    public void testInvokeChaincodeWithStringArgs() {
        final String txId = "txId", chaincodeName = "CHAINCODE_ID", channel = "CHAINCODE_CHANNEL";
        final ChaincodeStubImpl stub = new ChaincodeStubImpl(channel, txId, handler, Collections.emptyList(), null);
        final Chaincode.Response expectedResponse = new Chaincode.Response(Status.SUCCESS, "MESSAGE", "PAYLOAD".getBytes(UTF_8));
        when(handler.invokeChaincode(channel, txId, chaincodeName, Collections.emptyList())).thenReturn(expectedResponse);
        assertThat(stub.invokeChaincodeWithStringArgs(chaincodeName), is(expectedResponse));

        when(handler.invokeChaincode(channel, txId, chaincodeName, Collections.emptyList())).thenReturn(expectedResponse);
        assertThat(stub.invokeChaincodeWithStringArgs(chaincodeName, Collections.emptyList()), is(expectedResponse));

        when(handler.invokeChaincode(eq(channel), eq(txId), eq(chaincodeName + "/" + channel), anyList())).thenReturn(expectedResponse);
        assertThat(stub.invokeChaincodeWithStringArgs(chaincodeName, Collections.emptyList(), channel), is(expectedResponse));
    }

    @Test
    public void testGetSignedProposal() {
        final SignedProposal signedProposal = SignedProposal.newBuilder()
                .setProposalBytes(Proposal.newBuilder()
                        .setHeader(Header.newBuilder()
                                .setChannelHeader(ChannelHeader.newBuilder()
                                        .setType(ENDORSER_TRANSACTION_VALUE)
                                        .setTimestamp(Timestamp.getDefaultInstance())
                                        .build().toByteString()
                                )
                                .build().toByteString()
                        )
                        .build().toByteString()
                ).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), signedProposal);
        assertThat(stub.getSignedProposal(), is(signedProposal));
    }

    @Test
    public void testGetSignedProposalWithEmptyProposal() {
        final SignedProposal signedProposal = SignedProposal.newBuilder().setProposalBytes(ByteString.EMPTY).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txId", handler, Collections.emptyList(), signedProposal);
        assertThat(stub.getSignedProposal(), is(signedProposal));
    }

    @Test
    public void testGetTxTimestamp() {
        final Instant instant = Instant.now();
        final Timestamp timestamp = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
        final SignedProposal signedProposal = SignedProposal.newBuilder()
                .setProposalBytes(Proposal.newBuilder()
                        .setHeader(Header.newBuilder()
                                .setChannelHeader(ChannelHeader.newBuilder()
                                        .setType(ENDORSER_TRANSACTION_VALUE)
                                        .setTimestamp(timestamp)
                                        .build().toByteString()
                                )
                                .build().toByteString()
                        )
                        .build().toByteString()
                ).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txid", handler, new ArrayList<>(), signedProposal);
        assertThat(stub.getTxTimestamp(), is(instant));
    }

    @Test
    public void testGetTxTimestampNullSignedProposal() {
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txid", handler, new ArrayList<>(), null);
        assertThat(stub.getTxTimestamp(), is(nullValue()));
    }

    @Test
    public void testGetTxTimestampEmptySignedProposal() {
        final SignedProposal signedProposal = SignedProposal.newBuilder().setProposalBytes(ByteString.EMPTY).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txid", handler, new ArrayList<>(), signedProposal);
        assertThat(stub.getTxTimestamp(), is(nullValue()));
    }

    @Test
    public void testGetCreator() {
        final Instant instant = Instant.now();
        final byte[] creator = "CREATOR".getBytes(UTF_8);
        final Timestamp timestamp = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
        final SignedProposal signedProposal = SignedProposal.newBuilder()
                .setProposalBytes(Proposal.newBuilder()
                        .setHeader(Header.newBuilder()
                                .setChannelHeader(ChannelHeader.newBuilder()
                                        .setType(ENDORSER_TRANSACTION_VALUE)
                                        .setTimestamp(timestamp)
                                        .build().toByteString()
                                )
                                .setSignatureHeader(SignatureHeader.newBuilder()
                                        .setCreator(ByteString.copyFrom(creator))
                                        .build().toByteString()
                                )
                                .build().toByteString()
                        )
                        .build().toByteString()
                ).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txid", handler, new ArrayList<>(), signedProposal);
        assertThat(stub.getCreator(), is(creator));
    }

    @Test
    public void testGetTransient() {
        final SignedProposal signedProposal = SignedProposal.newBuilder()
                .setProposalBytes(Proposal.newBuilder()
                        .setHeader(Header.newBuilder()
                                .setChannelHeader(ChannelHeader.newBuilder()
                                        .setType(ENDORSER_TRANSACTION_VALUE)
                                        .setTimestamp(Timestamp.getDefaultInstance())
                                        .build().toByteString()
                                )
                                .build().toByteString()
                        )
                        .setPayload(ChaincodeProposalPayload.newBuilder()
                                .putTransientMap("key0", ByteString.copyFromUtf8("value0"))
                                .putTransientMap("key1", ByteString.copyFromUtf8("value1"))
                                .build().toByteString()
                        )
                        .build().toByteString()
                ).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txid", handler, new ArrayList<>(), signedProposal);
        assertThat(stub.getTransient(), allOf(
                hasEntry("key0", "value0".getBytes(UTF_8)),
                hasEntry("key1", "value1".getBytes(UTF_8))
        ));
    }

    @Test
    public void testGetBinding() {
        final byte[] expectedDigest = DatatypeConverter.parseHexBinary("5093dd4f4277e964da8f4afbde0a9674d17f2a6a5961f0670fc21ae9b67f2983");
        final SignedProposal signedProposal = SignedProposal.newBuilder()
                .setProposalBytes(Proposal.newBuilder()
                        .setHeader(Header.newBuilder()
                                .setChannelHeader(ChannelHeader.newBuilder()
                                        .setType(ENDORSER_TRANSACTION_VALUE)
                                        .setTimestamp(Timestamp.getDefaultInstance())
                                        .setEpoch(10)
                                        .build().toByteString()
                                )
                                .setSignatureHeader(SignatureHeader.newBuilder()
                                        .setNonce(ByteString.copyFromUtf8("nonce"))
                                        .setCreator(ByteString.copyFromUtf8("creator"))
                                        .build().toByteString()
                                )
                                .build().toByteString()
                        )
                        .build().toByteString()
                ).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txid", handler, new ArrayList<>(), signedProposal);
        assertThat(stub.getBinding(), is(expectedDigest));
    }

    @Test
    public void testGetBindingEmptyProposal() {
        final SignedProposal signedProposal = SignedProposal.newBuilder().setProposalBytes(ByteString.EMPTY).build();
        final ChaincodeStubImpl stub = new ChaincodeStubImpl("myc", "txid", handler, new ArrayList<>(), signedProposal);
        assertThat(stub.getBinding(), is((byte[]) null));
    }
}
