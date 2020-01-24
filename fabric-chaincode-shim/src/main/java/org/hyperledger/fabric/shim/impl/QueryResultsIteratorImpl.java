/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.impl;

import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.QUERY_STATE_CLOSE;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.QUERY_STATE_NEXT;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResponse;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResultBytes;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryStateClose;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryStateNext;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * This class provides an ITERABLE object of query results.
 *
 * NOTE the class name
 * is misleading - as this class is not an iterator itself, rather it implements
 * java.lang.Iterable via the QueryResultsIterator
 *
 * public interface QueryResultsIterator<T> extends Iterable<T>, AutoCloseable
 *
 * @param <T>
 */
class QueryResultsIteratorImpl<T> implements QueryResultsIterator<T> {

    private final ChaincodeInvocationTask handler;
    private final String channelId;
    private final String txId;
    private Iterator<QueryResultBytes> currentIterator;
    private QueryResponse currentQueryResponse;
    private Function<QueryResultBytes, T> mapper;

    QueryResultsIteratorImpl(final ChaincodeInvocationTask handler, final String channelId, final String txId, final ByteString responseBuffer,
            final Function<QueryResultBytes, T> mapper) {

        try {
            this.handler = handler;
            this.channelId = channelId;
            this.txId = txId;
            this.currentQueryResponse = QueryResponse.parseFrom(responseBuffer);
            this.currentIterator = currentQueryResponse.getResultsList().iterator();
            this.mapper = mapper;
        } catch (final InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return currentIterator.hasNext() || currentQueryResponse.getHasMore();
            }

            @Override
            public T next() {

                // return next fetched result, if any
                if (currentIterator.hasNext()) {
                    return mapper.apply(currentIterator.next());
                }

                // throw exception if there are no more expected results
                if (!currentQueryResponse.getHasMore()) {
                    throw new NoSuchElementException();
                }

                // get more results from peer

                final ByteString requestPayload = QueryStateNext.newBuilder().setId(currentQueryResponse.getId()).build().toByteString();
                final ChaincodeMessage requestNextMessage = ChaincodeMessageFactory.newEventMessage(QUERY_STATE_NEXT, channelId, txId, requestPayload);

                final ByteString responseMessage = QueryResultsIteratorImpl.this.handler.invoke(requestNextMessage);
                try {
                    currentQueryResponse = QueryResponse.parseFrom(responseMessage);
                } catch (final InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
                currentIterator = currentQueryResponse.getResultsList().iterator();

                // return next fetched result
                return mapper.apply(currentIterator.next());

            }

        };
    }

    @Override
    public void close() throws Exception {

        final ByteString requestPayload = QueryStateClose.newBuilder().setId(currentQueryResponse.getId()).build().toByteString();

        final ChaincodeMessage requestNextMessage = ChaincodeMessageFactory.newEventMessage(QUERY_STATE_CLOSE, channelId, txId, requestPayload);
        this.handler.invoke(requestNextMessage);

        this.currentIterator = Collections.emptyIterator();
        this.currentQueryResponse = QueryResponse.newBuilder().setHasMore(false).build();
    }

}
