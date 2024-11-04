/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.ByteString;
import java.util.function.Function;
import org.hyperledger.fabric.protos.peer.QueryResponse;
import org.hyperledger.fabric.protos.peer.QueryResponseMetadata;
import org.hyperledger.fabric.protos.peer.QueryResultBytes;
import org.junit.jupiter.api.Test;

final class QueryResultsIteratorWithMetadataImplTest {
    private static final Function<QueryResultBytes, Integer> QUERY_RESULT_BYTES_TO_KV = queryResultBytes -> 0;

    @Test
    void getMetadata() {
        final QueryResultsIteratorWithMetadataImpl<Integer> testIter = new QueryResultsIteratorWithMetadataImpl<>(
                null, "", "", prepareQueryResponse().toByteString(), QUERY_RESULT_BYTES_TO_KV);
        assertThat(testIter.getMetadata().getBookmark()).isEqualTo("asdf");
        assertThat(testIter.getMetadata().getFetchedRecordsCount()).isEqualTo(2);
    }

    @Test
    void getInvalidMetadata() {
        assertThatThrownBy(() -> new QueryResultsIteratorWithMetadataImpl<>(
                        null, "", "", prepareQueryResponseWrongMeta().toByteString(), QUERY_RESULT_BYTES_TO_KV))
                .isInstanceOf(RuntimeException.class);
    }

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
