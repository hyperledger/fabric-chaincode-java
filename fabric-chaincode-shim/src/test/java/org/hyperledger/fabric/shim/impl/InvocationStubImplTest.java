/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.GET_STATE_BY_RANGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.GetStateByRange;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResponse;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class InvocationStubImplTest {

    private final String channelId = "mychannel";
    private final String txId = "0xCAFEBABE";
    private final String simpleKeyStartNamespace = new String(Character.toChars(0x000001));

    @Nested
    class GetStateByRangeTests {

        private InvocationStubImpl stubImpl;
        private ArgumentCaptor<ChaincodeMessage> chaincodeMessageCaptor;
        private ChaincodeInvocationTask mockHandler;

        @BeforeEach
        public void beforeEach() throws Exception {
            final ChaincodeMessage mockMessage = ChaincodeMessageFactory.newGetStateEventMessage(channelId, txId, "",
                    "key");
            mockHandler = mock(ChaincodeInvocationTask.class);
            final ByteString mockString = QueryResponse.newBuilder().build().toByteString();

            chaincodeMessageCaptor = ArgumentCaptor.forClass(ChaincodeMessage.class);

            when(mockHandler.invoke(any())).thenReturn(mockString);
            stubImpl = new InvocationStubImpl(mockMessage, mockHandler);
        }

        @Test
        public void regular() throws InvalidProtocolBufferException {
            final QueryResultsIterator<KeyValue> qri = stubImpl.getStateByRange("Aardvark", "Zebra");

            verify(mockHandler).invoke(chaincodeMessageCaptor.capture());
            assertThat(qri).isNotNull();

            final ChaincodeMessage msg = chaincodeMessageCaptor.getValue();
            assertThat(msg.getTxid()).isEqualTo("0xCAFEBABE");
            assertThat(msg.getType()).isEqualTo(GET_STATE_BY_RANGE);

            final GetStateByRange range = GetStateByRange.parseFrom(msg.getPayload());
            assertThat(range.getStartKey()).isEqualTo("Aardvark");
            assertThat(range.getEndKey()).isEqualTo("Zebra");
        }

        @Test
        public void nullvalues() throws InvalidProtocolBufferException {
            final QueryResultsIterator<KeyValue> qri = stubImpl.getStateByRange(null, null);

            verify(mockHandler).invoke(chaincodeMessageCaptor.capture());
            assertThat(qri).isNotNull();

            final ChaincodeMessage msg = chaincodeMessageCaptor.getValue();
            assertThat(msg.getTxid()).isEqualTo("0xCAFEBABE");
            assertThat(msg.getType()).isEqualTo(GET_STATE_BY_RANGE);

            final GetStateByRange range = GetStateByRange.parseFrom(msg.getPayload());

            assertThat(range.getStartKey()).isEqualTo(simpleKeyStartNamespace);
            assertThat(range.getEndKey()).isEqualTo("");
        }

        @Test
        public void unbounded() throws InvalidProtocolBufferException {
            final QueryResultsIterator<KeyValue> qri = stubImpl.getStateByRange("", "");

            verify(mockHandler).invoke(chaincodeMessageCaptor.capture());
            assertThat(qri).isNotNull();

            final ChaincodeMessage msg = chaincodeMessageCaptor.getValue();
            assertThat(msg.getTxid()).isEqualTo("0xCAFEBABE");
            assertThat(msg.getType()).isEqualTo(GET_STATE_BY_RANGE);

            final GetStateByRange range = GetStateByRange.parseFrom(msg.getPayload());

            assertThat(range.getStartKey()).isEqualTo(simpleKeyStartNamespace);
            assertThat(range.getEndKey()).isEqualTo("");
        }

        @Test
        public void simplekeys() {
            assertThatThrownBy(() -> {
                final QueryResultsIterator<KeyValue> qri = stubImpl
                        .getStateByRange(new String(Character.toChars(Character.MIN_CODE_POINT)), "");
            }).hasMessageContaining("not allowed");

        }

    }

}
