/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.hamcrest.Matchers;
import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.Function;

import static org.hamcrest.Matchers.*;


import static org.junit.Assert.*;

public class QueryResultsIteratorWithMetadataImplTest {

    @Test
    public void getMetadata() {
        QueryResultsIteratorWithMetadataImpl<Integer> testIter = new QueryResultsIteratorWithMetadataImpl<>(null, "", "", prepareQueryResopnse().toByteString(), queryResultBytesToKv);
        assertThat(testIter.getMetadata().getBookmark(), is("asdf"));
        assertThat(testIter.getMetadata().getFetchedRecordsCount(), is(2));
    }

    @Test
    public void getInvalidMetadata() {
        try {
            new QueryResultsIteratorWithMetadataImpl<>(null, "", "", prepareQueryResopnseWrongMeta().toByteString(), queryResultBytesToKv);
            fail();
        } catch (RuntimeException e) {
        }
    }


    private Function<ChaincodeShim.QueryResultBytes, Integer> queryResultBytesToKv = new Function<ChaincodeShim.QueryResultBytes, Integer>() {
        public Integer apply(ChaincodeShim.QueryResultBytes queryResultBytes) {
            return new Integer(0);
        }
    };

    private ChaincodeShim.QueryResponse prepareQueryResopnse () {
        final ChaincodeShim.QueryResponseMetadata qrm = ChaincodeShim.QueryResponseMetadata.newBuilder()
        .setBookmark("asdf")
        .setFetchedRecordsCount(2)
        .build();

        return ChaincodeShim.QueryResponse.newBuilder()
                .setHasMore(false)
                .setMetadata(qrm.toByteString())
                .build();

    }

    private ChaincodeShim.QueryResponse prepareQueryResopnseWrongMeta () {
        ByteString bs = ByteString.copyFrom(new byte[]{0, 0});

        return ChaincodeShim.QueryResponse.newBuilder()
                .setHasMore(false)
                .setMetadata(bs)
                .build();

    }
}