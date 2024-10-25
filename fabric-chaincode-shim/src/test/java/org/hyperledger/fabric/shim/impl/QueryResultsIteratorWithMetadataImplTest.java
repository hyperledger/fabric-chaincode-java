/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.protobuf.ByteString;
import java.util.function.Function;
import org.hyperledger.fabric.protos.peer.QueryResponse;
import org.hyperledger.fabric.protos.peer.QueryResponseMetadata;
import org.hyperledger.fabric.protos.peer.QueryResultBytes;
import org.junit.jupiter.api.Test;

public class QueryResultsIteratorWithMetadataImplTest {

    @Test
    public void getMetadata() {
        final QueryResultsIteratorWithMetadataImpl<Integer> testIter = new QueryResultsIteratorWithMetadataImpl<>(
                null, "", "", prepareQueryResponse().toByteString(), queryResultBytesToKv);
        assertThat(testIter.getMetadata().getBookmark(), is("asdf"));
        assertThat(testIter.getMetadata().getFetchedRecordsCount(), is(2));
    }

    @Test
    public void getInvalidMetadata() {
        try {
            new QueryResultsIteratorWithMetadataImpl<>(
                    null, "", "", prepareQueryResponseWrongMeta().toByteString(), queryResultBytesToKv);
            fail();
        } catch (final RuntimeException e) {
        }
    }

    private final Function<QueryResultBytes, Integer> queryResultBytesToKv = new Function<QueryResultBytes, Integer>() {
        @Override
        public Integer apply(final QueryResultBytes queryResultBytes) {
            return 0;
        }
    };

    private QueryResponse prepareQueryResponse() {
        final QueryResponseMetadata qrm = QueryResponseMetadata.newBuilder()
                .setBookmark("asdf")
                .setFetchedRecordsCount(2)
                .build();

        return QueryResponse.newBuilder()
                .setHasMore(false)
                .setMetadata(qrm.toByteString())
                .build();
    }

    private QueryResponse prepareQueryResponseWrongMeta() {
        final ByteString bs = ByteString.copyFrom(new byte[] {0, 0});

        return QueryResponse.newBuilder().setHasMore(false).setMetadata(bs).build();
    }
}
