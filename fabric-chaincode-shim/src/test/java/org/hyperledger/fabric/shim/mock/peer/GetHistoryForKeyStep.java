/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import static java.util.stream.Collectors.toList;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hyperledger.fabric.protos.ledger.queryresult.KeyModification;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.QueryResponse;
import org.hyperledger.fabric.protos.peer.QueryResultBytes;

public final class GetHistoryForKeyStep implements ScenarioStep {
    private ChaincodeMessage orgMsg;
    private final String[] values;
    private final boolean hasNext;

    /**
     * Initiate step
     *
     * @param hasNext is response message QueryResponse hasMore field set
     * @param vals list of keys to generate ("key" => "key Value") pairs
     */
    public GetHistoryForKeyStep(final boolean hasNext, final String... vals) {
        this.values = vals;
        this.hasNext = hasNext;
    }

    @Override
    public boolean expected(final ChaincodeMessage msg) {
        orgMsg = msg;
        return msg.getType() == ChaincodeMessage.Type.GET_HISTORY_FOR_KEY;
    }

    @Override
    public List<ChaincodeMessage> next() {
        final List<KeyModification> keyModifications = Arrays.asList(values).stream()
                .map(x -> KeyModification.newBuilder()
                        .setTxId(x)
                        .setValue(ByteString.copyFromUtf8(x + " Value"))
                        .build())
                .collect(toList());

        final QueryResponse.Builder builder = QueryResponse.newBuilder();
        builder.setHasMore(hasNext);
        keyModifications.stream()
                .forEach(kv -> builder.addResults(QueryResultBytes.newBuilder().setResultBytes(kv.toByteString())));
        final ByteString historyPayload = builder.build().toByteString();

        final List<ChaincodeMessage> list = new ArrayList<>();
        list.add(ChaincodeMessage.newBuilder()
                .setType(ChaincodeMessage.Type.RESPONSE)
                .setChannelId(orgMsg.getChannelId())
                .setTxid(orgMsg.getTxid())
                .setPayload(historyPayload)
                .build());
        return list;
    }
}
