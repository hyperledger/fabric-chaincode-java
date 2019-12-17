/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;

import com.google.protobuf.ByteString;

/**
 * Base class for multi result query steps/messages
 */
public abstract class QueryResultStep implements ScenarioStep {
    protected ChaincodeShim.ChaincodeMessage orgMsg;
    protected final String[] values;
    protected final boolean hasNext;

    /**
     * Initiate step
     *
     * @param hasNext is response message QueryResponse hasMore field set
     * @param vals    list of keys to generate ("key" => "key Value") pairs
     */
    QueryResultStep(final boolean hasNext, final String... vals) {
        this.values = vals;
        this.hasNext = hasNext;
    }

    /**
     * Generate response message that list of (key => value) pairs
     *
     * @return
     */
    @Override
    public List<ChaincodeShim.ChaincodeMessage> next() {
        final List<KvQueryResult.KV> keyValues = Arrays.asList(values).stream().map(x -> KvQueryResult.KV.newBuilder()
                .setKey(x)
                .setValue(ByteString.copyFromUtf8(x + " Value"))
                .build()).collect(toList());

        final ChaincodeShim.QueryResponse.Builder builder = ChaincodeShim.QueryResponse.newBuilder();
        builder.setHasMore(hasNext);
        keyValues.stream().forEach(kv -> builder.addResults(ChaincodeShim.QueryResultBytes.newBuilder().setResultBytes(kv.toByteString())));
        final ByteString rangePayload = builder.build().toByteString();

        final List<ChaincodeShim.ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(ChaincodeShim.ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(rangePayload)
                .build());
        return list;
    }
}
