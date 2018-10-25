/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Implementation of {@link QueryResultsIteratorWithMetadata}, by extending {@link org.hyperledger.fabric.shim.ledger.QueryResultsIterator} implementations, {@link QueryResultsIteratorImpl}
 *
 * @param <T>
 */
public class QueryResultsIteratorWithMetadataImpl<T> extends QueryResultsIteratorImpl<T> implements QueryResultsIteratorWithMetadata<T> {
    private static Logger logger = Logger.getLogger(QueryResultsIteratorWithMetadataImpl.class.getName());

    ChaincodeShim.QueryResponseMetadata metadata;

    public QueryResultsIteratorWithMetadataImpl(Handler handler, String channelId, String txId, ChaincodeShim.QueryResponse queryResponse, Function<ChaincodeShim.QueryResultBytes, T> mapper) {
        super(handler, channelId, txId, queryResponse, mapper);
        try {
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
