/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import java.util.function.Function;
import java.util.logging.Logger;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResponse;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResultBytes;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implementation of {@link QueryResultsIteratorWithMetadata}, by extending {@link org.hyperledger.fabric.shim.ledger.QueryResultsIterator} implementations, {@link QueryResultsIteratorImpl}
 *
 * @param <T>
 */
public class QueryResultsIteratorWithMetadataImpl<T> extends QueryResultsIteratorImpl<T> implements QueryResultsIteratorWithMetadata<T> {
    private static Logger logger = Logger.getLogger(QueryResultsIteratorWithMetadataImpl.class.getName());

    ChaincodeShim.QueryResponseMetadata metadata;

    public QueryResultsIteratorWithMetadataImpl(final ChaincodeInnvocationTask handler,
			final String channelId, final String txId, final ByteString responseBuffer,
			Function<QueryResultBytes, T> mapper) {
        super(handler,channelId,txId,responseBuffer,mapper);
        try {
        	QueryResponse queryResponse = QueryResponse.parseFrom(responseBuffer);
            metadata = ChaincodeShim.QueryResponseMetadata.parseFrom(queryResponse.getMetadata());
        } catch (InvalidProtocolBufferException e) {
            logger.warning("can't parse response metadata");
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChaincodeShim.QueryResponseMetadata getMetadata() {
        return metadata;
    }
}
